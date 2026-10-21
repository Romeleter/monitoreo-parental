package com.proyectointegrador.monitoreo.configuracion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectointegrador.monitoreo.MonitoreoApp
import com.proyectointegrador.monitoreo.configuracion.vinculacion.CampoPin
import kotlinx.coroutines.launch

/** Entrada al modo padre: pide el PIN creado durante la vinculación. */
@Composable
fun PantallaPin(onCorrecto: () -> Unit) {
    val app = LocalContext.current.applicationContext as MonitoreoApp
    val scope = rememberCoroutineScope()
    var pin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun verificar() {
        scope.launch {
            val perfil = app.db.perfilDao().obtenerPerfil()
            if (perfil != null && PinSeguro.coincide(pin, perfil.pinPadreHash)) {
                onCorrecto()
            } else {
                error = "PIN incorrecto"
                pin = ""
            }
        }
    }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
            )
            Text("Modo padre", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Escribe tu PIN para ver el resumen y cambiar los límites.")
            CampoPin(
                valor = pin,
                onCambio = {
                    pin = it.filter { c -> c.isDigit() }.take(PinSeguro.LONGITUD)
                    error = null
                },
                etiqueta = "PIN",
                error = error,
            )
            Button(
                onClick = ::verificar,
                enabled = PinSeguro.esValido(pin),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Entrar") }
        }
    }
}
