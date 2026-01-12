package com.example.reposicioninterna

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView

class SplashActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tvLoading: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        tvLoading = findViewById(R.id.tvLoading)
        progressBar = findViewById(R.id.progressBar)

        if (checkPermissions()) {
            startLoadingProcess()
        } else {
            requestPermissions()
        }
    }
    
    private fun checkPermissions(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val p1 = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val p2 = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            return p1 == PackageManager.PERMISSION_GRANTED && p2 == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1001
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        startLoadingProcess()
    }

    private fun startLoadingProcess() {
        StartupLog.log(this, "Splash: Iniciando proceso de carga REAL")
        
        Thread {
            try {
                updateProgress(10, "Verificando entorno...")
                Thread.sleep(300)
                
                updateProgress(30, "Inicializando logs...")
                StartupLog.log(this, "Splash: Logs OK")
                Thread.sleep(300)
                
                updateProgress(50, "Cargando maestros...")
                // REAL LOAD 
                MasterDataManager.load(this)
                StartupLog.log(this, "Splash: Maestros cargados")
                Thread.sleep(500)
                
                updateProgress(80, "Verificando base de datos...")
                // REAL DB CHECK 
                ReposicionRepository.getInstance(this)
                StartupLog.log(this, "Splash: Base de datos abierta OK")
                Thread.sleep(500)
                
                updateProgress(90, "Preparando interfaz...")
                Thread.sleep(500) // Small pause to force update UI
                
                updateProgress(100, "Iniciando...")
                Thread.sleep(200)
                
                handler.post { goToMain() }
            } catch (t: Throwable) {
                StartupLog.log(this, "Splash: ERROR FATAL en carga", t)
                handler.post {
                     tvLoading.text = "ERROR: ${t.message}"
                     progressBar.indeterminateDrawable?.setColorFilter(android.graphics.Color.RED, android.graphics.PorterDuff.Mode.SRC_IN)
                }
            }
        }.start()
    }

    private fun updateProgress(progress: Int, message: String) {
        handler.post {
            progressBar.progress = progress
            tvLoading.text = "$message $progress%"
        }
    }

    private fun goToMain() {
        StartupLog.log(this, "Splash: Abriendo MainActivity (Rebuild)")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
