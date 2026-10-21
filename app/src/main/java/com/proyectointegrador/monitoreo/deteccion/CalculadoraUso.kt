package com.proyectointegrador.monitoreo.deteccion

/** Un evento simplificado de UsageStatsManager: una app pasó a primer plano o salió de él. */
data class EventoUso(val paquete: String, val momento: Long, val tipo: Tipo) {
    enum class Tipo { ABIERTA, CERRADA }
}

/** Tiempo en que el aviso de pausa estuvo encima de una app: no cuenta como uso. */
data class IntervaloPausa(val paquete: String, val desde: Long, val hasta: Long)

/**
 * Convierte la lista de eventos de Android en milisegundos de uso por app (RF-01).
 * Es una función pura (no usa Android) para poder probarla con pruebas unitarias.
 */
object CalculadoraUso {

    /**
     * @param eventos eventos ordenados por tiempo, entre [inicio] y [fin].
     * @param paquetes solo se cuentan estas apps; las demás se descartan (RF-18).
     * @param pausas intervalos con el aviso encima, que se restan del uso.
     */
    fun milisegundosPorApp(
        eventos: List<EventoUso>,
        paquetes: Set<String>,
        inicio: Long,
        fin: Long,
        pausas: List<IntervaloPausa> = emptyList(),
    ): Map<String, Long> {
        val abiertaDesde = mutableMapOf<String, Long>()
        val vistas = mutableSetOf<String>()
        val total = mutableMapOf<String, Long>()

        fun sumar(paquete: String, desde: Long, hasta: Long) {
            if (hasta <= desde) return
            val tiempoConAviso = pausas
                .filter { it.paquete == paquete }
                .sumOf { (minOf(hasta, it.hasta) - maxOf(desde, it.desde)).coerceAtLeast(0L) }
            val uso = (hasta - desde - tiempoConAviso).coerceAtLeast(0L)
            total[paquete] = (total[paquete] ?: 0L) + uso
        }

        for (evento in eventos) {
            if (evento.paquete !in paquetes) continue
            when (evento.tipo) {
                // Si llegan dos "abierta" seguidas (cambio de pantalla dentro de la app) se conserva la primera.
                EventoUso.Tipo.ABIERTA -> abiertaDesde.putIfAbsent(evento.paquete, evento.momento)
                EventoUso.Tipo.CERRADA -> {
                    val desde = abiertaDesde.remove(evento.paquete)
                    when {
                        desde != null -> sumar(evento.paquete, desde, evento.momento)
                        // Se cerró sin haberse abierto hoy: estaba abierta desde antes de la medianoche.
                        evento.paquete !in vistas -> sumar(evento.paquete, inicio, evento.momento)
                    }
                }
            }
            vistas += evento.paquete
        }
        // Apps que siguen abiertas en este momento.
        abiertaDesde.forEach { (paquete, desde) -> sumar(paquete, desde, fin) }
        return total
    }

    fun aMinutos(milisegundos: Long): Int = (milisegundos / 60_000L).toInt()
}
