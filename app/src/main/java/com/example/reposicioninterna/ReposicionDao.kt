package com.example.reposicioninterna

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReposicionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ReposicionEntity): Long

    @Query("SELECT * FROM reposicion ORDER BY timestamp DESC")
    suspend fun getAll(): List<ReposicionEntity>

    @Query("SELECT COUNT(*) FROM reposicion WHERE fecha = :fecha AND numeroPedido = :numero")
    suspend fun countByFechaAndNumero(fecha: String, numero: String): Int

    @Query("SELECT COUNT(*) FROM reposicion")
    suspend fun countAll(): Int

    @Query("SELECT * FROM reposicion WHERE yaEsDvh = 1 ORDER BY timestamp DESC")
    suspend fun getAllDvh(): List<ReposicionEntity>
}
