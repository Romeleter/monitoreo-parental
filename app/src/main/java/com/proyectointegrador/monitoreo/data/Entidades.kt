package com.proyectointegrador.monitoreo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Tablas de la base de datos (diagrama de clases, sección 5.6 del documento)

/** Perfil del niño o niña */
@Entity(tableName = "perfil_menor")
data class PerfilMenor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val edad: Int,
    /** Hash del PIN del padre, nunca el PIN en sí */
    val pinPadreHash: String,
    val fechaVinculacion: Long = System.currentTimeMillis(),
)

/** App de video corto que se monitorea y su límite diario */
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
    /** Paquete de Android, por ejemplo "com.zhiliaoapp.musically" para TikTok */
    @PrimaryKey val paquete: String,
    val perfilId: Long,
    val nombre: String,
    val limiteDiarioMin: Int,
    val monitoreoActivo: Boolean = true,
    val bloqueoManual: Boolean = false,
)

/** Minutos de uso de una app en un día y veces que salió el aviso */
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
    /** Fecha como "aaaa-mm-dd" */
    val fecha: String,
    val minutosUso: Int = 0,
    val activacionesAviso: Int = 0,
    /** Límite de ese día, para que el historial no cambie si el padre lo modifica después */
    val limiteMin: Int? = null,
)

/** Un aviso de pausa mostrado al niño */
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
    /** Cuándo el niño tocó "Entendido"; null si todavía no */
    val momentoReconocimiento: Long? = null,
    /**
     * Cuándo dejó de verse el aviso. Mientras está encima, Android sigue contando
     * la app como abierta, y ese tiempo se descuenta del uso.
     */
    val momentoCierre: Long? = null,
)
