package com.proyectointegrador.monitoreo.configuracion.limites

import com.proyectointegrador.monitoreo.configuracion.GestorLimites

/** Una fila de límites. El texto se guarda tal cual lo escribe el padre */
data class LimiteEditable(
    val paquete: String,
    val nombre: String,
    val texto: String,
    val monitoreoActivo: Boolean,
) {
    /** Minutos válidos, o null si está vacío o fuera de rango */
    val valor: Int? get() = GestorLimites.validar(texto)
}

/** Paso de los botones − y + */
const val PASO_MINUTOS = 5

fun List<LimiteEditable>.conTexto(paquete: String, texto: String) =
    modificar(paquete) { it.copy(texto = texto.filter { c -> c.isDigit() }.take(4)) }

/** Suman o restan [delta] minutos sin salirse del rango */
fun List<LimiteEditable>.sumando(paquete: String, delta: Int) = modificar(paquete) {
    val actual = it.texto.toIntOrNull() ?: 0
    val nuevo = (actual + delta).coerceIn(GestorLimites.MINIMO, GestorLimites.MAXIMO)
    it.copy(texto = nuevo.toString())
}

/** Pausa el monitoreo de una app sin perder su límite */
fun List<LimiteEditable>.conMonitoreo(paquete: String, activo: Boolean) =
    modificar(paquete) { it.copy(monitoreoActivo = activo) }

fun List<LimiteEditable>.todosValidos(): Boolean = all { it.valor != null }

private fun List<LimiteEditable>.modificar(paquete: String, cambio: (LimiteEditable) -> LimiteEditable) =
    map { if (it.paquete == paquete) cambio(it) else it }
