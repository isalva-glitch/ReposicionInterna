package com.example.reposicioninterna

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StartupLog {
    private const val FILE_NAME = "startup_log.txt"
    private const val TAG = "StartupLog"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    @Synchronized
    fun log(context: Context, message: String, throwable: Throwable? = null) {
        runCatching {
            val entry = buildString {
                append(dateFormat.format(Date()))
                append(" - ")
                append(message)
                append('\n')
                if (throwable != null) {
                    val writer = StringWriter()
                    throwable.printStackTrace(PrintWriter(writer))
                    append(writer.toString())
                    append('\n')
                }
            }

            val appContext = context.applicationContext
            // Priorizamos almacenamiento interno para evitar bloqueos o problemas de permisos
            val directory = resolveDirectory(appContext.filesDir)
                ?: resolveDirectory(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS))

            if (directory == null) {
                Log.e(TAG, "No se pudo acceder a ningún directorio para el log.")
                return
            }
            File(directory, FILE_NAME).appendText(entry)
        }.onFailure { t ->
            Log.e(TAG, "No se pudo escribir el registro de inicio.", t)
        }
    }

    private fun resolveDirectory(directory: File?): File? {
        if (directory == null) return null
        return if (directory.exists() || directory.mkdirs()) directory else null
    }
}
