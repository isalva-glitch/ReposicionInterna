package com.example.reposicioninterna

import android.app.Application

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
    }
}
