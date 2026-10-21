package com.proyectointegrador.monitoreo.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proyectointegrador.monitoreo.MonitoreoApp
import com.proyectointegrador.monitoreo.configuracion.vinculacion.CampoPin
import com.proyectointegrador.monitoreo.ui.componentes.BotonPrincipal
import com.proyectointegrador.monitoreo.ui.componentes.EncabezadoMarca
import com.proyectointegrador.monitoreo.ui.theme.Marca
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
                error = "PIN incorrecto. Inténtelo de nuevo."
                pin = ""
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                EncabezadoMarca()
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(88.dp)
                    .background(Marca.celeste, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            }
            Text(
                "Ingrese su PIN para continuar",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                "Así podrá ver el resumen y cambiar los límites.",
                style = MaterialTheme.typography.bodyLarge,
                color = Marca.textoSecundario,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            CampoPin(
                valor = pin,
                onCambio = {
                    pin = it.filter { c -> c.isDigit() }.take(PinSeguro.LONGITUD)
                    error = null
                },
                etiqueta = "PIN",
                error = error,
            )
            BotonPrincipal("Entrar", onClick = ::verificar, habilitado = PinSeguro.esValido(pin))
            Spacer(Modifier.height(48.dp))
        }
    }
}
