package com.proyectointegrador.monitoreo.reportes

import com.proyectointegrador.monitoreo.data.AppMonitoreada
import com.proyectointegrador.monitoreo.data.RegistroUso
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt

/** Uso de una app en un día del historial (RF-08). */
data class DiaUso(
    val fecha: LocalDate,
    val minutos: Int,
    /** Límite que tenía ese día (o el actual si el registro es antiguo). */
    val limite: Int,
    val avisos: Int,
) {
    /**
     * RF-09: días en que se llegó al límite. Se usa "llegó" (>=) y no "superó" (>) porque
     * el aviso de pausa bloquea la app justo al llegar, así que casi nunca se pasa de él.
     */
    val llegoAlLimite: Boolean get() = minutos >= limite

    val esFinDeSemana: Boolean get() = fecha.dayOfWeek == DayOfWeek.SATURDAY || fecha.dayOfWeek == DayOfWeek.SUNDAY
}

/** Historial de los últimos 7 días de una app, con los datos ya resumidos para la pantalla. */
data class HistorialApp(val app: AppMonitoreada, val dias: List<DiaUso>) {
    val totalMinutos: Int get() = dias.sumOf { it.minutos }
    val promedioDiario: Int get() = promedio(dias)
    val diasConLimite: Int get() = dias.count { it.llegoAlLimite }

    /** RF-25: porcentaje de días de la semana en que se llegó al límite. */
    val porcentajeDiasConLimite: Int get() = if (dias.isEmpty()) 0 else (diasConLimite * 100f / dias.size).roundToInt()

    /** RF-23: promedio de uso entre semana y en fin de semana. */
    val promedioEntreSemana: Int get() = promedio(dias.filterNot { it.esFinDeSemana })
    val promedioFinDeSemana: Int get() = promedio(dias.filter { it.esFinDeSemana })

    val avisosSemana: Int get() = dias.sumOf { it.avisos }

    private fun promedio(lista: List<DiaUso>) =
        if (lista.isEmpty()) 0 else (lista.sumOf { it.minutos }.toFloat() / lista.size).roundToInt()
}

/** Arma el historial de 7 días. Función pura para poder probarla sin Android. */
object CalculadoraHistorial {
    const val DIAS = 7

    /** Fechas del historial: de hace 6 días hasta [hoy], en orden. */
    fun fechas(hoy: LocalDate): List<LocalDate> = (DIAS - 1 downTo 0).map { hoy.minusDays(it.toLong()) }

    fun armar(apps: List<AppMonitoreada>, registros: List<RegistroUso>, hoy: LocalDate): List<HistorialApp> {
        val porAppYFecha = registros.associateBy { it.paquete to it.fecha }
        return apps.map { app ->
            val dias = fechas(hoy).map { fecha ->
                val registro = porAppYFecha[app.paquete to fecha.toString()]
                DiaUso(
                    fecha = fecha,
                    minutos = registro?.minutosUso ?: 0,
                    limite = registro?.limiteMin ?: app.limiteDiarioMin,
                    avisos = registro?.activacionesAviso ?: 0,
                )
            }
            HistorialApp(app, dias)
        }
    }
}
