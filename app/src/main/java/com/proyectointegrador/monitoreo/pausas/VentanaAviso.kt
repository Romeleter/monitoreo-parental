package com.proyectointegrador.monitoreo.pausas

import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.proyectointegrador.monitoreo.ui.theme.TemaMonitoreo

/** Dibuja el aviso a pantalla completa encima de cualquier app. Se usa desde el hilo principal */
class VentanaAviso(private val context: Context) {

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var vista: ComposeView? = null
    private var dueno: DuenoVentana? = null

    val visible: Boolean get() = vista != null

    fun mostrar(nombreApp: String, motivo: MotivoPausa, onEntendido: () -> Unit) {
        if (vista != null) return
        val nuevoDueno = DuenoVentana().also { it.iniciar() }
        val nuevaVista = ComposeView(context).apply {
            setViewTreeLifecycleOwner(nuevoDueno)
            setViewTreeSavedStateRegistryOwner(nuevoDueno)
            setContent {
                TemaMonitoreo {
                    PantallaAvisoPausa(nombreApp, motivo, SEGUNDOS_ESPERA, onEntendido)
                }
            }
        }
        val parametros = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.OPAQUE,
        )
        windowManager.addView(nuevaVista, parametros)
        vista = nuevaVista
        dueno = nuevoDueno
    }

    fun ocultar() {
        vista?.let { windowManager.removeView(it) }
        dueno?.destruir()
        vista = null
        dueno = null
    }

    companion object {
        /** Segundos que el niño debe esperar para tocar "Entendido" */
        const val SEGUNDOS_ESPERA = 7
    }
}

/**
 * Compose necesita un ciclo de vida. Las Activity lo traen, pero una ventana creada
 * desde un servicio no, así que se lo damos con esta clase.
 */
private class DuenoVentana : SavedStateRegistryOwner {
    private val registro = LifecycleRegistry(this)
    private val controlador = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = registro
    override val savedStateRegistry: SavedStateRegistry get() = controlador.savedStateRegistry

    fun iniciar() {
        controlador.performRestore(null)
        registro.currentState = Lifecycle.State.RESUMED
    }

    fun destruir() {
        registro.currentState = Lifecycle.State.DESTROYED
    }
}
