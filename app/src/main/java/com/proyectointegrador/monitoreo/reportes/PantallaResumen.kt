package com.proyectointegrador.monitoreo.reportes

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.proyectointegrador.monitoreo.MonitoreoApp
import com.proyectointegrador.monitoreo.R
import com.proyectointegrador.monitoreo.configuracion.BloqueoModoPadre
import com.proyectointegrador.monitoreo.configuracion.GestorLimites
import com.proyectointegrador.monitoreo.data.AppMonitoreada
import com.proyectointegrador.monitoreo.deteccion.PermisoUso
import com.proyectointegrador.monitoreo.deteccion.ServicioDeteccion
import com.proyectointegrador.monitoreo.pausas.PermisoSuperposicion
import com.proyectointegrador.monitoreo.pausas.ServicioVigilancia
import com.proyectointegrador.monitoreo.ui.componentes.BotonPrincipal
import com.proyectointegrador.monitoreo.ui.componentes.BotonSecundario
import com.proyectointegrador.monitoreo.ui.componentes.EncabezadoMarca
import com.proyectointegrador.monitoreo.ui.componentes.IconoApp
import com.proyectointegrador.monitoreo.ui.componentes.PanelIlustracion
import com.proyectointegrador.monitoreo.ui.componentes.TarjetaBlanca
import com.proyectointegrador.monitoreo.ui.formatearHora
import com.proyectointegrador.monitoreo.ui.formatearMinutos
import com.proyectointegrador.monitoreo.ui.theme.ColoresEstado
import com.proyectointegrador.monitoreo.ui.theme.Marca
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ResumenViewModel(
    generador: GeneradorReporte,
    private val servicio: ServicioDeteccion,
    private val gestorLimites: GestorLimites,
) : ViewModel() {
    val resumen: StateFlow<ResumenDelDia?> = generador.resumenDeHoy()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val ultimaActualizacion: StateFlow<Long?> = servicio.ultimaActualizacion

    /** Consulta UsageStatsManager ya, sin esperar a la tarea de cada 15 minutos. */
    fun actualizar() {
        viewModelScope.launch { servicio.actualizarUso() }
    }

    /** CU-04: bloquear o desbloquear una app de inmediato desde el modo padre. */
    fun cambiarBloqueo(paquete: String, bloqueada: Boolean) {
        viewModelScope.launch { gestorLimites.cambiarBloqueoManual(paquete, bloqueada) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MonitoreoApp
                ResumenViewModel(app.generadorReporte, app.servicioDeteccion, app.gestorLimites)
            }
        }
    }
}

