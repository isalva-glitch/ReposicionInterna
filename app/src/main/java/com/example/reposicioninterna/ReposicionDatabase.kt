package com.example.reposicioninterna

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReposicionEntity::class], version = 1, exportSchema = true)
abstract class ReposicionDatabase : RoomDatabase() {
    abstract fun reposicionDao(): ReposicionDao

    companion object {
        @Volatile
        private var INSTANCE: ReposicionDatabase? = null

        fun getInstance(context: Context): ReposicionDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext)
                    .also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): ReposicionDatabase {
            return try {
                createAndValidateDatabase(context)
            } catch (error: Exception) {
                if (shouldResetDatabase(error)) {
                    Log.w(
                        "ReposicionDatabase",
                        "Se detectó base de datos incompatible/corrupta, recreando",
                        error
                    )
                    context.deleteDatabase("reposicion.db")
                    createAndValidateDatabase(context)
                } else {
                    throw error
                }
            }
        }

        private fun createAndValidateDatabase(context: Context): ReposicionDatabase {
            val database = databaseBuilder(context)
            try {
                // Abrimos la base apenas se crea para detectar problemas en dispositivos con
                // archivos heredados de SQLiteOpenHelper antes de que el DAO la use.
                database.openHelper.writableDatabase
            } catch (error: Exception) {
                database.close()
                throw error
            }
            return database
        }

        private fun databaseBuilder(context: Context): ReposicionDatabase {
            return Room.databaseBuilder(
                context,
                ReposicionDatabase::class.java,
                "reposicion.db"
            )
                // En tablets estaba instalada la versión anterior basada en SQLiteOpenHelper
                // (misma base de datos "reposicion.db" pero con otro esquema). Room fallaba
                // al validar la integridad y la app no iniciaba. Preferimos recrear la base
                // de datos si el esquema no coincide, para que la app vuelva a abrir.
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
        }

        private fun shouldResetDatabase(error: Exception): Boolean {
            if (error is SQLiteException) return true

            val message = error.message ?: return false
            return message.contains("cannot verify the data integrity", ignoreCase = true) ||
                message.contains("file is not a database", ignoreCase = true) ||
                message.contains("room cannot verify", ignoreCase = true) ||
                message.contains("no such table", ignoreCase = true)
        }
    }
}
