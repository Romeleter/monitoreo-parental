package com.proyectointegrador.monitoreo.configuracion.limites

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de la edición de límites en pantalla: campo de minutos, botones − y + e interruptor (RF-03, RF-04, RF-22). */
class LimiteEditableTest {

    private val limites = listOf(
        LimiteEditable("tiktok", "TikTok", "10", monitoreoActivo = true),
        LimiteEditable("youtube", "YouTube", "30", monitoreoActivo = true),
    )

    private fun List<LimiteEditable>.de(paquete: String) = first { it.paquete == paquete }

    @Test
    fun elCampoSoloGuardaNumeros() {
        assertEquals("45", limites.conTexto("tiktok", "4a5").de("tiktok").texto)
        assertEquals("1234", limites.conTexto("tiktok", "123456").de("tiktok").texto)
    }

    @Test
    fun cambiarUnaAppNoAfectaALasDemas() {
        val resultado = limites.conTexto("tiktok", "60")
        assertEquals("60", resultado.de("tiktok").texto)
        assertEquals("30", resultado.de("youtube").texto)
    }

    @Test
    fun losBotonesSumanYRestanDeCincoEnCinco() {
        assertEquals("15", limites.sumando("tiktok", PASO_MINUTOS).de("tiktok").texto)
        assertEquals("5", limites.sumando("tiktok", -PASO_MINUTOS).de("tiktok").texto)
    }

    @Test
    fun restarNoBajaDeUnMinuto() {
        val resultado = limites.conTexto("tiktok", "3").sumando("tiktok", -PASO_MINUTOS)
        assertEquals("1", resultado.de("tiktok").texto)
    }

    @Test
    fun sumarNoPasaDeTrescientosMinutos() {
        val resultado = limites.conTexto("tiktok", "298").sumando("tiktok", PASO_MINUTOS)
        assertEquals("300", resultado.de("tiktok").texto)
    }

    @Test
    fun sumarDesdeUnCampoVacioEmpiezaEnCinco() {
        val resultado = limites.conTexto("tiktok", "").sumando("tiktok", PASO_MINUTOS)
        assertEquals("5", resultado.de("tiktok").texto)
    }

    @Test
    fun pausarElMonitoreoConservaElLimite() {
        val pausado = limites.conMonitoreo("tiktok", activo = false).de("tiktok")
        assertFalse(pausado.monitoreoActivo)
        assertEquals(10, pausado.valor)
    }

    @Test
    fun unValorFueraDeRangoNoEsValido() {
        assertNull(limites.conTexto("tiktok", "500").de("tiktok").valor)
        assertNull(limites.conTexto("tiktok", "0").de("tiktok").valor)
    }

    @Test
    fun soloSePuedeGuardarSiTodosLosLimitesSonValidos() {
        assertTrue(limites.todosValidos())
        assertFalse(limites.conTexto("youtube", "").todosValidos())
        assertFalse(limites.conTexto("youtube", "301").todosValidos())
    }
}
