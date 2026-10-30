package com.proyectointegrador.monitoreo.configuracion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas de la validación de límites */
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
}
