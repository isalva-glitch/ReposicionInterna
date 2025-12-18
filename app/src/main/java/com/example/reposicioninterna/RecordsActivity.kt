package com.example.reposicioninterna

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
        val records = dbHelper.getAllRecords()
        val adapter = RecordsAdapter(this, records)
        listView.adapter = adapter
    }
}
