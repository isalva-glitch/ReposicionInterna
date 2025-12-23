package com.example.reposicioninterna

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReposicionRepository private constructor(context: Context) {
    private val dao = ReposicionDatabase.getInstance(context).reposicionDao()

    suspend fun save(record: ReposicionRecord): Long = withContext(Dispatchers.IO) {
        dao.insert(ReposicionEntity.from(record))
    }

    suspend fun isDuplicated(fecha: String, numero: String): Boolean = withContext(Dispatchers.IO) {
        dao.countByFechaAndNumero(fecha, numero) > 0
    }

    suspend fun getAll(): List<ReposicionRecord> = withContext(Dispatchers.IO) {
        dao.getAll().map { it.toDomain() }
    }

    suspend fun getAllDvh(): List<ReposicionRecord> = withContext(Dispatchers.IO) {
        dao.getAllDvh().map { it.toDomain() }
    }

    suspend fun countAll(): Int = withContext(Dispatchers.IO) { dao.countAll() }

    companion object {
        @Volatile
        private var INSTANCE: ReposicionRepository? = null

        fun getInstance(context: Context): ReposicionRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ReposicionRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
