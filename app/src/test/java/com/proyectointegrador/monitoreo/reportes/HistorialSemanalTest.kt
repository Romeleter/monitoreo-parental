package com.proyectointegrador.monitoreo.reportes

import com.proyectointegrador.monitoreo.data.AppMonitoreada
import com.proyectointegrador.monitoreo.data.RegistroUso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Pruebas del historial semanal (RF-08, RF-09, RF-23, RF-25). */
class HistorialSemanalTest {

    // Domingo 25 de octubre de 2026: la ventana va del lunes 19 al domingo 25.
    private val hoy = LocalDate.of(2026, 10, 25)
    private val tiktok = AppMonitoreada("tiktok", perfilId = 1, nombre = "TikTok", limiteDiarioMin = 30)

    private fun registro(dia: Int, minutos: Int, limite: Int? = null, avisos: Int = 0) =
        RegistroUso(
            paquete = "tiktok",
            fecha = LocalDate.of(2026, 10, dia).toString(),
            minutosUso = minutos,
            activacionesAviso = avisos,
            limiteMin = limite,
        )

    private fun historial(vararg registros: RegistroUso) =
        CalculadoraHistorial.armar(listOf(tiktok), registros.toList(), hoy).single()

    @Test
    fun siempreTieneSieteDiasEnOrdenYLosDiasSinDatosValenCero() {
        val h = historial(registro(22, 10))
        assertEquals(7, h.dias.size)
        assertEquals(LocalDate.of(2026, 10, 19), h.dias.first().fecha)
        assertEquals(hoy, h.dias.last().fecha)
        assertEquals(listOf(0, 0, 0, 10, 0, 0, 0), h.dias.map { it.minutos })
    }

    @Test
    fun marcaLosDiasQueLlegaronAlLimite() {
        val h = historial(registro(19, 30), registro(20, 29), registro(21, 45))
        assertEquals(listOf(true, false, true), h.dias.take(3).map { it.llegoAlLimite })
        assertEquals(2, h.diasConLimite)
        assertEquals(29, h.porcentajeDiasConLimite) // 2 de 7 días
    }

    @Test
    fun usaElLimiteQueTeniaEseDia() {
        // Ese día el límite era 60: 45 minutos no llegan al límite aunque hoy sea 30.
        val h = historial(registro(21, 45, limite = 60))
        assertFalse(h.dias[2].llegoAlLimite)
        assertEquals(60, h.dias[2].limite)
        assertEquals(30, h.dias[3].limite)
    }

    @Test
    fun separaEntreSemanaYFinDeSemana() {
        // Lunes a viernes 10 min cada día; sábado 40 y domingo 60.
        val h = historial(
            registro(19, 10), registro(20, 10), registro(21, 10), registro(22, 10), registro(23, 10),
            registro(24, 40), registro(25, 60),
        )
        assertEquals(10, h.promedioEntreSemana)
        assertEquals(50, h.promedioFinDeSemana)
        assertEquals(150, h.totalMinutos)
        assertEquals(21, h.promedioDiario)
        assertTrue(h.dias.last().esFinDeSemana)
    }

    @Test
    fun sumaLosAvisosDeLaSemana() {
        val h = historial(registro(19, 30, avisos = 2), registro(25, 30, avisos = 3))
        assertEquals(5, h.avisosSemana)
    }
}