/** Pantalla 5 — Resumen de hoy: con actividad (5a) y sin actividad (5b). */
@Composable
fun PantallaResumen(
    onConfigurarLimites: () -> Unit,
    onVerHistorial: () -> Unit,
    vm: ResumenViewModel = viewModel(factory = ResumenViewModel.Factory),
) {
    val resumen by vm.resumen.collectAsStateWithLifecycle()
    val ultimaActualizacion by vm.ultimaActualizacion.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hayPermisoUso by remember { mutableStateOf(true) }
    var hayPermisoSuperposicion by remember { mutableStateOf(true) }

    // Al abrir la pantalla (o volver a ella) se revisan los permisos, se actualizan los datos
    // y se asegura que la vigilancia esté corriendo.
    LifecycleResumeEffect(Unit) {
        hayPermisoUso = PermisoUso.estaConcedido(context)
        hayPermisoSuperposicion = PermisoSuperposicion.estaConcedido(context)
        if (hayPermisoUso) vm.actualizar()
        ServicioVigilancia.iniciar(context)
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            Column {
                EncabezadoMarca {
                    IconButton(onClick = vm::actualizar, enabled = hayPermisoUso) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Actualizar ahora")
                    }
                    IconButton(onClick = onConfigurarLimites) {
                        Icon(Icons.Outlined.Tune, contentDescription = "Configurar límites")
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
    ) { padding ->
        val r = resumen ?: return@Scaffold
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            r.perfil?.let { perfil ->
                item { TarjetaPerfil("${perfil.nombre}, ${perfil.edad} años", ultimaActualizacion) }
            }
            if (!hayPermisoUso) {
                item {
                    TarjetaSinPermiso(
                        nombrePermiso = "Acceso a datos de uso",
                        consecuencia = "no se puede medir el tiempo en las apps de video corto",
                        onAbrirAjustes = {
                            BloqueoModoPadre.permitirSalidaUnaVez()
                            PermisoUso.abrirAjustes(context)
                        },
                    )
                }
            }
            if (!hayPermisoSuperposicion) {
                item {
                    TarjetaSinPermiso(
                        nombrePermiso = "Mostrar sobre otras apps",
                        consecuencia = "no se puede mostrar el aviso de pausa",
                        onAbrirAjustes = {
                            BloqueoModoPadre.permitirSalidaUnaVez()
                            PermisoSuperposicion.abrirAjustes(context)
                        },
                    )
                }
            }
            if (!r.hayActividad) {
                item { EstadoSinActividad(r.perfil?.nombre ?: "Su hijo o hija", onConfigurarLimites) }
            } else {
                item {
                    Text(
                        "Actividad de hoy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                items(r.usos, key = { it.app.paquete }) { TarjetaUsoApp(it) }
                item { TarjetaPausas(r.pausasHoy, r.ultimoReconocimiento, r.intentosReapertura) }
            }
            item { BotonSecundario("Ver últimos 7 días", onClick = onVerHistorial, icono = Icons.Outlined.BarChart) }
            item { TarjetaBloqueoManual(r.usos.map { it.app }, onCambiar = vm::cambiarBloqueo) }
        }
    }
}

@Composable
private fun TarjetaPerfil(perfil: String, ultimaActualizacion: Long?) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Marca.celeste, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text("Perfil activo", style = MaterialTheme.typography.bodyMedium, color = Marca.textoSecundario)
        Text(
            perfil,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        if (ultimaActualizacion != null) {
            Text(
                "Actualizado a las ${formatearHora(ultimaActualizacion)} · cada 15 minutos",
                style = MaterialTheme.typography.bodySmall,
                color = Marca.textoSecundario,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** RF-19: si un permiso se retiró después de la vinculación, se guía al padre para activarlo. */
@Composable
private fun TarjetaSinPermiso(nombrePermiso: String, consecuencia: String, onAbrirAjustes: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text(
                    "Permiso necesario",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            Text(
                "Se desactivó el permiso «$nombrePermiso». Sin él $consecuencia.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Button(
                onClick = onAbrirAjustes,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) { Text("Abrir ajustes de Android") }
        }
    }
}

/** Estado 5b: todavía no hay uso registrado hoy. */
@Composable
private fun EstadoSinActividad(nombre: String, onConfigurarLimites: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PanelIlustracion(R.drawable.ilus_sin_actividad, fondo = Marca.cremaFondo, alto = 200.dp)
        Text(
            "Aún no hay actividad registrada hoy",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            "$nombre no ha usado las apps supervisadas en lo que va del día.",
            style = MaterialTheme.typography.bodyLarge,
            color = Marca.textoSecundario,
            textAlign = TextAlign.Center,
        )
        BotonPrincipal("Configurar límites", onClick = onConfigurarLimites, icono = Icons.Outlined.Tune)
    }
}

/** Estado de una app con su color, ícono y texto. El texto evita depender solo del color. */
private data class Estado(val color: Color, val icono: ImageVector, val texto: String)

private fun estadoDe(uso: UsoApp): Estado = when {
    uso.app.bloqueoManual -> Estado(ColoresEstado.alcanzado, Icons.Filled.Lock, "Bloqueada por usted")
    !uso.app.monitoreoActivo -> Estado(Color(0xFF7A8583), Icons.Filled.PauseCircle, "Monitoreo en pausa")
    uso.estado == EstadoUso.ALCANZADO -> Estado(ColoresEstado.alcanzado, Icons.Filled.Block, "Límite alcanzado")
    uso.estado == EstadoUso.CERCA -> Estado(ColoresEstado.cerca, Icons.Filled.Warning, "Cerca del límite")
    else -> Estado(ColoresEstado.dentro, Icons.Filled.CheckCircle, "Dentro del límite")
}

@Composable
private fun TarjetaUsoApp(uso: UsoApp) {
    val estado = estadoDe(uso)
    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconoApp(uso.app.paquete, tamano = 42.dp)
            Column(Modifier.weight(1f)) {
                Text(uso.app.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Límite: ${formatearMinutos(uso.app.limiteDiarioMin)} al día",
                    style = MaterialTheme.typography.bodySmall,
                    color = Marca.textoSecundario,
                )
            }
            Text(formatearMinutos(uso.minutos), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(estado.icono, contentDescription = null, tint = estado.color, modifier = Modifier.size(16.dp))
            Text(estado.texto, style = MaterialTheme.typography.labelLarge, color = estado.color)
        }
        LinearProgressIndicator(
            progress = { uso.fraccion },
            color = estado.color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )
    }
}

@Composable
private fun IconoEnCirculo(icono: ImageVector) {
    Box(
        Modifier
            .size(36.dp)
            .background(Marca.celeste, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
    }
}

/** Pausas de hoy (RF-24), último reconocimiento del aviso (RF-16) e intentos de reapertura (RF-29). */
@Composable
private fun TarjetaPausas(pausas: Int, ultimoReconocimiento: Long?, intentos: List<Pair<AppMonitoreada, Int>>) {
    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconoEnCirculo(Icons.Outlined.PauseCircle)
            Text("Pausas de hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) { append("$pausas") }
                    append(if (pausas == 1) " aviso" else " avisos")
                },
                style = MaterialTheme.typography.titleMedium,
                color = Marca.textoSecundario,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(Marca.celeste, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text(
                if (pausas == 0) {
                    buildAnnotatedString { append("Hoy no se ha mostrado ningún aviso.") }
                } else if (ultimoReconocimiento != null) {
                    buildAnnotatedString {
                        append("Último aviso reconocido a las ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(formatearHora(ultimoReconocimiento)) }
                    }
                } else {
                    buildAnnotatedString { append("Todavía no se ha reconocido ningún aviso hoy.") }
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        intentos.forEach { (app, veces) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Replay, contentDescription = null, tint = Marca.ambar, modifier = Modifier.size(18.dp))
                Text(
                    buildAnnotatedString {
                        append("Intentó volver a abrir ${app.nombre} ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(if (veces == 1) "1 vez" else "$veces veces") }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

/** HU-03 / CU-04: el padre bloquea o desbloquea una app al instante (RF-06, RF-20). */
@Composable
private fun TarjetaBloqueoManual(apps: List<AppMonitoreada>, onCambiar: (String, Boolean) -> Unit) {
    TarjetaBlanca {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconoEnCirculo(Icons.Outlined.Lock)
            Column(Modifier.weight(1f)) {
                Text("Bloqueo manual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Bloquee una app al instante, por ejemplo durante las tareas o la comida.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Marca.textoSecundario,
                )
            }
        }
        apps.forEach { app ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconoApp(app.paquete, tamano = 32.dp)
                Text(app.nombre, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(
                    if (app.bloqueoManual) "Bloqueada" else "Permitida",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (app.bloqueoManual) ColoresEstado.alcanzado else Marca.textoSecundario,
                )
                Switch(checked = app.bloqueoManual, onCheckedChange = { onCambiar(app.paquete, it) })
            }
        }
    }
}
