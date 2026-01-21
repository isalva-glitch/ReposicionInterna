package com.example.reposicioninterna

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReposicionInternaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            StartupLog.log(
                this,
                "Fallo no controlado en hilo ${thread.name}",
                throwable
            )
            previousHandler?.uncaughtException(thread, throwable)
        }
        StartupLog.log(this, "Application onCreate")

        setupDailyBackup()
    }

    private fun setupDailyBackup() {
        // Calcular delay inicial hasta las 16:30
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        
        dueDate.set(Calendar.HOUR_OF_DAY, 16)
        dueDate.set(Calendar.MINUTE, 30)
        dueDate.set(Calendar.SECOND, 0)
        
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        
        val saveRequest = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag("db_backup")
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DAILY_BACKUP_1630",
            ExistingPeriodicWorkPolicy.KEEP, // Mantiene la existente si ya está programada
            saveRequest
        )
    }
}
