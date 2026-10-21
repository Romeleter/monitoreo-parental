package com.proyectointegrador.monitoreo.deteccion

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.proyectointegrador.monitoreo.MonitoreoApp
import java.util.concurrent.TimeUnit

/**
 * Tarea en segundo plano que Android ejecuta cada 15 minutos (RF-02, RF-17),
 * aunque la app esté cerrada o el teléfono se haya reiniciado.
 */
class TrabajoActualizacionUso(
    context: Context,
    parametros: WorkerParameters,
) : CoroutineWorker(context, parametros) {

    override suspend fun doWork(): Result {
        val app = applicationContext as MonitoreoApp
        app.servicioDeteccion.actualizarUso()
        return Result.success()
    }

    companion object {
        private const val NOMBRE = "actualizacion_uso"
        const val INTERVALO_MINUTOS = 15L

        /** Programa la tarea. Si ya estaba programada no la duplica. */
        fun programar(context: Context) {
            val solicitud = PeriodicWorkRequestBuilder<TrabajoActualizacionUso>(
                INTERVALO_MINUTOS,
                TimeUnit.MINUTES,
            ).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(NOMBRE, ExistingPeriodicWorkPolicy.KEEP, solicitud)
        }
    }
}
