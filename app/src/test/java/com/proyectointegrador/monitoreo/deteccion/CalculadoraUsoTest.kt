package com.proyectointegrador.monitoreo.deteccion

import com.proyectointegrador.monitoreo.deteccion.EventoUso.Tipo.ABIERTA
import com.proyectointegrador.monitoreo.deteccion.EventoUso.Tipo.CERRADA
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** Pruebas del cálculo de tiempo de uso (RF-01, RF-18). Los tiempos están en minutos para leerlos fácil. */
class CalculadoraUsoTest {

    private val tiktok = "com.zhiliaoapp.musically"
    private val youtube = "com.google.android.youtube"
    private val otraApp = "com.whatsapp"
    private val monitoreadas = setOf(tiktok, youtube)

    private fun min(m: Int) = m * 60_000L
    private fun evento(paquete: String, minuto: Int, tipo: EventoUso.Tipo) = EventoUso(paquete, min(minuto), tipo)

    private fun calcular(eventos: List<EventoUso>, fin: Int = 1_440) =
        CalculadoraUso.milisegundosPorApp(eventos, monitoreadas, inicio = 0L, fin = min(fin))
            .mapValues { CalculadoraUso.aMinutos(it.value) }

    @Test
    fun sumaVariasSesionesDeLaMismaApp() {
        val uso = calcular(
            listOf(
                evento(tiktok, 10, ABIERTA), evento(tiktok, 25, CERRADA),
                evento(tiktok, 100, ABIERTA), evento(tiktok, 130, CERRADA),
            ),
        )
        assertEquals(45, uso[tiktok])
    }

    @Test
    fun separaElTiempoPorApp() {
        val uso = calcular(
            listOf(
                evento(tiktok, 0, ABIERTA), evento(tiktok, 20, CERRADA),
                evento(youtube, 20, ABIERTA), evento(youtube, 50, CERRADA),
            ),
        )
        assertEquals(20, uso[tiktok])
        assertEquals(30, uso[youtube])
    }

    @Test
    fun descartaAppsNoMonitoreadas() {
        val uso = calcular(listOf(evento(otraApp, 0, ABIERTA), evento(otraApp, 60, CERRADA)))
        assertFalse(uso.containsKey(otraApp))
    }

    @Test
    fun cuentaHastaAhoraSiLaAppSigueAbierta() {
        val uso = calcular(listOf(evento(youtube, 50, ABIERTA)), fin = 80)
        assertEquals(30, uso[youtube])
    }

    @Test
    fun cuentaDesdeMedianocheSiLaAppVeniaAbierta() {
        // El niño abrió TikTok a las 11:50 p. m. y lo cerró a las 12:15 a. m.: hoy cuentan 15 minutos.
        val uso = calcular(listOf(evento(tiktok, 15, CERRADA)))
        assertEquals(15, uso[tiktok])
    }

    @Test
    fun noCuentaElTiempoConElAvisoEncima() {
        // YouTube abierto del minuto 0 al 30, pero el aviso estuvo encima del minuto 10 al 24.
        val uso = CalculadoraUso.milisegundosPorApp(
            listOf(evento(youtube, 0, ABIERTA), evento(youtube, 30, CERRADA)),
            monitoreadas,
            inicio = 0L,
            fin = min(1_440),
            pausas = listOf(IntervaloPausa(youtube, min(10), min(24))),
        ).mapValues { CalculadoraUso.aMinutos(it.value) }
        assertEquals(16, uso[youtube])
    }

    @Test
    fun elAvisoDeOtraAppNoDescuentaTiempo() {
        val uso = CalculadoraUso.milisegundosPorApp(
            listOf(evento(tiktok, 0, ABIERTA), evento(tiktok, 20, CERRADA)),
            monitoreadas,
            inicio = 0L,
            fin = min(1_440),
            pausas = listOf(IntervaloPausa(youtube, min(5), min(15))),
        ).mapValues { CalculadoraUso.aMinutos(it.value) }
        assertEquals(20, uso[tiktok])
    }

    @Test
    fun cambiosDePantallaDentroDeLaAppNoDuplicanTiempo() {
        val uso = calcular(
            listOf(
                evento(tiktok, 0, ABIERTA),
                evento(tiktok, 5, ABIERTA),
                evento(tiktok, 10, CERRADA),
            ),
        )
        assertEquals(10, uso[tiktok])
    }
}
