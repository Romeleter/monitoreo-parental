package com.proyectointegrador.monitoreo.configuracion.limites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyectointegrador.monitoreo.configuracion.GestorLimites
import com.proyectointegrador.monitoreo.ui.componentes.BotonPrincipal
import com.proyectointegrador.monitoreo.ui.componentes.EncabezadoMarca
import com.proyectointegrador.monitoreo.ui.componentes.IconoApp
import com.proyectointegrador.monitoreo.ui.componentes.TarjetaBlanca
import com.proyectointegrador.monitoreo.ui.theme.Marca

/** Pantalla 4 — Límites de tiempo (HU-02 / CU-03). */
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
            Column {
                EncabezadoMarca(onAtras = onAtras)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        // El botón de guardar queda fijo abajo para no tener que bajar hasta el final.
        bottomBar = {
            Box(
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                BotonPrincipal("Guardar límites", onClick = vm::guardar, icono = Icons.Outlined.Save)
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text("Límites de tiempo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            item { Instrucciones() }
            items(limites, key = { it.paquete }) { limite ->
                TarjetaLimite(
                    limite = limite,
                    mostrarError = true,
                    onTexto = { vm.cambiarTexto(limite.paquete, it) },
                    onSumar = { vm.sumar(limite.paquete, it) },
                    onMonitoreo = { vm.cambiarMonitoreo(limite.paquete, it) },
                )
            }
        }
    }
}

@Composable
private fun Instrucciones() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Marca.celeste, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Instrucciones", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Elija cuántos minutos al día se puede usar cada app, entre ${GestorLimites.MINIMO} y " +
                    "${GestorLimites.MAXIMO}. Con el interruptor puede pausar el monitoreo de una app sin perder su límite.",
                style = MaterialTheme.typography.bodyMedium,
                color = Marca.textoSecundario,
            )
        }
    }
}

/** Tarjeta de una app: ícono, minutos al día, botones − y + e interruptor de monitoreo (RF-22). */
@Composable
fun TarjetaLimite(
    limite: LimiteEditable,
    mostrarError: Boolean,
    onTexto: (String) -> Unit,
    onSumar: (Int) -> Unit,
    onMonitoreo: (Boolean) -> Unit,
) {
    val fueraDeRango = mostrarError && limite.valor == null
    TarjetaBlanca {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconoApp(limite.paquete, tamano = 48.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(limite.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (limite.monitoreoActivo) "Monitoreo activo" else "Monitoreo en pausa",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (limite.monitoreoActivo) Marca.textoSecundario else Marca.ambar,
                        )
                    }
                    Switch(checked = limite.monitoreoActivo, onCheckedChange = onMonitoreo)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = limite.texto,
                        onValueChange = onTexto,
                        singleLine = true,
                        isError = fueraDeRango,
                        placeholder = { Text("—", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color.White,
                            unfocusedBorderColor = Marca.bordeTarjeta,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(92.dp),
                    )
                    Text("minutos al día", style = MaterialTheme.typography.bodyMedium, color = Marca.textoSecundario)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (fueraDeRango) "Escriba un valor entre ${GestorLimites.MINIMO} y ${GestorLimites.MAXIMO} minutos" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                    BotonPaso(Icons.Default.Remove, "Restar $PASO_MINUTOS minutos") { onSumar(-PASO_MINUTOS) }
                    BotonPaso(Icons.Default.Add, "Sumar $PASO_MINUTOS minutos") { onSumar(PASO_MINUTOS) }
                }
            }
        }
    }
}

@Composable
private fun BotonPaso(icono: androidx.compose.ui.graphics.vector.ImageVector, descripcion: String, onClick: () -> Unit) {
    OutlinedIconButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
    ) {
        Icon(icono, contentDescription = descripcion, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
    }
}
