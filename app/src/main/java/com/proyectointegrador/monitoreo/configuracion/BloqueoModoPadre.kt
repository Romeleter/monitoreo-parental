package com.proyectointegrador.monitoreo.configuracion

/**
 * El modo padre se bloquea cuando la app pasa a segundo plano, para que el niño no entre
 * a cambiar límites. La excepción es cuando el padre abre los ajustes de Android desde la app.
 */
object BloqueoModoPadre {
    @Volatile
    private var salidaPermitida = false

    /** Llamar justo antes de abrir los ajustes de Android */
    fun permitirSalidaUnaVez() {
        salidaPermitida = true
    }

    /** Devuelve si la salida estaba permitida y la gasta */
    fun consumirSalidaPermitida(): Boolean {
        val permitida = salidaPermitida
        salidaPermitida = false
        return permitida
    }
}
