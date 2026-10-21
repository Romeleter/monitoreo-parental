package com.proyectointegrador.monitoreo

import android.app.Application
import com.proyectointegrador.monitoreo.configuracion.GestorLimites
import com.proyectointegrador.monitoreo.data.AppDatabase
import com.proyectointegrador.monitoreo.deteccion.ServicioDeteccion
import com.proyectointegrador.monitoreo.deteccion.TrabajoActualizacionUso
import com.proyectointegrador.monitoreo.reportes.GeneradorReporte

/**
 * Punto de arranque de la app. Crea una sola vez la base de datos y las clases
 * de lógica para que todas las pantallas usen las mismas instancias.
 */
class MonitoreoApp : Application() {
    val db: AppDatabase by lazy { AppDatabase.crear(this) }
    val gestorLimites: GestorLimites by lazy { GestorLimites(db.appMonitoreadaDao()) }
    val servicioDeteccion: ServicioDeteccion by lazy { ServicioDeteccion(this, db) }
    val generadorReporte: GeneradorReporte by lazy { GeneradorReporte(db) }

    override fun onCreate() {
        super.onCreate()
        TrabajoActualizacionUso.programar(this)
    }
}
