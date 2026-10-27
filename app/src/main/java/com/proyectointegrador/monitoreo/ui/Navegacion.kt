package com.proyectointegrador.monitoreo.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.proyectointegrador.monitoreo.configuracion.BloqueoModoPadre
import com.proyectointegrador.monitoreo.configuracion.PantallaPin
import com.proyectointegrador.monitoreo.configuracion.limites.PantallaLimites
import com.proyectointegrador.monitoreo.configuracion.vinculacion.PantallaDatosMenor
import com.proyectointegrador.monitoreo.configuracion.vinculacion.PantallaLimitesIniciales
import com.proyectointegrador.monitoreo.configuracion.vinculacion.PantallaPermiso
import com.proyectointegrador.monitoreo.configuracion.vinculacion.PantallaVinculacionExitosa
import com.proyectointegrador.monitoreo.configuracion.vinculacion.VinculacionViewModel
import com.proyectointegrador.monitoreo.pausas.ServicioVigilancia
import com.proyectointegrador.monitoreo.reportes.PantallaHistorial
import com.proyectointegrador.monitoreo.reportes.PantallaResumen

/** Nombres de las pantallas de la app. */
object Rutas {
    const val DATOS = "datos"
    const val LIMITES_INICIALES = "limites_iniciales"
    const val PERMISO = "permiso"
    const val EXITO = "exito"
    const val PIN = "pin"
    const val RESUMEN = "resumen"
    const val LIMITES = "limites"
    const val HISTORIAL = "historial"

    /** Pantallas del modo padre: se bloquean con PIN al salir de la app. */
    val MODO_PADRE = setOf(RESUMEN, LIMITES, HISTORIAL)
}

/**
 * Mapa de navegación. Si el dispositivo aún no está vinculado empieza en la pantalla 1;
 * si ya lo está, pide el PIN del padre.
 */
@Composable
fun NavegacionApp(yaVinculado: Boolean) {
    val nav = rememberNavController()
    val context = LocalContext.current
    // Compartido por las tres pantallas de vinculación para no perder los datos entre pasos.
    val vinculacion: VinculacionViewModel = viewModel(factory = VinculacionViewModel.Factory)
    val actividad = LocalActivity.current

    // Si la app pasa a segundo plano estando en el modo padre, al volver se pide el PIN otra vez.
    var pedirPinAlVolver by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        val enModoPadre = nav.currentDestination?.route in Rutas.MODO_PADRE
        // Girar la pantalla también detiene la actividad un instante: eso no cuenta como salir.
        val girando = actividad?.isChangingConfigurations == true
        if (enModoPadre && !girando && !BloqueoModoPadre.consumirSalidaPermitida()) {
            pedirPinAlVolver = true
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        if (pedirPinAlVolver) {
            pedirPinAlVolver = false
            nav.navigate(Rutas.PIN) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }

    NavHost(nav, startDestination = if (yaVinculado) Rutas.PIN else Rutas.DATOS) {
        composable(Rutas.DATOS) {
            PantallaDatosMenor(vinculacion, onContinuar = { nav.navigate(Rutas.PERMISO) })
        }
        composable(Rutas.PERMISO) {
            PantallaPermiso(
                // La pantalla de permisos se quita de la pila: si el padre vuelve atrás desde
                // los límites llega a sus datos, en vez de rebotar en los permisos ya concedidos.
                onConcedido = { nav.navigate(Rutas.LIMITES_INICIALES) { popUpTo(Rutas.PERMISO) { inclusive = true } } },
            )
        }
        composable(Rutas.LIMITES_INICIALES) {
            PantallaLimitesIniciales(
                vinculacion,
                onContinuar = {
                    vinculacion.vincular {
                        ServicioVigilancia.iniciar(context)
                        nav.navigate(Rutas.EXITO) { popUpTo(Rutas.DATOS) { inclusive = true } }
                    }
                },
            )
        }
        composable(Rutas.EXITO) {
            val datos by vinculacion.datos.collectAsStateWithLifecycle()
            PantallaVinculacionExitosa(
                nombre = datos.nombre.trim(),
                onIrAlPanel = { nav.navigate(Rutas.RESUMEN) { popUpTo(Rutas.EXITO) { inclusive = true } } },
            )
        }
        composable(Rutas.PIN) {
            PantallaPin(onCorrecto = { nav.navigate(Rutas.RESUMEN) { popUpTo(Rutas.PIN) { inclusive = true } } })
        }
        composable(Rutas.RESUMEN) {
            PantallaResumen(
                onConfigurarLimites = { nav.navigate(Rutas.LIMITES) },
                onVerHistorial = { nav.navigate(Rutas.HISTORIAL) },
            )
        }
        composable(Rutas.LIMITES) {
            PantallaLimites(onAtras = { nav.popBackStack() })
        }
        composable(Rutas.HISTORIAL) {
            PantallaHistorial(onAtras = { nav.popBackStack() })
        }
    }
}
