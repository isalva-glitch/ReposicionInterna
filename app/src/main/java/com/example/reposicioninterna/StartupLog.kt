package com.example.reposicioninterna

import android.content.Context
import android.util.Log
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StartupLog {
    private const val FILE_NAME = "startup_log.txt"
    private const val TAG = "StartupLog"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val lock = Any()

    fun log(context: Context, message: String) {
        val entry = "${dateFormat.format(Date())} - $message\n"
        try {
            synchronized(lock) {
                context.openFileOutput(FILE_NAME, Context.MODE_APPEND).use { output ->
                    output.write(entry.toByteArray())
                }
            }
        } catch (error: IOException) {
            Log.e(TAG, "No se pudo escribir el registro de inicio.", error)
        }
    }
}
