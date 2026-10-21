package com.proyectointegrador.monitoreo.configuracion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de la validación de límites (RF-04) y del PIN. */
class GestorLimitesTest {

    @Test
    fun aceptaValoresDentroDelRango() {
        assertEquals(1, GestorLimites.validar("1"))
        assertEquals(45, GestorLimites.validar(" 45 "))
        assertEquals(300, GestorLimites.validar("300"))
    }

    @Test
    fun rechazaValoresFueraDeRango() {
        assertNull(GestorLimites.validar("0"))
        assertNull(GestorLimites.validar("301"))
        assertNull(GestorLimites.validar("500"))
    }

    @Test
    fun rechazaValoresNoNumericos() {
        assertNull(GestorLimites.validar(""))
        assertNull(GestorLimites.validar("abc"))
        assertNull(GestorLimites.validar("4.5"))
    }

    @Test
    fun pinDebeTenerCuatroDigitos() {
        assertTrue(PinSeguro.esValido("1234"))
        assertFalse(PinSeguro.esValido("123"))
        assertFalse(PinSeguro.esValido("12a4"))
        assertTrue(PinSeguro.coincide("1234", PinSeguro.hash("1234")))
        assertFalse(PinSeguro.coincide("4321", PinSeguro.hash("1234")))
    }
}
