package com.proyectointegrador.monitoreo.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.proyectointegrador.monitoreo.configuracion.BloqueoModoPadre
import com.proyectointegrador.monitoreo.configuracion.GestorLimites
import com.proyectointegrador.monitoreo.data.AppMonitoreada
import com.proyectointegrador.monitoreo.deteccion.PermisoUso
import com.proyectointegrador.monitoreo.deteccion.ServicioDeteccion
import com.proyectointegrador.monitoreo.pausas.PermisoSuperposicion
import com.proyectointegrador.monitoreo.pausas.ServicioVigilancia
import com.proyectointegrador.monitoreo.ui.formatearHora
import com.proyectointegrador.monitoreo.ui.formatearMinutos
import com.proyectointegrador.monitoreo.ui.theme.ColoresEstado
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
@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = { Text("Resumen de hoy") },
                actions = {
                    IconButton(onClick = vm::actualizar, enabled = hayPermisoUso) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar ahora")
                    }
                    IconButton(onClick = onConfigurarLimites) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurar límites")
                    }
                },
            )
        },
    ) { padding ->
        val r = resumen ?: return@Scaffold
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            r.perfil?.let { perfil ->
                item {
                    Text(
                        "Perfil: ${perfil.nombre}, ${perfil.edad} años",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    ultimaActualizacion?.let {
                        Text(
                            "Actualizado a las ${formatearHora(it)} · se actualiza cada 15 minutos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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
                item { TarjetaSinActividad(onConfigurarLimites) }
            } else {
                items(r.usos, key = { it.app.paquete }) { TarjetaUsoApp(it) }
                item { TarjetaPausas(r.pausasHoy, r.ultimoReconocimiento) }
            }
            item {
                OutlinedButton(onClick = onVerHistorial, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Ver últimos 7 días", modifier = Modifier.padding(start = 8.dp))
                }
            }
            item { TarjetaBloqueoManual(r.usos.map { it.app }, onCambiar = vm::cambiarBloqueo) }
        }
    }
}

/** RF-19: si un permiso se retiró después de la vinculación, se guía al padre para activarlo. */
@Composable
private fun TarjetaSinPermiso(nombrePermiso: String, consecuencia: String, onAbrirAjustes: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EstadoConIcono(Icons.Default.Warning, "Permiso necesario", MaterialTheme.colorScheme.error)
            Text(
                "Se desactivó el permiso \"$nombrePermiso\". Sin él $consecuencia.",
            )
            Button(onClick = onAbrirAjustes) { Text("Abrir ajustes de Android") }
        }
    }
}

@Composable
private fun TarjetaSinActividad(onConfigurarLimites: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.HourglassEmpty,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                "Aún no hay actividad registrada hoy",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onConfigurarLimites) { Text("Configurar límites") }
        }
    }
}

@Composable
private fun TarjetaUsoApp(uso: UsoApp) {
    val (color, icono, texto) = when {
        uso.app.bloqueoManual -> Triple(ColoresEstado.alcanzado, Icons.Default.Lock, "Bloqueada por ti")
        !uso.app.monitoreoActivo -> Triple(Color.Gray, Icons.Default.PauseCircle, "Monitoreo pausado")
        uso.estado == EstadoUso.ALCANZADO -> Triple(ColoresEstado.alcanzado, Icons.Default.Block, "Límite alcanzado")
        uso.estado == EstadoUso.CERCA -> Triple(ColoresEstado.cerca, Icons.Default.Warning, "Cerca del límite")
        else -> Triple(ColoresEstado.dentro, Icons.Default.CheckCircle, "Dentro del límite")
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(uso.app.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    "${formatearMinutos(uso.minutos)} de ${formatearMinutos(uso.app.limiteDiarioMin)}",
                    fontWeight = FontWeight.SemiBold,
                )
            }
            LinearProgressIndicator(
                progress = { uso.fraccion },
                color = color,
                modifier = Modifier.fillMaxWidth(),
            )
            EstadoConIcono(icono, texto, color)
        }
    }
}

@Composable
private fun EstadoConIcono(icono: ImageVector, texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(texto, color = color, style = MaterialTheme.typography.bodyMedium)
    }
}

/** HU-03 / CU-04: el padre bloquea o desbloquea una app al instante (RF-06, RF-20). */
@Composable
private fun TarjetaBloqueoManual(apps: List<AppMonitoreada>, onCambiar: (String, Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Bloqueo manual", style = MaterialTheme.typography.titleMedium)
            Text(
                "Bloquea una app ahora mismo, por ejemplo durante las tareas o la comida.",
                style = MaterialTheme.typography.bodySmall,
            )
            apps.forEach { app ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(app.nombre, modifier = Modifier.weight(1f))
                    Text(
                        if (app.bloqueoManual) "Bloqueada" else "Permitida",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (app.bloqueoManual) ColoresEstado.alcanzado else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Switch(
                        checked = app.bloqueoManual,
                        onCheckedChange = { onCambiar(app.paquete, it) },
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaPausas(pausas: Int, ultimoReconocimiento: Long?) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Pausas de hoy", style = MaterialTheme.typography.titleMedium)
            Text("Avisos mostrados: $pausas")
            Text(
                "Último reconocimiento: " + (ultimoReconocimiento?.let(::formatearHora) ?: "—"),
            )
        }
    }
}
