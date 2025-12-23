package com.example.reposicioninterna

import android.content.Context
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
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
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
                    .also { INSTANCE = it }
            }
        }
    }
}
