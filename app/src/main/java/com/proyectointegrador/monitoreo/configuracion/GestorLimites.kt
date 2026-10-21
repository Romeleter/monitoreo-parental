package com.proyectointegrador.monitoreo.configuracion

import com.proyectointegrador.monitoreo.data.AppMonitoreada
import com.proyectointegrador.monitoreo.data.AppMonitoreadaDao
import kotlinx.coroutines.flow.Flow

/** Valida y guarda los límites diarios de cada app (HU-02 / CU-03). */
class GestorLimites(private val dao: AppMonitoreadaDao) {

    fun observarApps(): Flow<List<AppMonitoreada>> = dao.observarTodas()

    /** Guarda el límite de forma persistente (RF-05). [minutos] ya debe estar validado. */
    suspend fun guardarLimite(paquete: String, minutos: Int, monitoreoActivo: Boolean) {
        require(minutos in MINIMO..MAXIMO) { "Límite fuera de rango: $minutos" }
        dao.actualizarConfiguracion(paquete, minutos, monitoreoActivo)
    }

    /** HU-03: activa o quita el bloqueo manual inmediato de una app (RF-06, RF-20). */
    suspend fun cambiarBloqueoManual(paquete: String, bloqueada: Boolean) {
        dao.actualizarBloqueo(paquete, bloqueada)
    }

    companion object {
        const val MINIMO = 1
        const val MAXIMO = 300

        /** RF-04: devuelve los minutos si el texto es numérico y está entre 1 y 300; si no, null. */
        fun validar(texto: String): Int? {
            val minutos = texto.trim().toIntOrNull() ?: return null
            return minutos.takeIf { it in MINIMO..MAXIMO }
        }
    }
}
