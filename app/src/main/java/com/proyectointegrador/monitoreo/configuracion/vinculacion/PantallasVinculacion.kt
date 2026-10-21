package com.proyectointegrador.monitoreo.configuracion.vinculacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.proyectointegrador.monitoreo.R
import com.proyectointegrador.monitoreo.configuracion.GestorLimites
import com.proyectointegrador.monitoreo.configuracion.limites.TarjetaLimite
import com.proyectointegrador.monitoreo.deteccion.PermisoUso
import com.proyectointegrador.monitoreo.pausas.PermisoSuperposicion
import com.proyectointegrador.monitoreo.ui.componentes.BotonPrincipal
import com.proyectointegrador.monitoreo.ui.componentes.EncabezadoVinculacion
import com.proyectointegrador.monitoreo.ui.componentes.EtiquetaCampo
import com.proyectointegrador.monitoreo.ui.componentes.NotaGris
import com.proyectointegrador.monitoreo.ui.componentes.PanelIlustracion
import com.proyectointegrador.monitoreo.ui.componentes.Paso
import com.proyectointegrador.monitoreo.ui.componentes.TarjetaBlanca
import com.proyectointegrador.monitoreo.ui.componentes.TarjetaCrema
import com.proyectointegrador.monitoreo.ui.theme.Marca

private val EDADES = 5..12

/** Los cuatro pasos de la vinculación (HU-05 / CU-01). */
private val PASOS = listOf(
    Paso("Datos", Icons.Outlined.Person),
    Paso("Límites", Icons.Outlined.Timer),
    Paso("Permisos", Icons.Outlined.Shield),
    Paso("Éxito", Icons.Outlined.CheckCircle),
)

/** Estructura común de las pantallas de vinculación: encabezado con pasos y contenido desplazable. */
@Composable
private fun PlantillaPaso(paso: Int, contenido: @Composable ColumnScope.() -> Unit) {
    Scaffold(topBar = { EncabezadoVinculacion(PASOS, paso) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = contenido,
        )
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
    )
}

@Composable
private fun Subtitulo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyLarge,
        color = Marca.textoSecundario,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun coloresCampo(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    errorContainerColor = Color.White,
    unfocusedBorderColor = Marca.bordeTarjeta,
)

private val FormaCampo = RoundedCornerShape(12.dp)

/** Paso 1 — Datos del niño o niña (RF-10) y PIN del modo padre. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDatosMenor(vm: VinculacionViewModel, onContinuar: () -> Unit) {
    val datos by vm.datos.collectAsStateWithLifecycle()
    val errores = datos.mostrarErrores
    var menuEdadAbierto by remember { mutableStateOf(false) }

    PlantillaPaso(paso = 0) {
        PanelIlustracion(R.drawable.ilus_datos)
        Titulo("Ingrese los datos de su hijo o hija")

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EtiquetaCampo(Icons.Outlined.Face, "Nombre del niño o niña")
            OutlinedTextField(
                value = datos.nombre,
                onValueChange = vm::cambiarNombre,
                placeholder = { Text("Ej.: Mateo") },
                singleLine = true,
                shape = FormaCampo,
                colors = coloresCampo(),
                isError = errores && datos.errorNombre != null,
                supportingText = datos.errorNombre?.takeIf { errores }?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EtiquetaCampo(Icons.Outlined.CalendarMonth, "Edad")
            ExposedDropdownMenuBox(expanded = menuEdadAbierto, onExpandedChange = { menuEdadAbierto = it }) {
                OutlinedTextField(
                    value = datos.edad?.let { "$it años" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Elija la edad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuEdadAbierto) },
                    shape = FormaCampo,
                    colors = coloresCampo(),
                    isError = errores && datos.errorEdad != null,
                    supportingText = datos.errorEdad?.takeIf { errores }?.let { { Text(it) } },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = menuEdadAbierto, onDismissRequest = { menuEdadAbierto = false }) {
                    EDADES.forEach { edad ->
                        DropdownMenuItem(
                            text = { Text("$edad años") },
                            onClick = {
                                vm.cambiarEdad(edad)
                                menuEdadAbierto = false
                            },
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EtiquetaCampo(Icons.Outlined.Lock, "PIN del padre o madre")
            Text(
                "Se pedirá para entrar al modo padre, así su hijo o hija no podrá cambiar los límites.",
                style = MaterialTheme.typography.bodySmall,
                color = Marca.textoSecundario,
            )
            CampoPin(
                valor = datos.pin,
                onCambio = vm::cambiarPin,
                etiqueta = "PIN de 4 números",
                error = datos.errorPin?.takeIf { errores },
            )
            CampoPin(
                valor = datos.pinConfirmacion,
                onCambio = vm::cambiarPinConfirmacion,
                etiqueta = "Repita el PIN",
                error = datos.errorConfirmacion?.takeIf { errores },
            )
        }

        TarjetaCrema(
            Icons.Outlined.Info,
            buildAnnotatedString {
                append("Para saber cuánto tiempo se usan los videos, la app necesitará permisos de Android como ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("«Acceso a datos de uso»") }
                append(". No lee mensajes ni fotos.")
            },
        )

        BotonPrincipal("Continuar", onClick = { if (vm.validarDatos()) onContinuar() }, conFlecha = true)
    }
}

@Composable
fun CampoPin(valor: String, onCambio: (String) -> Unit, etiqueta: String, error: String?) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        shape = FormaCampo,
        colors = coloresCampo(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Paso 2 — Límites de tiempo iniciales (HU-02 / CU-03). Solo aparece la primera vez. */
