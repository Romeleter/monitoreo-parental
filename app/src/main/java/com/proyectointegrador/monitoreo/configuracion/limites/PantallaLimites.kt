package com.proyectointegrador.monitoreo.configuracion.limites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyectointegrador.monitoreo.configuracion.GestorLimites

/** Pantalla 4 — Límites de tiempo (HU-02 / CU-03). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLimites(
    onAtras: () -> Unit,
    vm: LimitesViewModel = viewModel(factory = LimitesViewModel.Factory),
) {
    val limites by vm.limites.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.mensajes.collect { snackbar.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Límites de tiempo") },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Elige cuántos minutos al día puede usar cada app. " +
                        "El límite debe estar entre ${GestorLimites.MINIMO} y ${GestorLimites.MAXIMO} minutos.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            items(limites, key = { it.paquete }) { limite ->
                TarjetaLimite(
                    limite = limite,
                    mostrarError = true,
                    onTexto = { vm.cambiarTexto(limite.paquete, it) },
                    onSumar = { vm.sumar(limite.paquete, it) },
                    onMonitoreo = { vm.cambiarMonitoreo(limite.paquete, it) },
                )
            }
            item {
                Button(onClick = vm::guardar, modifier = Modifier.fillMaxWidth()) {
                    Text("Guardar límites")
                }
            }
        }
    }
}

/** Tarjeta de una app con el campo de minutos, botones − y + y el interruptor de monitoreo. */
@Composable
fun TarjetaLimite(
    limite: LimiteEditable,
    mostrarError: Boolean,
    onTexto: (String) -> Unit,
    onSumar: (Int) -> Unit,
    onMonitoreo: (Boolean) -> Unit,
) {
    val fueraDeRango = mostrarError && limite.valor == null
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(limite.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("Monitorear", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(8.dp))
                Switch(checked = limite.monitoreoActivo, onCheckedChange = onMonitoreo)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalIconButton(onClick = { onSumar(-PASO_MINUTOS) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Restar $PASO_MINUTOS minutos")
                }
                OutlinedTextField(
                    value = limite.texto,
                    onValueChange = onTexto,
                    singleLine = true,
                    isError = fueraDeRango,
                    suffix = { Text("min") },
                    supportingText = if (fueraDeRango) {
                        { Text("Escribe un valor entre ${GestorLimites.MINIMO} y ${GestorLimites.MAXIMO} minutos") }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                FilledTonalIconButton(onClick = { onSumar(PASO_MINUTOS) }) {
                    Icon(Icons.Default.Add, contentDescription = "Sumar $PASO_MINUTOS minutos")
                }
            }
        }
    }
}
