package com.proyectointegrador.monitoreo.configuracion

import java.security.MessageDigest

/** PIN de 4 dígitos que protege el modo padre */
object PinSeguro {
    const val LONGITUD = 4

    fun esValido(pin: String): Boolean = pin.length == LONGITUD && pin.all { it.isDigit() }

    /** Se guarda el hash SHA-256, no el PIN */
    fun hash(pin: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }

    fun coincide(pin: String, hashGuardado: String): Boolean = hash(pin) == hashGuardado
}
