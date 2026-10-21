package com.proyectointegrador.monitoreo.reportes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.proyectointegrador.monitoreo.MonitoreoApp
import com.proyectointegrador.monitoreo.data.PerfilDao
import com.proyectointegrador.monitoreo.ui.componentes.EncabezadoMarca
import com.proyectointegrador.monitoreo.ui.componentes.TarjetaBlanca
import com.proyectointegrador.monitoreo.ui.formatearMinutos
import com.proyectointegrador.monitoreo.ui.theme.ColoresEstado
import com.proyectointegrador.monitoreo.ui.theme.Marca
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistorialViewModel(generador: GeneradorReporte, perfilDao: PerfilDao) : ViewModel() {
    val historial: StateFlow<List<HistorialApp>?> = generador.historialSemanal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Nombre del niño o niña, para la frase de resumen. */
    val nombre: StateFlow<String?> = perfilDao.observarPerfil()
        .map { it?.nombre }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MonitoreoApp
                HistorialViewModel(app.generadorReporte, app.db.perfilDao())
            }
        }
    }
}

private val DIAS_CORTOS = listOf("lun", "mar", "mié", "jue", "vie", "sáb", "dom")
private val formatoFecha = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es-CO"))

private fun etiquetaCorta(fecha: LocalDate, hoy: LocalDate) =
    if (fecha == hoy) "hoy" else DIAS_CORTOS[fecha.dayOfWeek.value - 1]

private fun etiquetaLarga(fecha: LocalDate, hoy: LocalDate) =
    if (fecha == hoy) "Hoy" else fecha.format(formatoFecha).replaceFirstChar { it.uppercase() }

/**
 * Pantalla de historial semanal (HU-04). Primero la conclusión, luego el gráfico y los datos clave;
 * la lista día por día queda plegada para quien la quiera.
 */
@Composable
fun PantallaHistorial(
    onAtras: () -> Unit,
    vm: HistorialViewModel = viewModel(factory = HistorialViewModel.Factory),
) {
    val historial by vm.historial.collectAsStateWithLifecycle()
    val nombre by vm.nombre.collectAsStateWithLifecycle()
    var paqueteElegido by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Column {
                EncabezadoMarca(onAtras = onAtras)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
    ) { padding ->
        val lista = historial ?: return@Scaffold
        if (lista.isEmpty()) return@Scaffold
        val elegido = lista.firstOrNull { it.app.paquete == paqueteElegido } ?: lista.first()
        val hoy = elegido.dias.last().fecha

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Últimos 7 días", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(lista, key = { it.app.paquete }) { h ->
                        FilterChip(
                            selected = h.app.paquete == elegido.app.paquete,
                            onClick = { paqueteElegido = h.app.paquete },
                            label = { Text(h.app.nombre) },
                        )
                    }
                }
            }
            item { TarjetaSemana(elegido, hoy, nombre ?: "Su hijo o hija") }
            item { SeccionDiaPorDia(elegido, hoy) }
        }
    }
}

