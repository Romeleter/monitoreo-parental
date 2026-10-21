package com.proyectointegrador.monitoreo.pausas

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log
import com.proyectointegrador.monitoreo.data.AppDatabase
import com.proyectointegrador.monitoreo.data.AppMonitoreada
import com.proyectointegrador.monitoreo.data.AvisoPausa
import com.proyectointegrador.monitoreo.deteccion.ServicioDeteccion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Módulo de pausas (HU-06, HU-07, HU-03 / CU-04, CU-05).
 * Cada pocos segundos revisa qué app está en pantalla y, si debe pausarse,
 * muestra el aviso y registra la activación y el reconocimiento.
 */
class ControladorPausa(
    private val context: Context,
    private val db: AppDatabase,
    private val deteccion: ServicioDeteccion,
    private val scope: CoroutineScope,
) {
    private val usageStats = context.getSystemService(UsageStatsManager::class.java)
    private val audio = context.getSystemService(AudioManager::class.java)
    private val ventana = VentanaAviso(context)

    // Pedir el foco de audio hace que TikTok, Instagram o YouTube pausen el video que suena detrás del aviso.
    private val focoAudio = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build()

    private var appEnPrimerPlano: String? = null
    private var ultimaConsulta = System.currentTimeMillis() - CONSULTA_INICIAL_MS

    // Uso de hoy de la app abierta: se calcula completo al abrirla y luego se suma el tiempo transcurrido.
    private var paqueteMedido: String? = null
    private var usoBaseMs = 0L
    private var momentoBase = 0L

    private var avisoActualId: Long? = null
    private var ultimoReconocimiento = 0L

    init {
        // Avisos que quedaron abiertos si el servicio se detuvo antes de cerrarlos.
        scope.launch { db.avisoPausaDao().cerrarPendientes() }
    }

    /** Una revisión. La llama [ServicioVigilancia] cada [ServicioVigilancia.INTERVALO_MS]. */
    suspend fun revisar() {
        val ahora = System.currentTimeMillis()
        actualizarAppEnPrimerPlano(ahora)

        val app = appEnPrimerPlano?.let { db.appMonitoreadaDao().obtener(it) }
        val motivo = app?.let { motivoDePausa(it, ahora) }

        if (app == null || motivo == null) {
            // El niño salió de la app o ya no hay que pausarla: se quita el aviso sin marcarlo como reconocido.
            cerrarAviso(ahora)
            if (app == null) paqueteMedido = null
            return
        }
        if (ventana.visible) return
        // Pequeño margen tras pulsar "Entendido" para que Android termine de ir a la pantalla de inicio.
        if (ahora - ultimoReconocimiento < MARGEN_TRAS_RECONOCER_MS) return

        mostrarAviso(app, motivo, ahora)
    }

    fun liberar() {
        if (ventana.visible) ventana.ocultar()
        audio.abandonAudioFocusRequest(focoAudio)
    }

    private suspend fun motivoDePausa(app: AppMonitoreada, ahora: Long): MotivoPausa? {
        if (app.bloqueoManual) return MotivoPausa.BLOQUEO_MANUAL
        if (!app.monitoreoActivo) return null
        val limiteMs = app.limiteDiarioMin * 60_000L
        return if (usoDeHoyMs(app.paquete, ahora) >= limiteMs) MotivoPausa.LIMITE else null
    }

    private suspend fun usoDeHoyMs(paquete: String, ahora: Long): Long {
        if (paquete != paqueteMedido || ahora - momentoBase > RECALCULAR_CADA_MS) {
            usoBaseMs = deteccion.milisegundosHoy(paquete)
            momentoBase = ahora
            paqueteMedido = paquete
        }
        return usoBaseMs + (ahora - momentoBase)
    }

    private suspend fun mostrarAviso(app: AppMonitoreada, motivo: MotivoPausa, ahora: Long) {
        val mensaje = when (motivo) {
            MotivoPausa.LIMITE -> "Límite diario alcanzado"
            MotivoPausa.BLOQUEO_MANUAL -> "Bloqueo manual"
        }
        val id = db.avisoPausaDao().insertar(
            AvisoPausa(
                paquete = app.paquete,
                mensaje = mensaje,
                tiempoEsperaSeg = VentanaAviso.SEGUNDOS_ESPERA,
                momentoMostrado = ahora,
            ),
        )
        db.registroUsoDao().sumarActivacion(app.paquete, LocalDate.now().toString())
        avisoActualId = id

        withContext(Dispatchers.Main) {
            ventana.mostrar(app.nombre, motivo, onEntendido = { scope.launch { reconocer(id) } })
            audio.requestAudioFocus(focoAudio)
        }
    }

    /** Quita el aviso (si está visible) y guarda cuándo dejó de verse. */
    private suspend fun cerrarAviso(ahora: Long) {
        if (avisoActualId == null && !ventana.visible) return
        avisoActualId?.let { db.avisoPausaDao().marcarCerrado(it, ahora) }
        avisoActualId = null
        withContext(Dispatchers.Main) {
            if (ventana.visible) ventana.ocultar()
            audio.abandonAudioFocusRequest(focoAudio)
        }
    }

    private suspend fun reconocer(id: Long) {
        val ahora = System.currentTimeMillis()
        ultimoReconocimiento = ahora
        db.avisoPausaDao().marcarReconocido(id, ahora)
        cerrarAviso(ahora)
        withContext(Dispatchers.Main) { irAPantallaDeInicio() }
    }

    /** Tras el aviso se cierra la app de video llevando al niño a la pantalla de inicio. */
    private fun irAPantallaDeInicio() {
        val inicio = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(inicio)
        } catch (e: Exception) {
            // Si Android no lo permite, el aviso volverá a salir mientras la app siga abierta.
            Log.w(TAG, "No se pudo ir a la pantalla de inicio", e)
        }
    }

    /** Lee los eventos nuevos desde la última consulta para saber qué app está en pantalla. */
    private suspend fun actualizarAppEnPrimerPlano(ahora: Long) = withContext(Dispatchers.IO) {
        // Se repasan unos segundos de más por si Android registró algún evento con retraso.
        val eventos = usageStats.queryEvents(ultimaConsulta - SOLAPE_MS, ahora)
        val evento = UsageEvents.Event()
        while (eventos.hasNextEvent()) {
            eventos.getNextEvent(evento)
            when (evento.eventType) {
                EVENTO_ABIERTA -> appEnPrimerPlano = evento.packageName
                EVENTO_CERRADA -> if (evento.packageName == appEnPrimerPlano) appEnPrimerPlano = null
            }
        }
        ultimaConsulta = ahora
    }

    companion object {
        private const val TAG = "ControladorPausa"
        private const val CONSULTA_INICIAL_MS = 60 * 60_000L
        private const val SOLAPE_MS = 5_000L
        private const val RECALCULAR_CADA_MS = 60_000L
        private const val MARGEN_TRAS_RECONOCER_MS = 3_000L
        private const val EVENTO_ABIERTA = 1
        private const val EVENTO_CERRADA = 2
    }
}
