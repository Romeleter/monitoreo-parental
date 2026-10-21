package com.proyectointegrador.monitoreo.pausas

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Permiso "Mostrar sobre otras apps" (SYSTEM_ALERT_WINDOW). Lo necesita el aviso de pausa
 * para aparecer encima de TikTok, Instagram o YouTube (RF-13).
 */
object PermisoSuperposicion {

    fun estaConcedido(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun abrirAjustes(context: Context) {
        val directo = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            .setData(Uri.parse("package:${context.packageName}"))
        try {
            context.startActivity(directo)
        } catch (_: ActivityNotFoundException) {
            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }
    }
}
