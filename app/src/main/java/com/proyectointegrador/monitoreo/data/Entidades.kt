package com.proyectointegrador.monitoreo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * Clases de datos del diagrama de clases (sección 5.6 del documento).
 * Cada una es una tabla de la base de datos local (Room / SQLite).
 */

/** Perfil del menor que se monitorea (RF-10). */
@Entity(tableName = "perfil_menor")
data class PerfilMenor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val edad: Int,
    /** PIN del padre guardado como hash, nunca en texto plano. Protege el modo padre. */
    val pinPadreHash: String,
    val fechaVinculacion: Long = System.currentTimeMillis(),
)

/** Aplicación de video corto monitoreada y su límite diario (RF-03, RF-05, RF-22). */
@Entity(
    tableName = "app_monitoreada",
    foreignKeys = [
        ForeignKey(
            entity = PerfilMenor::class,
            parentColumns = ["id"],
            childColumns = ["perfilId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("perfilId")],
)
data class AppMonitoreada(
    /** Nombre del paquete Android, p. ej. "com.zhiliaoapp.musically" para TikTok. */
    @PrimaryKey val paquete: String,
    val perfilId: Long,
    val nombre: String,
    val limiteDiarioMin: Int,
    val monitoreoActivo: Boolean = true,
    val bloqueoManual: Boolean = false,
)

/** Minutos de uso de una app en un día (RF-01) y cuántas veces saltó el aviso (RF-24). */
@Entity(
    tableName = "registro_uso",
    foreignKeys = [
        ForeignKey(
            entity = AppMonitoreada::class,
            parentColumns = ["paquete"],
            childColumns = ["paquete"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["paquete", "fecha"], unique = true)],
)
data class RegistroUso(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paquete: String,
    /** Fecha en formato ISO "aaaa-mm-dd". */
    val fecha: String,
    val minutosUso: Int = 0,
    val activacionesAviso: Int = 0,
)

/** Cada vez que se muestra el aviso de pausa al niño (RF-13, RF-16). */
@Entity(
    tableName = "aviso_pausa",
    foreignKeys = [
        ForeignKey(
            entity = AppMonitoreada::class,
            parentColumns = ["paquete"],
            childColumns = ["paquete"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("paquete")],
)
data class AvisoPausa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paquete: String,
    val mensaje: String,
    val tiempoEsperaSeg: Int,
    val momentoMostrado: Long,
    /** Momento en que el niño pulsó "Entendido"; null si aún no lo ha reconocido. */
    val momentoReconocimiento: Long? = null,
    /**
     * Momento en que el aviso dejó de verse (por "Entendido" o porque el niño salió de la app).
     * Mientras el aviso está encima, Android sigue contando la app como abierta; ese tiempo
     * se descuenta del uso. null mientras el aviso sigue visible.
     */
    val momentoCierre: Long? = null,
)
