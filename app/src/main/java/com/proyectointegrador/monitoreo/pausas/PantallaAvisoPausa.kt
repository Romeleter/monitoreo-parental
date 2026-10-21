package com.proyectointegrador.monitoreo.pausas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Por qué se muestra el aviso: se llegó al límite (HU-06) o el padre bloqueó la app (HU-03). */
enum class MotivoPausa { LIMITE, BLOQUEO_MANUAL }

private val FondoAviso = Color(0xFFE0F2F1)

/**
 * Pantalla 6 — Aviso de pausa. Es la única pantalla que ve el niño (RF-13, RF-14, RF-15).
 * Ocupa toda la pantalla y no tiene opciones para cerrarla ni cambiar la configuración.
 */
@Composable
fun PantallaAvisoPausa(
    nombreApp: String,
    motivo: MotivoPausa,
    segundosEspera: Int,
    onEntendido: () -> Unit,
) {
    var restantes by remember { mutableIntStateOf(segundosEspera) }

    // Cuenta regresiva: el botón se habilita solo cuando llega a cero (HU-07).
    LaunchedEffect(Unit) {
        while (restantes > 0) {
            delay(1_000)
            restantes--
        }
    }

    val mensaje = when (motivo) {
        MotivoPausa.LIMITE -> "Ya usaste $nombreApp todo el tiempo de hoy."
        MotivoPausa.BLOQUEO_MANUAL -> "Tu papá o tu mamá pausó $nombreApp por ahora."
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(FondoAviso)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                Modifier
                    .size(160.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.SelfImprovement,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(110.dp),
                )
            }
            Text(
                "¡Hora de una pausa!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                mensaje,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                "Descansa tus ojos, estírate o juega a otra cosa. 🌳",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Button(
                onClick = onEntendido,
                enabled = restantes == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            ) {
                Text(
                    if (restantes > 0) "Espera $restantes segundos…" else "Entendido",
                    fontSize = 20.sp,
                )
            }
        }
    }
}
