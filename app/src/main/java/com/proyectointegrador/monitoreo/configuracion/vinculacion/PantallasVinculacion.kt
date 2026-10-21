package com.proyectointegrador.monitoreo.configuracion.vinculacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.proyectointegrador.monitoreo.configuracion.GestorLimites
import com.proyectointegrador.monitoreo.configuracion.limites.TarjetaLimite
import com.proyectointegrador.monitoreo.deteccion.PermisoUso
import com.proyectointegrador.monitoreo.pausas.PermisoSuperposicion

private val EDADES = 5..12

/** Barra de cuatro pasos (Datos, Límites, Permisos, Éxito) de la vinculación. */
@Composable
fun BarraPasos(pasoActual: Int, modifier: Modifier = Modifier) {
    val pasos = listOf("Datos", "Límites", "Permisos", "Éxito")
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        pasos.forEachIndexed { indice, nombre ->
            val activo = indice <= pasoActual
            val color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            Column(Modifier.weight(1f)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${indice + 1}. $nombre",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Estructura común de las pantallas de vinculación. */
@Composable
private fun PlantillaPaso(paso: Int, titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BarraPasos(paso)
            Text(titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            contenido()
        }
    }
}

@Composable
private fun TarjetaAviso(icono: ImageVector, texto: String, error: Boolean = false) {
    val colores = if (error) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    }
    Card(colors = colores) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icono, contentDescription = null)
            Text(texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Pantalla 1 — Datos del niño/a (RF-10). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaDatosMenor(vm: VinculacionViewModel, onContinuar: () -> Unit) {
    val datos by vm.datos.collectAsStateWithLifecycle()
    val errores = datos.mostrarErrores

    PlantillaPaso(paso = 0, titulo = "Datos del niño o niña") {
        OutlinedTextField(
            value = datos.nombre,
            onValueChange = vm::cambiarNombre,
            label = { Text("Nombre") },
            singleLine = true,
            isError = errores && datos.errorNombre != null,
            supportingText = datos.errorNombre?.takeIf { errores }?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Edad", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EDADES.forEach { edad ->
                FilterChip(
                    selected = datos.edad == edad,
                    onClick = { vm.cambiarEdad(edad) },
                    label = { Text("$edad años") },
                )
            }
        }
        if (errores && datos.errorEdad != null) {
            Text(datos.errorEdad!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Text("PIN del padre o madre", style = MaterialTheme.typography.titleSmall)
        Text(
            "Lo pedirá la app para entrar al modo padre, así el niño no podrá cambiar los límites.",
            style = MaterialTheme.typography.bodySmall,
        )
        CampoPin(
            valor = datos.pin,
            onCambio = vm::cambiarPin,
            etiqueta = "PIN (4 números)",
            error = datos.errorPin?.takeIf { errores },
        )
        CampoPin(
            valor = datos.pinConfirmacion,
            onCambio = vm::cambiarPinConfirmacion,
            etiqueta = "Repite el PIN",
            error = datos.errorConfirmacion?.takeIf { errores },
        )

        TarjetaAviso(
            Icons.Default.Info,
            "La app necesitará el permiso de Android \"Acceso a datos de uso\". " +
                "Solo mide el tiempo en apps de video corto: no lee mensajes ni fotos.",
        )

        Button(
            onClick = { if (vm.validarDatos()) onContinuar() },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Continuar") }
    }
}

@Composable
fun CampoPin(valor: String, onCambio: (String) -> Unit, etiqueta: String, error: String?) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
    )
}

private enum class Permiso { USO, SUPERPOSICION }

/** Paso 2 — Límites de tiempo iniciales (HU-02 / CU-03). Solo aparece la primera vez. */
@Composable
fun PantallaLimitesIniciales(vm: VinculacionViewModel, onContinuar: () -> Unit) {
    val limites by vm.limites.collectAsStateWithLifecycle()
    val mostrarErrores by vm.mostrarErroresLimites.collectAsStateWithLifecycle()

    PlantillaPaso(paso = 1, titulo = "Límites de tiempo") {
        Text(
            "¿Cuántos minutos al día puede usar cada app? Escribe un valor entre " +
                "${GestorLimites.MINIMO} y ${GestorLimites.MAXIMO} minutos.",
            style = MaterialTheme.typography.bodyMedium,
        )
        limites.forEach { limite ->
            TarjetaLimite(
                limite = limite,
                // Un campo vacío solo se marca en rojo después de pulsar "Continuar".
                mostrarError = mostrarErrores || limite.texto.isNotEmpty(),
                onTexto = { vm.cambiarTextoLimite(limite.paquete, it) },
                onSumar = { vm.sumarLimite(limite.paquete, it) },
                onMonitoreo = { vm.cambiarMonitoreo(limite.paquete, it) },
            )
        }
        TarjetaAviso(Icons.Default.Info, "Podrás cambiar estos límites cuando quieras desde el modo padre.")
        Button(
            onClick = { if (vm.validarLimites()) onContinuar() },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Continuar") }
    }
}

/** Pantalla 2 — Permisos (2a) y permiso no concedido (2b). RF-11, RF-19. */
@Composable
fun PantallaPermiso(onConcedido: () -> Unit) {
    val context = LocalContext.current
    var usoConcedido by remember { mutableStateOf(PermisoUso.estaConcedido(context)) }
    var superposicionConcedida by remember { mutableStateOf(PermisoSuperposicion.estaConcedido(context)) }

    // Permiso cuyos ajustes se abrieron y aún no se ha vuelto a la app.
    var ajustesAbiertos by rememberSaveable { mutableStateOf<Permiso?>(null) }
    // Se vuelven true si el padre regresó de los ajustes sin activar ese permiso (estado 2b).
    var fallidoUso by rememberSaveable { mutableStateOf(false) }
    var fallidoSuperposicion by rememberSaveable { mutableStateOf(false) }

    // Cada vez que el padre vuelve a la app (desde los ajustes), se verifican los permisos.
    LifecycleResumeEffect(Unit) {
        usoConcedido = PermisoUso.estaConcedido(context)
        superposicionConcedida = PermisoSuperposicion.estaConcedido(context)
        when (ajustesAbiertos) {
            Permiso.USO -> fallidoUso = !usoConcedido
            Permiso.SUPERPOSICION -> fallidoSuperposicion = !superposicionConcedida
            null -> Unit
        }
        ajustesAbiertos = null
        if (usoConcedido && superposicionConcedida) onConcedido()
        onPauseOrDispose { }
    }

    val faltantes = buildList {
        if (fallidoUso && !usoConcedido) add("\"Acceso a datos de uso\"")
        if (fallidoSuperposicion && !superposicionConcedida) add("\"Mostrar sobre otras apps\"")
    }

    PlantillaPaso(paso = 2, titulo = "Permisos") {
        if (faltantes.isNotEmpty()) {
            TarjetaAviso(
                Icons.Default.Warning,
                "Permiso necesario: no se activó ${faltantes.joinToString(" ni ")}. " +
                    "Sin él la app no puede funcionar.",
                error = true,
            )
        }

        TarjetaPermiso(
            numero = 1,
            titulo = "Acceso a datos de uso",
            descripcion = "Para medir cuánto tiempo se usa TikTok, Instagram y YouTube.",
            instrucciones = "Busca \"Monitoreo Parental\" en la lista y activa \"Permitir acceso al uso\". " +
                "Luego vuelve con el botón Atrás.",
            concedido = usoConcedido,
            fallido = fallidoUso,
            onAbrirAjustes = {
                ajustesAbiertos = Permiso.USO
                PermisoUso.abrirAjustes(context)
            },
        )
        TarjetaPermiso(
            numero = 2,
            titulo = "Mostrar sobre otras apps",
            descripcion = "Para mostrar el aviso de pausa encima de la app de video cuando se llega al límite.",
            instrucciones = "Activa \"Permitir mostrar sobre otras apps\" y vuelve con el botón Atrás.",
            concedido = superposicionConcedida,
            fallido = fallidoSuperposicion,
            onAbrirAjustes = {
                ajustesAbiertos = Permiso.SUPERPOSICION
                PermisoSuperposicion.abrirAjustes(context)
            },
        )

        TarjetaAviso(
            Icons.Default.Info,
            "Solo se mide el tiempo de uso de TikTok, Instagram y YouTube. La app no lee mensajes ni fotos.",
        )
    }
}

@Composable
private fun TarjetaPermiso(
    numero: Int,
    titulo: String,
    descripcion: String,
    instrucciones: String,
    concedido: Boolean,
    fallido: Boolean,
    onAbrirAjustes: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$numero. $titulo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (concedido) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Concedido", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Text(descripcion, style = MaterialTheme.typography.bodyMedium)
            if (!concedido) {
                Text(instrucciones, style = MaterialTheme.typography.bodySmall)
                Button(onClick = onAbrirAjustes, modifier = Modifier.fillMaxWidth()) {
                    Text(if (fallido) "Intentar de nuevo" else "Abrir ajustes de Android")
                }
            }
        }
    }
}

/** Pantalla 3 — Vinculación exitosa (RF-12). */
@Composable
fun PantallaVinculacionExitosa(nombre: String, onIrAlPanel: () -> Unit) {
    PlantillaPaso(paso = 3, titulo = "¡Vinculación exitosa!") {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally),
        )
        Text(
            "Ya estamos midiendo el uso de videos cortos de $nombre.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        TarjetaAviso(
            Icons.Default.Info,
            "Los datos se actualizan cada 15 minutos y se guardan solo en este dispositivo.",
        )
        Button(onClick = onIrAlPanel, modifier = Modifier.fillMaxWidth()) { Text("Ir al panel") }
    }
}
