package com.proyectointegrador.monitoreo.reportes

import com.proyectointegrador.monitoreo.data.AppDatabase
import com.proyectointegrador.monitoreo.data.AppMonitoreada
import com.proyectointegrador.monitoreo.data.AvisoPausa
import com.proyectointegrador.monitoreo.data.PerfilMenor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId

enum class EstadoUso { DENTRO, CERCA, ALCANZADO }

data class UsoApp(val app: AppMonitoreada, val minutos: Int) {
    val fraccion: Float get() = (minutos.toFloat() / app.limiteDiarioMin).coerceIn(0f, 1f)

    val estado: EstadoUso
        get() = when {
            minutos >= app.limiteDiarioMin -> EstadoUso.ALCANZADO
            minutos >= app.limiteDiarioMin * 0.8 -> EstadoUso.CERCA
            else -> EstadoUso.DENTRO
        }
}

data class ResumenDelDia(
    val perfil: PerfilMenor?,
    val usos: List<UsoApp>,
    val pausasHoy: Int,
    val ultimoReconocimiento: Long?,
    /** Veces que el niño volvió a abrir cada app después del primer aviso */
    val intentosReapertura: List<Pair<AppMonitoreada, Int>>,
) {
    val hayActividad: Boolean get() = usos.any { it.minutos > 0 } || pausasHoy > 0
}

/** Prepara los datos del resumen y del historial */
class GeneradorReporte(private val db: AppDatabase) {

    fun resumenDeHoy(): Flow<ResumenDelDia> {
        val hoy = LocalDate.now()
        val inicioDelDia = hoy.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return combine(
            db.perfilDao().observarPerfil(),
            db.appMonitoreadaDao().observarTodas(),
            db.registroUsoDao().observarDelDia(hoy.toString()),
            db.avisoPausaDao().observarDesde(inicioDelDia),
        ) { perfil, apps, registros, avisos ->
            val minutosPorApp = registros.associate { it.paquete to it.minutosUso }
            ResumenDelDia(
                perfil = perfil,
                usos = apps.map { UsoApp(it, minutosPorApp[it.paquete] ?: 0) },
                pausasHoy = avisos.size,
                ultimoReconocimiento = avisos.mapNotNull { it.momentoReconocimiento }.maxOrNull(),
                intentosReapertura = CalculadoraPausas.intentosDeReapertura(avisos).let { intentos ->
                    apps.mapNotNull { app -> intentos[app.paquete]?.let { app to it } }
                },
            )
        }
    }

    /** Historial de los últimos 7 días por app */
    fun historialSemanal(): Flow<List<HistorialApp>> {
        val hoy = LocalDate.now()
        val fechas = CalculadoraHistorial.fechas(hoy)
        return combine(
            db.appMonitoreadaDao().observarTodas(),
            db.registroUsoDao().observarEntre(fechas.first().toString(), fechas.last().toString()),
        ) { apps, registros -> CalculadoraHistorial.armar(apps, registros, hoy) }
    }
}

object CalculadoraPausas {
    /**
     * El primer aviso del día es cuando se alcanza el límite. Cada aviso siguiente significa
     * que el niño volvió a abrir la app, y cuenta como un intento.
     */
    fun intentosDeReapertura(avisosDelDia: List<AvisoPausa>): Map<String, Int> =
        avisosDelDia.groupingBy { it.paquete }.eachCount()
            .mapValues { it.value - 1 }
            .filterValues { it > 0 }
}
