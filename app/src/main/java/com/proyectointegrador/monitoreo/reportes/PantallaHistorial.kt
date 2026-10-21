package com.proyectointegrador.monitoreo.reportes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.proyectointegrador.monitoreo.MonitoreoApp
import com.proyectointegrador.monitoreo.ui.formatearMinutos
import com.proyectointegrador.monitoreo.ui.theme.ColoresEstado
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistorialViewModel(generador: GeneradorReporte) : ViewModel() {
    val historial: StateFlow<List<HistorialApp>?> = generador.historialSemanal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MonitoreoApp
                HistorialViewModel(app.generadorReporte)
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

/** Pantalla de historial semanal (HU-04: RF-08, RF-09, RF-23, RF-25, RF-27, RF-30). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorial(
    onAtras: () -> Unit,
    vm: HistorialViewModel = viewModel(factory = HistorialViewModel.Factory),
) {
    val historial by vm.historial.collectAsStateWithLifecycle()
    var paqueteElegido by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Últimos 7 días") },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        val lista = historial ?: return@Scaffold
        if (lista.isEmpty()) return@Scaffold
        val elegido = lista.firstOrNull { it.app.paquete == paqueteElegido } ?: lista.first()
        val hoy = elegido.dias.last().fecha

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
            item { TarjetaGrafico(elegido, hoy) }
            item { TarjetaResumenSemana(elegido) }
            item { TarjetaTablaDias(elegido, hoy) }
        }
    }
}

/** RF-08 + RF-09: barras por día, línea del límite y días que llegaron al límite en rojo. */
@Composable
private fun TarjetaGrafico(historial: HistorialApp, hoy: LocalDate) {
    var diaElegido by rememberSaveable(historial.app.paquete) { mutableStateOf(historial.dias.lastIndex) }
    val dia = historial.dias[diaElegido.coerceIn(historial.dias.indices)]

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tiempo en ${historial.app.nombre} por día", style = MaterialTheme.typography.titleMedium)

            GraficoBarras(
                dias = historial.dias,
                elegido = diaElegido,
                onElegir = { diaElegido = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .semantics {
                        contentDescription = "Gráfico de barras del uso de ${historial.app.nombre} en los últimos 7 días"
                    },
            )
            Row(Modifier.fillMaxWidth()) {
                historial.dias.forEachIndexed { i, d ->
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

            // Detalle del día tocado (equivalente táctil al "pasar el mouse" de un gráfico web).
            Text(
                "${etiquetaLarga(dia.fecha, hoy)}: ${formatearMinutos(dia.minutos)} de ${formatearMinutos(dia.limite)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (dia.llegoAlLimite) EstadoLimite(llego = true)

            Leyenda()
            Text(
                "Toca una barra para ver el detalle de ese día.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
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

@Composable
private fun Leyenda() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        ItemLeyenda(MaterialTheme.colorScheme.primary, "Tiempo usado")
        ItemLeyenda(ColoresEstado.alcanzado, "Llegó al límite")
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
            Text("Límite", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ItemLeyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Text(texto, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EstadoLimite(llego: Boolean) {
    val (icono, texto, color) = if (llego) {
        Triple(Icons.Default.Block, "Llegó al límite", ColoresEstado.alcanzado)
    } else {
        Triple(Icons.Default.CheckCircle, "Dentro del límite", ColoresEstado.dentro)
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(texto, color = color, style = MaterialTheme.typography.bodyMedium)
    }
}

/** RF-30, RF-25, RF-23 y RF-27: resumen de la semana en frases simples. */
@Composable
private fun TarjetaResumenSemana(h: HistorialApp) {
    val limiteActual = h.app.limiteDiarioMin
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Resumen de la semana", style = MaterialTheme.typography.titleMedium)

            // RF-30: límite frente al uso real, con dos barras comparables.
            BarraComparacion("Límite por día", limiteActual, maxOf(limiteActual, h.promedioDiario), Color.Gray)
            BarraComparacion(
                "Uso promedio por día",
                h.promedioDiario,
                maxOf(limiteActual, h.promedioDiario),
                if (h.promedioDiario >= limiteActual) ColoresEstado.alcanzado else MaterialTheme.colorScheme.primary,
            )

            HorizontalDivider()
            val estilo = MaterialTheme.typography.bodyMedium
            // RF-25
            Text("Llegó al límite ${h.diasConLimite} de ${h.dias.size} días (${h.porcentajeDiasConLimite} %).", style = estilo)
            // RF-23
            Text("Entre semana: ${formatearMinutos(h.promedioEntreSemana)} por día.", style = estilo)
            Text("Fin de semana: ${formatearMinutos(h.promedioFinDeSemana)} por día.", style = estilo)
            Text("Total de la semana: ${formatearMinutos(h.totalMinutos)}.", style = estilo)
            Text("Avisos de pausa: ${h.avisosSemana}.", style = estilo)
        }
    }
}

@Composable
private fun BarraComparacion(titulo: String, minutos: Int, maximo: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row {
            Text(titulo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(formatearMinutos(minutos), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            val fraccion = if (maximo == 0) 0f else minutos.toFloat() / maximo
            if (fraccion > 0f) {
                Box(
                    Modifier
                        .fillMaxWidth(fraccion)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color),
                )
            }
        }
    }
}

/** Los mismos datos del gráfico en forma de lista (para quien prefiera leerlos así). */
@Composable
private fun TarjetaTablaDias(h: HistorialApp, hoy: LocalDate) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Día por día", style = MaterialTheme.typography.titleMedium)
            h.dias.asReversed().forEach { dia ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        etiquetaLarga(dia.fecha, hoy),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
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
                    Text(
                        formatearMinutos(dia.minutos),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
