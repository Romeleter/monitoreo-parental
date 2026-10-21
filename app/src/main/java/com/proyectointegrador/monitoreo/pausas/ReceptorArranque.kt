package com.proyectointegrador.monitoreo.pausas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Vuelve a iniciar la vigilancia cuando el teléfono se enciende. */
class ReceptorArranque : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ServicioVigilancia.iniciar(context)
        }
    }
}
