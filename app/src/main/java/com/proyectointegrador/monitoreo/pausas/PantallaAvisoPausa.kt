package com.proyectointegrador.monitoreo.pausas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectointegrador.monitoreo.R
import com.proyectointegrador.monitoreo.ui.theme.Nunito
import kotlinx.coroutines.delay

/** Por qué sale el aviso: se llegó al límite o el padre bloqueó la app */
enum class MotivoPausa { LIMITE, BLOQUEO_MANUAL }

private val FondoAviso = Color(0xFFFAF8F4)
private val TintaAviso = Color(0xFF1F2A37)
private val TextoAviso = Color(0xFF556170)
private val BotonEsperaFondo = Color(0xFFDDE3E8)
private val BotonEsperaTexto = Color(0xFF3F4A57)

/** Aviso de pausa: la única pantalla que ve el niño. No tiene opciones para cerrarla ni cambiar nada */
@Composable
fun PantallaAvisoPausa(
    nombreApp: String,
    motivo: MotivoPausa,
    segundosEspera: Int,
    onEntendido: () -> Unit,
) {
    var restantes by remember { mutableIntStateOf(segundosEspera) }

    // Cuenta regresiva: el botón se habilita cuando llega a cero
    LaunchedEffect(Unit) {
        while (restantes > 0) {
            delay(1_000)
            restantes--
        }
    }

    val mensaje = when (motivo) {
        MotivoPausa.LIMITE -> "Ya viste mucho $nombreApp por hoy."
        MotivoPausa.BLOQUEO_MANUAL -> "Tu papá o tu mamá pausó $nombreApp por ahora."
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(FondoAviso)
            .padding(horizontal = 28.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                painterResource(R.drawable.ilus_pausa),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "¡Hora de una pausa!",
                style = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 38.sp),
                color = TintaAviso,
                textAlign = TextAlign.Center,
            )
            Text(
                "$mensaje Respira hondo, mira a tu alrededor y estira el cuerpo.",
                style = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 29.sp),
                color = TextoAviso,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            BotonEntendido(restantes, segundosEspera, onEntendido)
        }
    }
}

@Composable
private fun BotonEntendido(restantes: Int, segundosEspera: Int, onEntendido: () -> Unit) {
    // El anillo se llena poco a poco mientras pasan los segundos
    val avance by animateFloatAsState(
        targetValue = 1f - restantes.toFloat() / segundosEspera,
        animationSpec = tween(durationMillis = 1_000, easing = LinearEasing),
        label = "avanceEspera",
    )
    val estiloBoton = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)

    Button(
        onClick = onEntendido,
        enabled = restantes == 0,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = BotonEsperaFondo,
            disabledContentColor = BotonEsperaTexto,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
    ) {
        if (restantes > 0) {
            CircularProgressIndicator(
                progress = { avance },
                color = BotonEsperaTexto,
                trackColor = BotonEsperaTexto.copy(alpha = 0.18f),
                strokeWidth = 2.5.dp,
                gapSize = 0.dp,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                if (restantes == 1) "Espera 1 segundo…" else "Espera $restantes segundos…",
                style = estiloBoton,
            )
        } else {
            Text("Entendido", style = estiloBoton)
        }
    }
}
