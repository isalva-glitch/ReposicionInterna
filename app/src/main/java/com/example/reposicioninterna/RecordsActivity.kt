package com.example.reposicioninterna

import android.os.Bundle

import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecordsActivity : AppCompatActivity() {

    private lateinit var dbHelper: ReposicionDbHelper
    private lateinit var lvRegistros: ListView
    private lateinit var btnVolver: Button
    private lateinit var tvEmpty: TextView
    private lateinit var tvTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        dbHelper = ReposicionDbHelper(this)

        lvRegistros = findViewById(R.id.lvRegistros)
        btnVolver = findViewById(R.id.btnVolver)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvTotal = findViewById(R.id.tvTotal)

        val recordLimit = 20
        val registros = dbHelper.getRecentRecords(recordLimit)
        val adapter = RecordsAdapter(this, registros)


        lvRegistros.adapter = adapter
        lvRegistros.emptyView = tvEmpty
        tvEmpty.text = getString(R.string.records_empty)
        tvTotal.text = when (registros.size) {
            0 -> getString(R.string.records_empty)
            1 -> getString(R.string.records_recent_single)
            recordLimit -> getString(R.string.records_recent_limit, recordLimit)
            else -> getString(R.string.records_recent, registros.size)
        }

        btnVolver.setOnClickListener {
            finish()  // vuelve a MainActivity
        }
    }
}