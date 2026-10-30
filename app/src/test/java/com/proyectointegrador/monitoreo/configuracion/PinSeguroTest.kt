package com.proyectointegrador.monitoreo.configuracion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del PIN */
class PinSeguroTest {

    @Test
    fun aceptaPinDeCuatroNumeros() {
        assertTrue(PinSeguro.esValido("1234"))
        assertTrue(PinSeguro.esValido("0000"))
    }

    @Test
    fun rechazaPinConOtraLongitud() {
        assertFalse(PinSeguro.esValido(""))
        assertFalse(PinSeguro.esValido("123"))
        assertFalse(PinSeguro.esValido("12345"))
    }

    @Test
    fun rechazaPinConLetrasOEspacios() {
        assertFalse(PinSeguro.esValido("12a4"))
        assertFalse(PinSeguro.esValido("12 4"))
    }

    @Test
    fun noGuardaElPinEnTextoPlano() {
        val hash = PinSeguro.hash("1234")
        assertNotEquals("1234", hash)
        assertFalse(hash.contains("1234"))
        // SHA-256 en hexadecimal: 64 caracteres
        assertEquals(64, hash.length)
    }

    @Test
    fun elMismoPinSiempreDaElMismoHash() {
        assertEquals(PinSeguro.hash("2580"), PinSeguro.hash("2580"))
        assertNotEquals(PinSeguro.hash("2580"), PinSeguro.hash("2581"))
    }

    @Test
    fun soloCoincideElPinCorrecto() {
        val guardado = PinSeguro.hash("1234")
        assertTrue(PinSeguro.coincide("1234", guardado))
        assertFalse(PinSeguro.coincide("4321", guardado))
        assertFalse(PinSeguro.coincide("", guardado))
    }
}