@Composable
fun PantallaLimitesIniciales(vm: VinculacionViewModel, onContinuar: () -> Unit) {
    val limites by vm.limites.collectAsStateWithLifecycle()
    val mostrarErrores by vm.mostrarErroresLimites.collectAsStateWithLifecycle()
    val datos by vm.datos.collectAsStateWithLifecycle()
    val nombre = datos.nombre.trim().ifEmpty { "su hijo o hija" }

    PlantillaPaso(paso = 1) {
        Titulo("¿Cuánto tiempo al día?")
        Subtitulo(
            "Elija cuántos minutos al día puede usar $nombre cada app, entre " +
                "${GestorLimites.MINIMO} y ${GestorLimites.MAXIMO}. Podrá cambiarlos cuando quiera.",
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
        BotonPrincipal("Continuar", onClick = { if (vm.validarLimites()) onContinuar() }, conFlecha = true)
    }
}

private enum class Permiso { USO, SUPERPOSICION }

/** Datos que cambian según el permiso: nombre en Ajustes, para qué sirve y qué interruptor activar. */
private data class InfoPermiso(
    val nombre: String,
    val paraQue: String,
    val sinEl: String,
    val interruptor: String,
)

private val INFO_PERMISO = mapOf(
    Permiso.USO to InfoPermiso(
        nombre = "Acceso a datos de uso",
        paraQue = "Para medir cuánto tiempo se usan TikTok, Instagram y YouTube.",
        sinEl = "no podemos medir el uso de videos cortos",
        interruptor = "Permitir acceso al uso",
    ),
    Permiso.SUPERPOSICION to InfoPermiso(
        nombre = "Mostrar sobre otras apps",
        paraQue = "Para mostrar el aviso de pausa encima de la app de video.",
        sinEl = "no podemos mostrar el aviso de pausa",
        interruptor = "Permitir mostrar sobre otras apps",
    ),
)

/** Paso 3 — Permisos (2a) y permiso no concedido (2b). RF-11, RF-19. */
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

    // Se guía un permiso a la vez: primero el de uso y luego el de mostrar encima.
    val pendiente = when {
        !usoConcedido -> Permiso.USO
        !superposicionConcedida -> Permiso.SUPERPOSICION
        else -> null
    }
    val abrirAjustes: () -> Unit = {
        ajustesAbiertos = pendiente
        when (pendiente) {
            Permiso.USO -> PermisoUso.abrirAjustes(context)
            Permiso.SUPERPOSICION -> PermisoSuperposicion.abrirAjustes(context)
            null -> Unit
        }
    }
    val fallido = when (pendiente) {
        Permiso.USO -> fallidoUso
        Permiso.SUPERPOSICION -> fallidoSuperposicion
        null -> false
    }

    PlantillaPaso(paso = 2) {
        if (pendiente != null && fallido) {
            ContenidoPermisoDenegado(INFO_PERMISO.getValue(pendiente), abrirAjustes)
        } else {
            ContenidoPermisos(usoConcedido, superposicionConcedida, pendiente, abrirAjustes)
        }
    }
}

/** Estado 2a: lista de permisos con la guía paso a paso del que falta. */
@Composable
private fun ColumnScope.ContenidoPermisos(
    usoConcedido: Boolean,
    superposicionConcedida: Boolean,
    pendiente: Permiso?,
    abrirAjustes: () -> Unit,
) {
    PanelIlustracion(R.drawable.ilus_permiso)
    Titulo("Permisos de acceso")
    Subtitulo("Para proteger a su hijo o hija, la app necesita dos permisos de Android.")

    TarjetaBlanca {
        FilaPermiso(1, INFO_PERMISO.getValue(Permiso.USO), usoConcedido)
        if (pendiente == Permiso.USO) GuiaPasos(INFO_PERMISO.getValue(Permiso.USO))
        HorizontalDivider(color = Marca.bordeTarjeta)
        FilaPermiso(2, INFO_PERMISO.getValue(Permiso.SUPERPOSICION), superposicionConcedida)
        if (pendiente == Permiso.SUPERPOSICION) GuiaPasos(INFO_PERMISO.getValue(Permiso.SUPERPOSICION))
    }

    TarjetaCrema(
        Icons.Outlined.Info,
        "Solo medimos cuánto tiempo se usan las apps de video corto. No leemos mensajes ni fotos.",
        titulo = "Privacidad garantizada",
    )

    if (pendiente != null) {
        BotonPrincipal("Abrir ajustes de Android", onClick = abrirAjustes, conFlecha = true)
    }
}

