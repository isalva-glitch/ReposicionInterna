package com.example.reposicioninterna

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReposicionEntity::class], version = 2, exportSchema = true)
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
            return Room.databaseBuilder(
                context,
                ReposicionDatabase::class.java,
                "reposicion.db"
            )
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
        }
    }
}
