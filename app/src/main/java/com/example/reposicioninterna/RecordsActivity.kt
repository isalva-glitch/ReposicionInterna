package com.example.reposicioninterna

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import java.io.File
import kotlinx.coroutines.launch

class RecordsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var btnVolver: MaterialButton
    private lateinit var chipFilters: ChipGroup
    private lateinit var tvEmptyState: TextView
    private lateinit var tvHeader: TextView
    private var repository: ReposicionRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        listView = findViewById(R.id.lvRegistros)
        btnVolver = findViewById(R.id.btnVolver)
        chipFilters = findViewById(R.id.chipFilters)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        tvHeader = findViewById(R.id.tvHeader)
        repository = try {
            ReposicionRepository.getInstance(this)
        } catch (error: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir la base de datos. Volvé a intentar más tarde.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            null
        }

        btnVolver.setOnClickListener { finish() }
        chipFilters.setOnCheckedChangeListener { _, _ -> loadRecords() }

        loadRecords()
    }

    private fun loadRecords() {
        lifecycleScope.launch {
            val repo = repository ?: return@launch
            val showOnlyDvh = chipFilters.checkedChipId == R.id.chipFiltroDvh
            val records = if (showOnlyDvh) repo.getAllDvh() else repo.getAll()
            tvHeader.text = "Registros guardados (${records.size})"

            listView.adapter = RecordsAdapter(
                this@RecordsActivity,
                records,
                onDetalle = { showDetalle(it) },
                onReenviar = { shareRecord(it) }
            )

            tvEmptyState.visibility = if (records.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun showDetalle(record: ReposicionRecord) {
        val detalle = """
            ${record.fecha} · Pedido ${record.numeroPedido}
            Material: ${record.material} (${record.origenCorte})
            Medidas: ${record.alto.orEmpty()} x ${record.ancho.orEmpty()}
            Vidrio 1: ${if (record.pulidoCara1) "Pulido" else "-"} ${if (record.templadoCara1) "Templado" else ""} ${record.cara1?.ifBlank { "" }}
            Vidrio 2: ${if (record.pulidoCara2) "Pulido" else "-"} ${if (record.templadoCara2) "Templado" else ""} ${record.cara2?.ifBlank { "" }}
            DVH: ${if (record.yaEsDvh) "Sí" else "No"}
            Resp: ${record.responsable} · Sector: ${record.sector}
            Motivo: ${record.motivo?.ifEmpty { "-" } ?: "-"}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Detalle de pedido")
            .setMessage(detalle)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun shareRecord(record: ReposicionRecord) {
        val pdfFile = record.pdfPath?.let { File(it) }?.takeIf { it.exists() }
            ?: PdfGenerator.generate(this, record)

        if (pdfFile == null) {
            Toast.makeText(this, "No se pudo generar el PDF", Toast.LENGTH_SHORT).show()
            return
        }

        val authority = "${applicationContext.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(this, authority, pdfFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "Reposición interna #${record.numeroPedido}")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Reenviar o compartir"))
    }
}
