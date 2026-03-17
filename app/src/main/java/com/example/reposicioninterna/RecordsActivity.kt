package com.example.reposicioninterna

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class RecordsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var btnVolver: Button
    private lateinit var dbHelper: ReposicionDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        listView = findViewById(R.id.lvRegistros)
        btnVolver = findViewById(R.id.btnVolver)
        dbHelper = ReposicionDbHelper(this)

        loadRecords()

        btnVolver.setOnClickListener { finish() }
    }

    private fun loadRecords() {
        val allRecords = dbHelper.getAllRecords()
        
        // Group by order number, sort groups by most recent activity, then flatten
        val sortedRecords = allRecords.groupBy { it.numeroPedido }
            .entries.sortedByDescending { entry ->
                entry.value.maxOfOrNull { it.timestamp } ?: 0L
            }
            .flatMap { entry -> 
                // Sort items within order by timestamp (oldest first or newest first? usually item 1, 2, 3...)
                entry.value.sortedBy { it.timestamp } 
            }

        val adapter = RecordsAdapter(this, sortedRecords) { numeroPedido ->
            // Load the selected order into MainActivity and close this screen
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_LOAD_PEDIDO, numeroPedido)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
        listView.adapter = adapter
    }
}

