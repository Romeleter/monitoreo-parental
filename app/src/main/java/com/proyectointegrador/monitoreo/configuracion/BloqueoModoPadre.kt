package com.proyectointegrador.monitoreo.configuracion

/**
 * El modo padre se bloquea cada vez que la app pasa a segundo plano, para que el niño
 * no pueda entrar a cambiar límites si el padre solo salió de la app sin cerrarla.
 *
 * Excepción: cuando el propio padre abre los ajustes de Android desde la app (para conceder
 * un permiso), esa salida no bloquea, para no pedirle el PIN otra vez al volver.
 */
object BloqueoModoPadre {
    @Volatile
    private var salidaPermitida = false

    /** Llamar justo antes de abrir los ajustes de Android desde el modo padre. */
    fun permitirSalidaUnaVez() {
        salidaPermitida = true
    }

    /** Devuelve true si la salida actual fue permitida, y la consume. */
    fun consumirSalidaPermitida(): Boolean {
        val permitida = salidaPermitida
        salidaPermitida = false
        return permitida
    }
}