@Composable
private fun FilaPermiso(numero: Int, info: InfoPermiso, concedido: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (concedido) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Activado",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        } else {
            NumeroEnCirculo(numero)
        }
        Column(Modifier.weight(1f)) {
            Text(info.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(info.paraQue, style = MaterialTheme.typography.bodySmall, color = Marca.textoSecundario)
        }
        Text(
            if (concedido) "Activado" else "Pendiente",
            style = MaterialTheme.typography.labelMedium,
            color = if (concedido) MaterialTheme.colorScheme.primary else Marca.ambar,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NumeroEnCirculo(numero: Int) {
    Box(
        Modifier
            .size(28.dp)
            .background(Marca.celeste, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$numero",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** Línea de tiempo con los tres pasos a seguir en los ajustes de Android. */
@Composable
private fun GuiaPasos(info: InfoPermiso) {
    val pasos = listOf(
        Triple(Icons.Outlined.TouchApp, "Abrir ajustes", "Toque el botón de abajo para ir a «${info.nombre}»."),
        Triple(Icons.Outlined.Search, "Buscar la app", "Busque «Monitoreo Parental» en la lista de apps."),
        Triple(Icons.Outlined.ToggleOn, "Activar el permiso", "Active «${info.interruptor}» y vuelva con el botón Atrás."),
    )
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text("Pasos a seguir", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        pasos.forEachIndexed { i, (icono, titulo, texto) ->
            PasoGuia(i + 1, icono, titulo, texto, ultimo = i == pasos.lastIndex)
        }
    }
}

@Composable
private fun PasoGuia(numero: Int, icono: ImageVector, titulo: String, texto: String, ultimo: Boolean) {
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            NumeroEnCirculo(numero)
            if (!ultimo) {
                Box(
                    Modifier
                        .padding(vertical = 4.dp)
                        .width(2.dp)
                        .weight(1f)
                        .background(Marca.bordeTarjeta),
                )
            }
        }
        Column(Modifier.padding(bottom = if (ultimo) 0.dp else 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(texto, style = MaterialTheme.typography.bodyMedium, color = Marca.textoSecundario)
        }
    }
}

/** Estado 2b: el padre volvió de los ajustes sin activar el permiso. */
@Composable
private fun ColumnScope.ContenidoPermisoDenegado(info: InfoPermiso, abrirAjustes: () -> Unit) {
    PanelIlustracion(R.drawable.ilus_error, fondo = MaterialTheme.colorScheme.errorContainer)
    Titulo("Permiso necesario")
    Subtitulo("Para proteger a su hijo o hija, la app necesita este permiso de Android.")
    TarjetaCrema(
        Icons.Outlined.GppMaybe,
        "Aún no ha activado «${info.nombre}». Sin él ${info.sinEl}.",
        titulo = "Acceso denegado",
    )
    NotaGris(
        Icons.Outlined.WarningAmber,
        "Busque «Monitoreo Parental» en «${info.nombre}» y active el interruptor.",
    )
    BotonPrincipal("Intentar de nuevo", onClick = abrirAjustes, icono = Icons.Outlined.Refresh)
}

/** Paso 4 — Vinculación exitosa (RF-12). */
@Composable
fun PantallaVinculacionExitosa(nombre: String, onIrAlPanel: () -> Unit) {
    PlantillaPaso(paso = 3) {
        PanelIlustracion(R.drawable.ilus_exito)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(8.dp))
            Text("¡Listo!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        Text(
            buildAnnotatedString {
                append("Ya estamos midiendo el uso de ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) { append(nombre) }
                append(". Desde el panel principal podrá ver su actividad y ajustar los límites.")
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Marca.textoSecundario,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        TarjetaCrema(
            Icons.Outlined.Update,
            "Los datos se actualizan cada 15 minutos y cada vez que abre el panel.",
            titulo = "Actualización automática",
        )
        BotonPrincipal("Ir al panel", onClick = onIrAlPanel, conFlecha = true)
        Text(
            "Los datos de su hijo o hija se guardan solo en este dispositivo.",
            style = MaterialTheme.typography.bodySmall,
            color = Marca.textoSecundario,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
