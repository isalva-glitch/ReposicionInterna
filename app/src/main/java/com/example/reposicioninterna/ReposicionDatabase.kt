package com.example.reposicioninterna

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.Locale

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
            val appContext = context.applicationContext
            preemptiveSchemaCheck(appContext)

            return try {
                createAndValidateDatabase(appContext)
            } catch (error: Exception) {
                if (shouldResetDatabase(error)) {
                    Log.w(
                        "ReposicionDatabase",
                        "Se detectó base de datos incompatible/corrupta, recreando",
                        error
                    )
                    deleteLegacyDatabase(appContext)
                    createAndValidateDatabase(appContext)
                } else {
                    throw error
                }
            }
        }

        private fun preemptiveSchemaCheck(context: Context) {
            val dbFile = context.getDatabasePath("reposicion.db")
            if (!dbFile.exists()) return

            val isValid = try {
                SQLiteDatabase.openDatabase(
                    dbFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                ).use { db ->
                    val tableInfo = db.rawQuery("PRAGMA table_info(`reposicion`)", null)
                    val columnNames = mutableSetOf<String>()
                    tableInfo.use { cursor ->
                        while (cursor.moveToNext()) {
                            val nameIndex = cursor.getColumnIndex("name")
                            if (nameIndex >= 0) {
                                columnNames.add(cursor.getString(nameIndex))
                            }
                        }
                    }

                    val expectedColumns = setOf(
                        "id",
                        "fecha",
                        "numeroPedido",
                        "responsable",
                        "sector",
                        "material",
                        "alto",
                        "ancho",
                        "cara1",
                        "cara2",
                        "motivo",
                        "pulidoCara1",
                        "templadoCara1",
                        "pulidoCara2",
                        "templadoCara2",
                        "yaEsDvh",
                        "origenCorte",
                        "pdfPath",
                        "timestamp"
                    )

                    columnNames.isNotEmpty() && columnNames == expectedColumns
                }
            } catch (error: Exception) {
                Log.w("ReposicionDatabase", "Error leyendo esquema heredado", error)
                false
            }

            if (!isValid) {
                Log.w(
                    "ReposicionDatabase",
                    "Base de datos heredada incompatible detectada antes de inicializar Room; se recreará"
                )
                deleteLegacyDatabase(context)
            }
        }

        private fun deleteLegacyDatabase(context: Context) {
            context.deleteDatabase("reposicion.db")
            val dbDir = context.getDatabasePath("reposicion.db").parentFile ?: return
            listOf("reposicion.db-shm", "reposicion.db-wal").forEach { suffix ->
                val file = dbDir.resolve(suffix)
                if (file.exists()) file.delete()
            }
        }

        private fun createAndValidateDatabase(context: Context): ReposicionDatabase {
            val database = databaseBuilder(context)
            try {
                // Fuerza apertura temprana para detectar DB heredada/corrupta antes de usar DAO.
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
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
        }

        private fun shouldResetDatabase(error: Exception): Boolean {
            if (error is SQLiteException) return true

            val message = error.message?.lowercase(Locale.getDefault()) ?: return false
            return message.contains("cannot verify the data integrity") ||
                    message.contains("file is not a database") ||
                    message.contains("room cannot verify") ||
                    message.contains("no such table") ||
                    (message.contains("expected") && message.contains("found")) ||
                    message.contains("has a schema mismatch") ||
                    message.contains("mismatched columns") ||
                    message.contains("has no column named")
        }
    }
}
