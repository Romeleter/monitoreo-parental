package com.proyectointegrador.monitoreo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Base de datos local. Todo se guarda solo en el dispositivo del menor. */
@Database(
    entities = [PerfilMenor::class, AppMonitoreada::class, RegistroUso::class, AvisoPausa::class],
    version = 2,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perfilDao(): PerfilDao
    abstract fun appMonitoreadaDao(): AppMonitoreadaDao
    abstract fun registroUsoDao(): RegistroUsoDao
    abstract fun avisoPausaDao(): AvisoPausaDao

    companion object {
        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "monitoreo.db")
                .addMigrations(MIGRACION_1_2)
                .build()

        /** Versión 2: se agrega aviso_pausa.momentoCierre. */
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE aviso_pausa ADD COLUMN momentoCierre INTEGER")
                db.execSQL(
                    "UPDATE aviso_pausa SET momentoCierre = " +
                        "COALESCE(momentoReconocimiento, momentoMostrado)",
                )
            }
        }
    }
}
