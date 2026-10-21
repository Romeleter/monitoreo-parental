package com.proyectointegrador.monitoreo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colores = lightColorScheme(
    primary = Color(0xFF00796B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00251F),
    secondary = Color(0xFFFF8F00),
    secondaryContainer = Color(0xFFCDE8E3),
    onSecondaryContainer = Color(0xFF00251F),
    background = Color(0xFFF5F8F7),
    surface = Color(0xFFF5F8F7),
    surfaceVariant = Color(0xFFDDE5E2),
    onSurfaceVariant = Color(0xFF3F4946),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF4F2),
    surfaceContainer = Color(0xFFE9EFED),
    surfaceContainerHigh = Color(0xFFE3EAE7),
    surfaceContainerHighest = Color(0xFFDDE5E2),
    outline = Color(0xFF6F7976),
    outlineVariant = Color(0xFFBFC9C5),
    error = Color(0xFFC62828),
)

/** Colores de los estados de uso en el resumen del día (pantalla 5). */
object ColoresEstado {
    val dentro = Color(0xFF2E7D32)
    val cerca = Color(0xFFEF6C00)
    val alcanzado = Color(0xFFC62828)
}

@Composable
fun TemaMonitoreo(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colores, content = content)
}
