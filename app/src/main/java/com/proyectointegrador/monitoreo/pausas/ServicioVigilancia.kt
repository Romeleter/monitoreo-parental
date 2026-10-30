package com.proyectointegrador.monitoreo.pausas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.proyectointegrador.monitoreo.MonitoreoApp
import com.proyectointegrador.monitoreo.R
import com.proyectointegrador.monitoreo.deteccion.PermisoUso
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Mantiene viva la vigilancia aunque la app esté cerrada. Android exige una notificación fija */
class ServicioVigilancia : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var ciclo: Job? = null
    private lateinit var controlador: ControladorPausa

    override fun onCreate() {
        super.onCreate()
        val app = application as MonitoreoApp
        controlador = ControladorPausa(this, app.db, app.servicioDeteccion, scope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        iniciarEnPrimerPlano()
        if (ciclo == null) {
            ciclo = scope.launch {
                while (isActive) {
                    try {
                        controlador.revisar()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(TAG, "Error revisando el uso", e)
                    }
                    delay(INTERVALO_MS)
                }
            }
        }
        // Si Android detiene el servicio por falta de memoria, lo vuelve a iniciar
        return START_STICKY
    }

    override fun onDestroy() {
        controlador.liberar()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun iniciarEnPrimerPlano() {
        val notificaciones = getSystemService(NotificationManager::class.java)
        notificaciones.createNotificationChannel(
            NotificationChannel(CANAL, "Monitoreo activo", NotificationManager.IMPORTANCE_LOW),
        )
        val notificacion = NotificationCompat.Builder(this, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Monitoreo de videos cortos activo")
            .setContentText("Se mostrará una pausa al llegar al límite.")
            .setOngoing(true)
            .build()
        val tipo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, ID_NOTIFICACION, notificacion, tipo)
    }

    companion object {
        private const val TAG = "ServicioVigilancia"
        private const val CANAL = "monitoreo_activo"
        private const val ID_NOTIFICACION = 1

        /** Cada cuánto se revisa qué app está en pantalla */
        const val INTERVALO_MS = 2_000L

        /** Inicia el servicio si ya están los permisos. Si ya está corriendo no pasa nada */
        fun iniciar(context: Context) {
            if (!PermisoUso.estaConcedido(context) || !PermisoSuperposicion.estaConcedido(context)) return
            try {
                ContextCompat.startForegroundService(context, Intent(context, ServicioVigilancia::class.java))
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo iniciar el servicio", e)
            }
        }
    }
}
