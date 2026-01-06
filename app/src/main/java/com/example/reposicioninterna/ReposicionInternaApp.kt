package com.example.reposicioninterna

import android.app.Application

class ReposicionInternaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        StartupLog.log(this, "Application onCreate")
    }
}
