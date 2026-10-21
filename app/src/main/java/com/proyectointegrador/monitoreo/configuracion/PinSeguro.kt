package com.proyectointegrador.monitoreo.configuracion

import java.security.MessageDigest

/** PIN de 4 dígitos que protege el modo padre para que el niño no cambie los límites. */
object PinSeguro {
    const val LONGITUD = 4

    fun esValido(pin: String): Boolean = pin.length == LONGITUD && pin.all { it.isDigit() }

    /** Se guarda el hash SHA-256 del PIN, no el PIN en sí. */
    fun hash(pin: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }

    fun coincide(pin: String, hashGuardado: String): Boolean = hash(pin) == hashGuardado
}
