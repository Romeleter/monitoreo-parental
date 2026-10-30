package com.proyectointegrador.monitoreo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.proyectointegrador.monitoreo.pausas.ServicioVigilancia
import com.proyectointegrador.monitoreo.ui.NavegacionApp
import com.proyectointegrador.monitoreo.ui.theme.TemaMonitoreo

/** La única actividad: dibuja las pantallas de Compose */
class MainActivity : ComponentActivity() {
    /** La pantalla de carga sigue mientras se consulta si ya hay perfil */
    private var cargando = true

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { cargando }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MonitoreoApp
        ServicioVigilancia.iniciar(this)

        setContent {
            TemaMonitoreo {
                // null mientras se consulta si ya hay perfil
                val yaVinculado by produceState<Boolean?>(initialValue = null) {
                    value = app.db.perfilDao().obtenerPerfil() != null
                }
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    yaVinculado?.let {
                        LaunchedEffect(Unit) { cargando = false }
                        NavegacionApp(yaVinculado = it)
                    }
                }
            }
        }
    }
}
