package com.proyectointegrador.monitoreo.configuracion.limites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.proyectointegrador.monitoreo.MonitoreoApp
import com.proyectointegrador.monitoreo.configuracion.GestorLimites
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LimitesViewModel(private val gestor: GestorLimites) : ViewModel() {

    private val _limites = MutableStateFlow<List<LimiteEditable>>(emptyList())
    val limites = _limites.asStateFlow()

    private val _mensajes = Channel<String>()
    val mensajes = _mensajes.receiveAsFlow()

    init {
        viewModelScope.launch {
            _limites.value = gestor.observarApps().first().map {
                LimiteEditable(it.paquete, it.nombre, it.limiteDiarioMin.toString(), it.monitoreoActivo)
            }
        }
    }

    fun cambiarTexto(paquete: String, texto: String) = _limites.update { it.conTexto(paquete, texto) }
    fun sumar(paquete: String, delta: Int) = _limites.update { it.sumando(paquete, delta) }
    fun cambiarMonitoreo(paquete: String, activo: Boolean) = _limites.update { it.conMonitoreo(paquete, activo) }

    fun guardar() {
        val lista = _limites.value
        viewModelScope.launch {
            if (!lista.todosValidos()) {
                _mensajes.send("Revise los valores marcados en rojo")
                return@launch
            }
            lista.forEach { gestor.guardarLimite(it.paquete, it.valor!!, it.monitoreoActivo) }
            _mensajes.send("Límites guardados")
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MonitoreoApp
                LimitesViewModel(app.gestorLimites)
            }
        }
    }
}
