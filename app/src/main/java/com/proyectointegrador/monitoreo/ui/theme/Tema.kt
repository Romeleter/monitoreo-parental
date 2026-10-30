package com.proyectointegrador.monitoreo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.proyectointegrador.monitoreo.R

private val Colores = lightColorScheme(
    // Azul oscuro, azul claro y turquesa tomados del logo oficial
    primary = Color(0xFF1462A0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E8F6),
    onPrimaryContainer = Color(0xFF0B2E4F),
    secondary = Color(0xFFD97706),
    secondaryContainer = Color(0xFFDCEBF7),
    onSecondaryContainer = Color(0xFF0B2E4F),
    tertiary = Color(0xFF1DB2AE),
    background = Color(0xFFF4F6F9),
    onBackground = Color(0xFF1C2733),
    surface = Color(0xFFF4F6F9),
    onSurface = Color(0xFF1C2733),
    surfaceVariant = Color(0xFFE1E7EE),
    onSurfaceVariant = Color(0xFF4A5562),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAFBFC),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color(0xFFE1E7EE),
    outline = Color(0xFF6E7987),
    outlineVariant = Color(0xFFD9E0E8),
    error = Color(0xFFB91C1C),
    errorContainer = Color(0xFFFDECEC),
    onErrorContainer = Color(0xFF5F1111),
)

/** Colores de la marca que no tienen un rol directo en Material */
object Marca {
    /** Tarjetas blancas con borde suave */
    val tarjeta = Color.White
    val bordeTarjeta = Color(0xFFE0E6EE)

    /** Tarjetas informativas color crema */
    val cremaFondo = Color(0xFFFFF7EC)
    val cremaBorde = Color(0xFFF8DDB6)
    val ambar = Color(0xFFD97706)
    val textoCrema = Color(0xFF5B3A0C)

    /** Fondo celeste del perfil activo y de los paneles de ilustración */
    val celeste = Color(0xFFEAF3FB)
    val textoSecundario = Color(0xFF5B6673)
}

/** Colores de los estados de uso en el resumen */
object ColoresEstado {
    val dentro = Color(0xFF0F766E)
    val cerca = Color(0xFFC26A00)
    val alcanzado = Color(0xFFC0392B)
}

/** Nunito: letra redondeada, usada solo en el aviso que ve el niño */
@OptIn(ExperimentalTextApi::class)
val Nunito = FontFamily(
    Font(R.font.nunito, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.nunito, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.nunito, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800))),
)

@Composable
fun TemaMonitoreo(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colores, content = content)
}
