package com.proyectointegrador.monitoreo.ui.componentes

import android.content.pm.PackageManager
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.proyectointegrador.monitoreo.R
import com.proyectointegrador.monitoreo.ui.theme.Marca

// Piezas visuales compartidas por todas las pantallas

/** Encabezado con el escudo y el nombre de la app. [derecha] admite acciones o el "PASO X DE 4" */
@Composable
fun EncabezadoMarca(
    modifier: Modifier = Modifier,
    onAtras: (() -> Unit)? = null,
    derecha: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            // El encabezado empieza debajo de la barra de estado (hora, batería)
            .statusBarsPadding()
            .height(64.dp)
            .padding(start = if (onAtras != null) 4.dp else 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onAtras != null) {
            IconButton(onClick = onAtras) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(Modifier.width(4.dp))
        }
        Image(
            painterResource(R.drawable.logo_simbolo),
            contentDescription = null,
            modifier = Modifier.height(36.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "Monitoreo Parental",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        derecha()
    }
}

/** Paso del indicador del registro */
data class Paso(val nombre: String, val icono: ImageVector)

/** Círculos con ícono unidos por líneas */
@Composable
fun IndicadorPasos(pasos: List<Paso>, pasoActual: Int, modifier: Modifier = Modifier) {
    val primario = MaterialTheme.colorScheme.primary
    val apagado = MaterialTheme.colorScheme.outlineVariant
    Row(modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.Top) {
        pasos.forEachIndexed { i, paso ->
            val hecho = i <= pasoActual
            Column(Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (hecho) primario else Color.White)
                        .border(1.5.dp, if (hecho) primario else apagado, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        paso.icono,
                        contentDescription = null,
                        tint = if (hecho) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    paso.nombre,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (i == pasoActual) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (i == pasoActual) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (i < pasos.lastIndex) {
                Box(
                    Modifier
                        .weight(1f)
                        .padding(top = 19.dp)
                        .height(2.dp)
                        .background(if (i < pasoActual) primario else apagado),
                )
            }
        }
    }
}

/** Encabezado del registro: marca, "PASO X DE N", indicador y línea divisoria */
@Composable
fun EncabezadoVinculacion(pasos: List<Paso>, pasoActual: Int) {
    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        EncabezadoMarca {
            Text(
                "PASO ${pasoActual + 1} DE ${pasos.size}",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
        IndicadorPasos(pasos, pasoActual)
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** Botón principal grande */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    conFlecha: Boolean = false,
    habilitado: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(texto.uppercase(), fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, fontSize = 15.sp)
        if (conFlecha) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(22.dp))
        }
    }
}

/** Botón secundario con borde */
@Composable
fun BotonSecundario(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, icono: ImageVector? = null) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, fontWeight = FontWeight.SemiBold)
    }
}

/** Tarjeta blanca con borde suave, la base de las pantallas del padre */
@Composable
fun TarjetaBlanca(
    modifier: Modifier = Modifier,
    relleno: Dp = 16.dp,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Marca.tarjeta),
        border = BorderStroke(1.dp, Marca.bordeTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(relleno), verticalArrangement = Arrangement.spacedBy(12.dp), content = contenido)
    }
}

/** Tarjeta crema con ícono ámbar, para información importante que no es un error */
@Composable
fun TarjetaCrema(icono: ImageVector, texto: AnnotatedString, modifier: Modifier = Modifier, titulo: String? = null) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Marca.cremaFondo,
        border = BorderStroke(1.dp, Marca.cremaBorde),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icono, contentDescription = null, tint = Marca.ambar, modifier = Modifier.size(22.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (titulo != null) {
                    Text(titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Marca.textoCrema)
                }
                Text(texto, style = MaterialTheme.typography.bodyMedium, color = Marca.textoCrema)
            }
        }
    }
}

@Composable
fun TarjetaCrema(icono: ImageVector, texto: String, modifier: Modifier = Modifier, titulo: String? = null) =
    TarjetaCrema(icono, AnnotatedString(texto), modifier, titulo)

/** Nota gris con ícono */
@Composable
fun NotaGris(icono: ImageVector, texto: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Panel de color suave con una ilustración centrada */
@Composable
fun PanelIlustracion(@DrawableRes ilustracion: Int, modifier: Modifier = Modifier, alto: Dp = 190.dp, fondo: Color = Marca.celeste) {
    Box(
        modifier
            .fillMaxWidth()
            .height(alto)
            .clip(RoundedCornerShape(24.dp))
            .background(fondo),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(ilustracion), contentDescription = null, modifier = Modifier.fillMaxHeight(0.86f))
    }
}

/** Etiqueta de un campo con un ícono pequeño */
@Composable
fun EtiquetaCampo(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

/** Ícono real de la app si está instalada; si no, uno genérico sobre fondo menta */
@Composable
fun IconoApp(paquete: String, modifier: Modifier = Modifier, tamano: Dp = 44.dp) {
    val context = LocalContext.current
    val icono = remember(paquete) {
        try {
            context.packageManager.getApplicationIcon(paquete).toBitmap(144, 144).asImageBitmap()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
    if (icono != null) {
        Image(icono, contentDescription = null, modifier = modifier.size(tamano).clip(RoundedCornerShape(12.dp)))
    } else {
        Box(
            modifier
                .size(tamano)
                .clip(RoundedCornerShape(12.dp))
                .background(Marca.celeste),
            contentAlignment = Alignment.Center,
        ) {
            Icon(iconoDeRespaldo(paquete), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun iconoDeRespaldo(paquete: String): ImageVector = when {
    "musically" in paquete -> Icons.Default.MusicNote
    "instagram" in paquete -> Icons.Default.PhotoCamera
    "youtube" in paquete -> Icons.Default.PlayCircle
    else -> Icons.Default.Apps
}