/** Resumen (RF-09, RF-25), gráfico (RF-08, RF-30) y datos clave (RF-23, RF-24) en una sola tarjeta. */
@Composable
private fun TarjetaSemana(h: HistorialApp, hoy: LocalDate, nombre: String) {
    var diaElegido by rememberSaveable(h.app.paquete) { mutableStateOf(h.dias.lastIndex) }
    val dia = h.dias[diaElegido.coerceIn(h.dias.indices)]

    TarjetaBlanca {
        FraseResumen(h, nombre)

        GraficoBarras(
            dias = h.dias,
            elegido = diaElegido,
            onElegir = { diaElegido = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .semantics {
                    contentDescription = "Gráfico de barras del uso de ${h.app.nombre} en los últimos 7 días"
                },
        )
        Row(Modifier.fillMaxWidth()) {
            h.dias.forEachIndexed { i, d ->
                Text(
                    etiquetaCorta(d.fecha, hoy),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (i == diaElegido) FontWeight.Bold else FontWeight.Normal,
                    color = if (i == diaElegido) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Detalle del día tocado en el gráfico.
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "${etiquetaLarga(dia.fecha, hoy)}: ${formatearMinutos(dia.minutos)} de ${formatearMinutos(dia.limite)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (dia.llegoAlLimite) {
                Icon(Icons.Default.Block, contentDescription = null, tint = ColoresEstado.alcanzado, modifier = Modifier.size(16.dp))
                Text("Llegó al límite", style = MaterialTheme.typography.labelMedium, color = ColoresEstado.alcanzado)
            }
        }
        Leyenda()

        HorizontalDivider(color = Marca.bordeTarjeta)
        Row(Modifier.fillMaxWidth()) {
            DatoClave("Entre semana", "${formatearMinutos(h.promedioEntreSemana)}/día", Modifier.weight(1f))
            DatoClave("Fin de semana", "${formatearMinutos(h.promedioFinDeSemana)}/día", Modifier.weight(1f))
            DatoClave("Avisos de pausa", "${h.avisosSemana}", Modifier.weight(1f))
        }
    }
}

/** La conclusión de la semana en una frase, con ícono y texto para no depender solo del color. */
@Composable
private fun FraseResumen(h: HistorialApp, nombre: String) {
    val llego = h.diasConLimite > 0
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(
            if (llego) Icons.Default.Block else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (llego) ColoresEstado.alcanzado else ColoresEstado.dentro,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp),
        )
        Text(
            buildAnnotatedString {
                if (llego) {
                    append("Esta semana $nombre llegó al límite de ${h.app.nombre} ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${h.diasConLimite} de ${h.dias.size} días")
                    }
                    append(" (${h.porcentajeDiasConLimite} %).")
                } else {
                    append("Esta semana $nombre se mantuvo ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("dentro del límite") }
                    append(" de ${h.app.nombre} todos los días.")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun DatoClave(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(valor, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = Marca.textoSecundario)
    }
}

@Composable
private fun GraficoBarras(
    dias: List<DiaUso>,
    elegido: Int,
    onElegir: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorBarra = MaterialTheme.colorScheme.primary
    val colorLimite = MaterialTheme.colorScheme.onSurfaceVariant
    val colorEje = MaterialTheme.colorScheme.outlineVariant
    val colorElegido = MaterialTheme.colorScheme.onSurface
    val medidorTexto = rememberTextMeasurer()
    val estiloEtiqueta = MaterialTheme.typography.labelSmall.copy(color = colorLimite)

    // La escala llega un poco por encima del valor más alto o del límite, para que la línea siempre se vea.
    val maximo = (dias.maxOf { maxOf(it.minutos, it.limite) } * 1.15f).coerceAtLeast(1f)

    Canvas(
        modifier.pointerInput(dias.size) {
            detectTapGestures { posicion ->
                val indice = (posicion.x / (size.width / dias.size)).toInt()
                onElegir(indice.coerceIn(dias.indices))
            }
        },
    ) {
        val anchoCelda = size.width / dias.size
        val anchoBarra = anchoCelda * 0.55f
        fun y(minutos: Int) = size.height - (minutos / maximo) * size.height

        // Eje inferior.
        drawLine(colorEje, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())

        dias.forEachIndexed { i, dia ->
            val x = i * anchoCelda + (anchoCelda - anchoBarra) / 2
            val top = y(dia.minutos)
            val color = if (dia.llegoAlLimite) ColoresEstado.alcanzado else colorBarra
            if (dia.minutos > 0) dibujarBarra(color, x, top, anchoBarra, size.height - top)
            if (i == elegido && dia.minutos > 0) {
                // Marco del día elegido (si no hubo uso, basta con la etiqueta en negrita).
                drawRoundRect(
                    color = colorElegido,
                    topLeft = Offset(x - 3.dp.toPx(), minOf(top, size.height - 4.dp.toPx()) - 3.dp.toPx()),
                    size = Size(anchoBarra + 6.dp.toPx(), size.height - minOf(top, size.height - 4.dp.toPx()) + 3.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
            // Línea del límite de ese día (RF-30): punteada sobre cada columna.
            val yLimite = y(dia.limite)
            drawLine(
                color = colorLimite,
                start = Offset(i * anchoCelda + 2.dp.toPx(), yLimite),
                end = Offset((i + 1) * anchoCelda - 2.dp.toPx(), yLimite),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
            )
        }

        // Valor del límite escrito sobre la línea, arriba a la izquierda (es la única escala que se necesita).
        val limiteHoy = dias.last().limite
        val etiqueta = medidorTexto.measure("Límite: ${formatearMinutos(limiteHoy)}", estiloEtiqueta)
        drawText(etiqueta, topLeft = Offset(2.dp.toPx(), (y(dias.first().limite) - etiqueta.size.height - 2.dp.toPx()).coerceAtLeast(0f)))
    }
}

/** Barra con las esquinas de arriba redondeadas y la base recta sobre el eje. */
private fun DrawScope.dibujarBarra(color: Color, x: Float, top: Float, ancho: Float, alto: Float) {
    val radio = minOf(4.dp.toPx(), alto, ancho / 2)
    val path = Path().apply {
        moveTo(x, top + alto)
        lineTo(x, top + radio)
        quadraticTo(x, top, x + radio, top)
        lineTo(x + ancho - radio, top)
        quadraticTo(x + ancho, top, x + ancho, top + radio)
        lineTo(x + ancho, top + alto)
        close()
    }
    drawPath(path, color)
}

/** Solo lo que no se explica solo: el rojo y la línea punteada. */
@Composable
private fun Leyenda() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ColoresEstado.alcanzado),
            )
            Text("Llegó al límite", style = MaterialTheme.typography.labelSmall, color = Marca.textoSecundario)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Canvas(Modifier.size(width = 16.dp, height = 2.dp)) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
                )
            }
            Text("Límite diario", style = MaterialTheme.typography.labelSmall, color = Marca.textoSecundario)
        }
    }
}

/** Los mismos datos del gráfico en forma de lista, plegados para no recargar la pantalla. */
@Composable
private fun SeccionDiaPorDia(h: HistorialApp, hoy: LocalDate) {
    var abierta by rememberSaveable { mutableStateOf(false) }
    TarjetaBlanca(relleno = 0.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { abierta = !abierta }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Ver día por día", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(
                if (abierta) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (abierta) "Ocultar" else "Mostrar",
            )
        }
        AnimatedVisibility(abierta) {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                h.dias.asReversed().forEach { dia ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(etiquetaLarga(dia.fecha, hoy), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (dia.llegoAlLimite) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = "Llegó al límite",
                                tint = ColoresEstado.alcanzado,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(18.dp),
                            )
                        }
                        Text(formatearMinutos(dia.minutos), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
