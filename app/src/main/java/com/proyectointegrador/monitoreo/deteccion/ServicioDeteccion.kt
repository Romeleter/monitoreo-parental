package com.proyectointegrador.monitoreo.deteccion

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.proyectointegrador.monitoreo.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Módulo de detección de uso (HU-01 / CU-02): consulta UsageStatsManager,
 * suma el tiempo de las apps monitoreadas y lo guarda en RegistroUso.
 */
class ServicioDeteccion(private val context: Context, private val db: AppDatabase) {

    private val preferencias = context.getSharedPreferences("deteccion", Context.MODE_PRIVATE)

    private val _ultimaActualizacion = MutableStateFlow(
        preferencias.getLong(CLAVE_ULTIMA, 0L).takeIf { it > 0 },
    )

    /** Momento de la última consulta exitosa, para mostrarlo en el panel (RF-02). */
    val ultimaActualizacion: StateFlow<Long?> = _ultimaActualizacion.asStateFlow()

    /**
     * Recalcula el uso del día actual. Si la última actualización fue un día anterior,
     * también cierra ese día para que el historial semanal quede completo.
     */
    suspend fun actualizarUso() = withContext(Dispatchers.IO) {
        if (!PermisoUso.estaConcedido(context)) return@withContext

        val zona = ZoneId.systemDefault()
        val hoy = LocalDate.now(zona)
        _ultimaActualizacion.value
            ?.let { Instant.ofEpochMilli(it).atZone(zona).toLocalDate() }
            ?.takeIf { it.isBefore(hoy) }
            ?.let { diaAnterior -> actualizarDia(diaAnterior, zona) }

        actualizarDia(hoy, zona)

        val ahora = System.currentTimeMillis()
        preferencias.edit().putLong(CLAVE_ULTIMA, ahora).apply()
        _ultimaActualizacion.value = ahora
    }

    /** Milisegundos de uso de hoy para una sola app, incluida la sesión abierta ahora mismo. */
    suspend fun milisegundosHoy(paquete: String): Long = withContext(Dispatchers.IO) {
        val zona = ZoneId.systemDefault()
        val inicio = LocalDate.now(zona).atStartOfDay(zona).toInstant().toEpochMilli()
        val ahora = System.currentTimeMillis()
        val pausas = leerPausas(inicio, ahora)
        CalculadoraUso.milisegundosPorApp(leerEventos(inicio, ahora), setOf(paquete), inicio, ahora, pausas)[paquete]
            ?: 0L
    }

    /** Intervalos en que el aviso de pausa estuvo visible; un aviso aún abierto llega hasta [fin]. */
    private suspend fun leerPausas(inicio: Long, fin: Long): List<IntervaloPausa> =
        db.avisoPausaDao().obtenerEntre(inicio, fin).map {
            IntervaloPausa(it.paquete, it.momentoMostrado, it.momentoCierre ?: fin)
        }

    private suspend fun actualizarDia(dia: LocalDate, zona: ZoneId) {
        val paquetes = db.appMonitoreadaDao().observarTodas().first()
            .filter { it.monitoreoActivo }
            .map { it.paquete }
            .toSet()
        if (paquetes.isEmpty()) return

        val inicio = dia.atStartOfDay(zona).toInstant().toEpochMilli()
        val finDelDia = dia.plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli()
        val fin = minOf(finDelDia, System.currentTimeMillis())

        val usoPorApp = CalculadoraUso.milisegundosPorApp(
            leerEventos(inicio, fin),
            paquetes,
            inicio,
            fin,
            leerPausas(inicio, fin),
        )
        paquetes.forEach { paquete ->
            val minutos = CalculadoraUso.aMinutos(usoPorApp[paquete] ?: 0L)
            db.registroUsoDao().guardarMinutos(paquete, dia.toString(), minutos)
        }
    }

    /** Lee los eventos de primer plano de Android entre [inicio] y [fin]. */
    private fun leerEventos(inicio: Long, fin: Long): List<EventoUso> {
        val usageStats = context.getSystemService(UsageStatsManager::class.java)
        val eventos = usageStats.queryEvents(inicio, fin)
        val evento = UsageEvents.Event()
        val lista = mutableListOf<EventoUso>()
        while (eventos.hasNextEvent()) {
            eventos.getNextEvent(evento)
            val tipo = when (evento.eventType) {
                EVENTO_ABIERTA -> EventoUso.Tipo.ABIERTA
                EVENTO_CERRADA -> EventoUso.Tipo.CERRADA
                else -> continue
            }
            lista += EventoUso(evento.packageName, evento.timeStamp, tipo)
        }
        return lista
    }

    companion object {
        private const val CLAVE_ULTIMA = "ultima_actualizacion"

        // ACTIVITY_RESUMED / ACTIVITY_PAUSED (antes MOVE_TO_FOREGROUND / MOVE_TO_BACKGROUND).
        // Se usan los valores numéricos porque los nombres nuevos solo existen desde Android 10.
        private const val EVENTO_ABIERTA = 1
        private const val EVENTO_CERRADA = 2
    }
}
