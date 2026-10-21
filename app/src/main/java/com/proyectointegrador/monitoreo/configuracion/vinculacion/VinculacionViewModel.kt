package com.proyectointegrador.monitoreo.configuracion.vinculacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.withTransaction
import com.proyectointegrador.monitoreo.MonitoreoApp
import com.proyectointegrador.monitoreo.configuracion.AppsPredeterminadas
import com.proyectointegrador.monitoreo.configuracion.PinSeguro
import com.proyectointegrador.monitoreo.configuracion.limites.LimiteEditable
import com.proyectointegrador.monitoreo.configuracion.limites.conMonitoreo
import com.proyectointegrador.monitoreo.configuracion.limites.conTexto
import com.proyectointegrador.monitoreo.configuracion.limites.sumando
import com.proyectointegrador.monitoreo.configuracion.limites.todosValidos
import com.proyectointegrador.monitoreo.data.AppDatabase
import com.proyectointegrador.monitoreo.data.AppMonitoreada
import com.proyectointegrador.monitoreo.data.PerfilMenor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lo que el padre escribe en la pantalla 1 (Datos del niño/a). */
data class DatosMenorUi(
    val nombre: String = "",
    val edad: Int? = null,
    val pin: String = "",
    val pinConfirmacion: String = "",
    /** Los errores solo se muestran después de pulsar "Continuar" la primera vez. */
    val mostrarErrores: Boolean = false,
) {
    val errorNombre: String? get() = if (nombre.isBlank()) "Escriba el nombre del niño o niña" else null
    val errorEdad: String? get() = if (edad == null) "Elija la edad" else null
    val errorPin: String? get() = if (!PinSeguro.esValido(pin)) "El PIN debe tener 4 números" else null
    val errorConfirmacion: String? get() = if (pinConfirmacion != pin) "Los PIN no coinciden" else null

    val esValido: Boolean
        get() = listOf(errorNombre, errorEdad, errorPin, errorConfirmacion).all { it == null }
}

/** Flujo de vinculación del dispositivo (HU-05 / CU-01): datos → límites → permisos → éxito. */
class VinculacionViewModel(private val db: AppDatabase) : ViewModel() {

    private val _datos = MutableStateFlow(DatosMenorUi())
    val datos = _datos.asStateFlow()

    // Paso de límites (HU-02): los campos empiezan vacíos para que el padre elija cada límite.
    private val _limites = MutableStateFlow(
        AppsPredeterminadas.lista.map { (nombre, paquete) ->
            LimiteEditable(paquete = paquete, nombre = nombre, texto = "", monitoreoActivo = true)
        },
    )
    val limites = _limites.asStateFlow()

    /** Los errores de límites solo se muestran después de pulsar "Continuar". */
    private val _mostrarErroresLimites = MutableStateFlow(false)
    val mostrarErroresLimites = _mostrarErroresLimites.asStateFlow()

    private var vinculando = false

    fun cambiarNombre(valor: String) = _datos.update { it.copy(nombre = valor) }
    fun cambiarEdad(valor: Int) = _datos.update { it.copy(edad = valor) }
    fun cambiarPin(valor: String) = _datos.update { it.copy(pin = soloDigitos(valor)) }
    fun cambiarPinConfirmacion(valor: String) =
        _datos.update { it.copy(pinConfirmacion = soloDigitos(valor)) }

    /** Devuelve true si se puede pasar al paso de límites. */
    fun validarDatos(): Boolean {
        _datos.update { it.copy(mostrarErrores = true) }
        return _datos.value.esValido
    }

    fun cambiarTextoLimite(paquete: String, texto: String) = _limites.update { it.conTexto(paquete, texto) }
    fun sumarLimite(paquete: String, delta: Int) = _limites.update { it.sumando(paquete, delta) }
    fun cambiarMonitoreo(paquete: String, activo: Boolean) = _limites.update { it.conMonitoreo(paquete, activo) }

    /** Devuelve true si todos los límites están entre 1 y 300 y se puede pasar al paso de permisos. */
    fun validarLimites(): Boolean {
        _mostrarErroresLimites.value = true
        return _limites.value.todosValidos()
    }

    /**
     * Guarda el perfil y las apps monitoreadas. Según el diagrama de actividad,
     * esto ocurre solo cuando el permiso ya fue concedido.
     */
    fun vincular(alTerminar: () -> Unit) {
        if (vinculando) return
        vinculando = true
        val datos = _datos.value
        val limites = _limites.value
        viewModelScope.launch {
            db.withTransaction {
                val perfilId = db.perfilDao().insertar(
                    PerfilMenor(
                        nombre = datos.nombre.trim(),
                        edad = checkNotNull(datos.edad),
                        pinPadreHash = PinSeguro.hash(datos.pin),
                    ),
                )
                db.appMonitoreadaDao().insertarTodas(
                    limites.map {
                        AppMonitoreada(
                            paquete = it.paquete,
                            perfilId = perfilId,
                            nombre = it.nombre,
                            limiteDiarioMin = checkNotNull(it.valor),
                            monitoreoActivo = it.monitoreoActivo,
                        )
                    },
                )
            }
            alTerminar()
        }
    }

    private fun soloDigitos(valor: String) = valor.filter { it.isDigit() }.take(PinSeguro.LONGITUD)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MonitoreoApp
                VinculacionViewModel(app.db)
            }
        }
    }
}
