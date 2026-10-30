package com.proyectointegrador.monitoreo.deteccion

import android.Manifest
import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings

/**
 * Permiso "Acceso a datos de uso". No se puede pedir con un diálogo normal:
 * el padre debe activarlo en los ajustes de Android.
 */
object PermisoUso {

    @Suppress("DEPRECATION")
    fun estaConcedido(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val modo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        return if (modo == AppOpsManager.MODE_DEFAULT) {
            context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            modo == AppOpsManager.MODE_ALLOWED
        }
    }

    /** Abre los ajustes donde se concede el permiso */
    fun abrirAjustes(context: Context) {
        val directo = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            .setData(Uri.parse("package:${context.packageName}"))
        try {
            context.startActivity(directo)
        } catch (_: ActivityNotFoundException) {
            // Algunos fabricantes no dejan ir directo a la app: se abre la lista general
            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }
}
