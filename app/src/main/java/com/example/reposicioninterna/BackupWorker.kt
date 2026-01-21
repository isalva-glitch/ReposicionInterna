package com.example.reposicioninterna

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            performDatabaseBackup()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun performDatabaseBackup() {
        val dbFile = applicationContext.getDatabasePath("reposicion.db")
        if (!dbFile.exists()) return

        val backupDir = File(applicationContext.getExternalFilesDir(null), "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        // Backup principal (sobreescribe el último)
        val backupFile = File(backupDir, "reposicion_backup.db")
        
        dbFile.inputStream().use { input ->
            backupFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // Backup con fecha (historial)
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val historyFile = File(backupDir, "reposicion_$dateStr.db")
        backupFile.copyTo(historyFile, overwrite = true)
    }
}
