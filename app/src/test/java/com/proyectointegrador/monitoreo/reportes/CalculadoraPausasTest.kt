package com.proyectointegrador.monitoreo.reportes

import com.proyectointegrador.monitoreo.data.AvisoPausa
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pruebas de los intentos de reapertura (RF-29). */
class CalculadoraPausasTest {

    private fun aviso(paquete: String) =
        AvisoPausa(paquete = paquete, mensaje = "Límite diario alcanzado", tiempoEsperaSeg = 7, momentoMostrado = 0L)

    @Test
    fun sinAvisosNoHayIntentos() {
        assertEquals(emptyMap<String, Int>(), CalculadoraPausas.intentosDeReapertura(emptyList()))
    }

    @Test
    fun elPrimerAvisoNoCuentaComoIntento() {
        assertEquals(emptyMap<String, Int>(), CalculadoraPausas.intentosDeReapertura(listOf(aviso("tiktok"))))
    }

    @Test
    fun cadaAvisoSiguienteEsUnIntentoPorApp() {
        val avisos = listOf(aviso("tiktok"), aviso("youtube"), aviso("tiktok"), aviso("tiktok"))
        assertEquals(mapOf("tiktok" to 2), CalculadoraPausas.intentosDeReapertura(avisos))
    }
}
