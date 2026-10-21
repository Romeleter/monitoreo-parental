package com.proyectointegrador.monitoreo.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** RF-02: muestra minutos en formato de horas y minutos, p. ej. "1 h 05 min". */
fun formatearMinutos(minutos: Int): String {
    if (minutos < 60) return "$minutos min"
    val horas = minutos / 60
    val resto = minutos % 60
    return if (resto == 0) "$horas h" else "$horas h %02d min".format(resto)
}

private val formatoHora = DateTimeFormatter.ofPattern("h:mm a", Locale.forLanguageTag("es-CO"))

fun formatearHora(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(formatoHora)
