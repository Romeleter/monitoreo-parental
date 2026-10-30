package com.proyectointegrador.monitoreo.reportes

import com.proyectointegrador.monitoreo.data.AppMonitoreada
import com.proyectointegrador.monitoreo.data.RegistroUso
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt

/** Uso de una app en un día */
data class DiaUso(
    val fecha: LocalDate,
    val minutos: Int,
    /** Límite de ese día, o el actual si el registro es antiguo */
    val limite: Int,
    val avisos: Int,
) {
    /** Se usa >= y no > porque el aviso bloquea la app justo al llegar, casi nunca se pasa */
    val llegoAlLimite: Boolean get() = minutos >= limite

    val esFinDeSemana: Boolean get() = fecha.dayOfWeek == DayOfWeek.SATURDAY || fecha.dayOfWeek == DayOfWeek.SUNDAY
}

/** Historial de 7 días de una app, ya resumido para la pantalla */
data class HistorialApp(val app: AppMonitoreada, val dias: List<DiaUso>) {
    val totalMinutos: Int get() = dias.sumOf { it.minutos }
    val promedioDiario: Int get() = promedio(dias)
    val diasConLimite: Int get() = dias.count { it.llegoAlLimite }

    /** Porcentaje de días de la semana en que se llegó al límite */
    val porcentajeDiasConLimite: Int get() = if (dias.isEmpty()) 0 else (diasConLimite * 100f / dias.size).roundToInt()

    /** Promedio de uso entre semana y en fin de semana */
    val promedioEntreSemana: Int get() = promedio(dias.filterNot { it.esFinDeSemana })
    val promedioFinDeSemana: Int get() = promedio(dias.filter { it.esFinDeSemana })

    val avisosSemana: Int get() = dias.sumOf { it.avisos }

    private fun promedio(lista: List<DiaUso>) =
        if (lista.isEmpty()) 0 else (lista.sumOf { it.minutos }.toFloat() / lista.size).roundToInt()
}

/** Arma el historial. No usa Android, para poder probarlo sin teléfono */
object CalculadoraHistorial {
    const val DIAS = 7

    /** De hace 6 días hasta [hoy], en orden */
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
