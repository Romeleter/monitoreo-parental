package com.proyectointegrador.monitoreo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// Consultas de cada tabla. Las que devuelven Flow se actualizan solas cuando cambian los datos

@Dao
interface PerfilDao {
    @Query("SELECT * FROM perfil_menor LIMIT 1")
    fun observarPerfil(): Flow<PerfilMenor?>

    @Query("SELECT * FROM perfil_menor LIMIT 1")
    suspend fun obtenerPerfil(): PerfilMenor?

    @Insert
    suspend fun insertar(perfil: PerfilMenor): Long
}

@Dao
interface AppMonitoreadaDao {
    @Query("SELECT * FROM app_monitoreada ORDER BY nombre")
    fun observarTodas(): Flow<List<AppMonitoreada>>

    @Query("SELECT * FROM app_monitoreada WHERE paquete = :paquete")
    suspend fun obtener(paquete: String): AppMonitoreada?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodas(apps: List<AppMonitoreada>)

    @Query("UPDATE app_monitoreada SET bloqueoManual = :bloqueada WHERE paquete = :paquete")
    suspend fun actualizarBloqueo(paquete: String, bloqueada: Boolean)

    @Query(
        "UPDATE app_monitoreada SET limiteDiarioMin = :limite, monitoreoActivo = :activo " +
            "WHERE paquete = :paquete",
    )
    suspend fun actualizarConfiguracion(paquete: String, limite: Int, activo: Boolean)
}

@Dao
interface RegistroUsoDao {
    @Query("SELECT * FROM registro_uso WHERE fecha = :fecha")
    fun observarDelDia(fecha: String): Flow<List<RegistroUso>>

    /** Registros entre dos fechas, ambas incluidas */
    @Query("SELECT * FROM registro_uso WHERE fecha BETWEEN :desde AND :hasta")
    fun observarEntre(desde: String, hasta: String): Flow<List<RegistroUso>>

    @Query(
        "UPDATE registro_uso SET minutosUso = :minutos, limiteMin = :limite " +
            "WHERE paquete = :paquete AND fecha = :fecha",
    )
    suspend fun actualizarMinutos(paquete: String, fecha: String, minutos: Int, limite: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(registro: RegistroUso)

    /** Crea el registro del día o actualiza el que ya existe */
    @Transaction
    suspend fun guardarMinutos(paquete: String, fecha: String, minutos: Int, limite: Int) {
        if (actualizarMinutos(paquete, fecha, minutos, limite) == 0) {
            insertar(RegistroUso(paquete = paquete, fecha = fecha, minutosUso = minutos, limiteMin = limite))
        }
    }

    @Query(
        "UPDATE registro_uso SET activacionesAviso = activacionesAviso + 1 " +
            "WHERE paquete = :paquete AND fecha = :fecha",
    )
    suspend fun incrementarActivaciones(paquete: String, fecha: String): Int

    /** Suma un aviso al registro del día */
    @Transaction
    suspend fun sumarActivacion(paquete: String, fecha: String) {
        if (incrementarActivaciones(paquete, fecha) == 0) {
            insertar(RegistroUso(paquete = paquete, fecha = fecha, activacionesAviso = 1))
        }
    }
}

@Dao
interface AvisoPausaDao {
    @Query("SELECT * FROM aviso_pausa WHERE momentoMostrado >= :desde ORDER BY momentoMostrado DESC")
    fun observarDesde(desde: Long): Flow<List<AvisoPausa>>

    @Insert
    suspend fun insertar(aviso: AvisoPausa): Long

    /** Guarda la hora en que el niño tocó "Entendido" */
    @Query("UPDATE aviso_pausa SET momentoReconocimiento = :momento WHERE id = :id")
    suspend fun marcarReconocido(id: Long, momento: Long)

    @Query("UPDATE aviso_pausa SET momentoCierre = :momento WHERE id = :id")
    suspend fun marcarCerrado(id: Long, momento: Long)

    /** Si el servicio se detuvo con un aviso abierto, lo cierra con la última hora conocida */
    @Query(
        "UPDATE aviso_pausa SET momentoCierre = COALESCE(momentoReconocimiento, momentoMostrado) " +
            "WHERE momentoCierre IS NULL",
    )
    suspend fun cerrarPendientes()

    /** Avisos que se vieron en algún momento entre [desde] y [hasta] */
    @Query(
        "SELECT * FROM aviso_pausa WHERE momentoMostrado < :hasta " +
            "AND (momentoCierre IS NULL OR momentoCierre > :desde)",
    )
    suspend fun obtenerEntre(desde: Long, hasta: Long): List<AvisoPausa>
}
