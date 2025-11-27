package com.example.reposicioninterna

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class RecordsActivity : AppCompatActivity() {

    private lateinit var dbHelper: ReposicionDbHelper
    private lateinit var lvRegistros: ListView
    private lateinit var btnVolver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        dbHelper = ReposicionDbHelper(this)

        lvRegistros = findViewById(R.id.lvRegistros)
        btnVolver = findViewById(R.id.btnVolver)

        val registros = dbHelper.getAllRecords()

        val items = registros.map { r ->
                val c1 = "C1: " +
                (if (r.pulidoCara1) "Pulido " else "") +
                (if (r.templadoCara1) "Templado" else "")
            val c2 = "C2: " +
                    (if (r.pulidoCara2) "Pulido " else "") +
                    (if (r.templadoCara2) "Templado" else "")

            "${r.fecha}  Ped:${r.numeroPedido}  Mat:${r.material ?: ""}\n" +
                    "$c1   $c2\n" +
                    "Resp:${r.responsable ?: ""}  Sector:${r.sector ?: ""}"
        }

        val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                items
        )
        lvRegistros.adapter = adapter

        btnVolver.setOnClickListener {
            finish()  // vuelve a MainActivity
        }
    }
}
