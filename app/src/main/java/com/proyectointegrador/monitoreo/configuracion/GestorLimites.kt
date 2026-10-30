package com.proyectointegrador.monitoreo.configuracion

import com.proyectointegrador.monitoreo.data.AppMonitoreada
import com.proyectointegrador.monitoreo.data.AppMonitoreadaDao
import kotlinx.coroutines.flow.Flow

/** Valida y guarda los límites diarios */
class GestorLimites(private val dao: AppMonitoreadaDao) {

    fun observarApps(): Flow<List<AppMonitoreada>> = dao.observarTodas()

    /** [minutos] ya debe venir validado */
    suspend fun guardarLimite(paquete: String, minutos: Int, monitoreoActivo: Boolean) {
        require(minutos in MINIMO..MAXIMO) { "Límite fuera de rango: $minutos" }
        dao.actualizarConfiguracion(paquete, minutos, monitoreoActivo)
    }

    /** Activa o quita el bloqueo manual de una app */
    suspend fun cambiarBloqueoManual(paquete: String, bloqueada: Boolean) {
        dao.actualizarBloqueo(paquete, bloqueada)
    }

    companion object {
        const val MINIMO = 1
        const val MAXIMO = 300

        /** Devuelve los minutos si el texto es un número entre 1 y 300; si no, null */
        fun validar(texto: String): Int? {
            val minutos = texto.trim().toIntOrNull() ?: return null
            return minutos.takeIf { it in MINIMO..MAXIMO }
        }
    }
}
