package com.proyectointegrador.monitoreo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.proyectointegrador.monitoreo.pausas.ServicioVigilancia
import com.proyectointegrador.monitoreo.ui.NavegacionApp
import com.proyectointegrador.monitoreo.ui.theme.TemaMonitoreo

/** Única actividad de la app: dibuja las pantallas de Compose. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MonitoreoApp
        ServicioVigilancia.iniciar(this)

        setContent {
            TemaMonitoreo {
                // null mientras se consulta la base de datos para saber si ya hay un perfil.
                val yaVinculado by produceState<Boolean?>(initialValue = null) {
                    value = app.db.perfilDao().obtenerPerfil() != null
                }
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    yaVinculado?.let { NavegacionApp(yaVinculado = it) }
                }
            }
        }
    }
}
