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
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCEFE9),
    onPrimaryContainer = Color(0xFF00332D),
    secondary = Color(0xFFD97706),
    secondaryContainer = Color(0xFFD7EFEA),
    onSecondaryContainer = Color(0xFF00332D),
    background = Color(0xFFF4F6F6),
    onBackground = Color(0xFF1E2A28),
    surface = Color(0xFFF4F6F6),
    onSurface = Color(0xFF1E2A28),
    surfaceVariant = Color(0xFFE2E8E6),
    onSurfaceVariant = Color(0xFF4B5755),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAFBFB),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color(0xFFE2E8E6),
    outline = Color(0xFF6F7B79),
    outlineVariant = Color(0xFFDCE3E1),
    error = Color(0xFFB91C1C),
    errorContainer = Color(0xFFFDECEC),
    onErrorContainer = Color(0xFF5F1111),
)

/** Colores de la marca que no tienen un rol directo en Material. */
object Marca {
    /** Tarjetas blancas con borde suave, como en los mockups. */
    val tarjeta = Color.White
    val bordeTarjeta = Color(0xFFE1E7E5)

    /** Tarjetas informativas color crema (avisos de permisos, privacidad, actualización). */
    val cremaFondo = Color(0xFFFFF7EC)
    val cremaBorde = Color(0xFFF8DDB6)
    val ambar = Color(0xFFD97706)
    val textoCrema = Color(0xFF5B3A0C)

    /** Fondo menta del perfil activo y de los paneles de ilustración. */
    val menta = Color(0xFFE6F6F3)
    val textoSecundario = Color(0xFF5F6B69)
}

/** Colores de los estados de uso en el resumen del día (pantalla 5). */
object ColoresEstado {
    val dentro = Color(0xFF0F766E)
    val cerca = Color(0xFFC26A00)
    val alcanzado = Color(0xFFC0392B)
}

/** Nunito: letra redondeada y amable, usada solo en el aviso que ve el niño. */
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
