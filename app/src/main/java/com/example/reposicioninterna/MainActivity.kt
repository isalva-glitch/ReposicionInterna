package com.example.reposicioninterna

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.LinkedHashSet
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var topAppBar: MaterialToolbar
    private lateinit var tilFecha: TextInputLayout
    private lateinit var tilNumeroPedido: TextInputLayout
    private lateinit var tilResponsable: TextInputLayout
    private lateinit var tilSector: TextInputLayout
    private lateinit var tilMaterial: TextInputLayout
    private lateinit var tilAlto: TextInputLayout
    private lateinit var tilAncho: TextInputLayout
    private lateinit var tilCara1: TextInputLayout
    private lateinit var tilCara2: TextInputLayout
    private lateinit var tilMotivo: TextInputLayout

    private lateinit var etFecha: TextInputEditText
    private lateinit var etNumeroPedido: TextInputEditText
    private lateinit var etAlto: TextInputEditText
    private lateinit var etAncho: TextInputEditText
    private lateinit var etCara1: TextInputEditText
    private lateinit var etCara2: TextInputEditText
    private lateinit var etMotivo: TextInputEditText
    private lateinit var actvResponsable: MaterialAutoCompleteTextView
    private lateinit var actvSector: MaterialAutoCompleteTextView
    private lateinit var actvMaterial: MaterialAutoCompleteTextView

    private lateinit var chipGroupCara1: ChipGroup
    private lateinit var chipGroupCara2: ChipGroup
    private lateinit var chipPulido: Chip
    private lateinit var chipTemplado: Chip
    private lateinit var chipPulidoCara2: Chip
    private lateinit var chipTempladoCara2: Chip
    private lateinit var cbDvh: CheckBox
    private lateinit var rgOrigenCorte: RadioGroup
    private lateinit var rbFloat: RadioButton
    private lateinit var rbLaminado: RadioButton

    private lateinit var btnGuardarEnviar: com.google.android.material.button.MaterialButton
    private lateinit var btnPreviewPdf: com.google.android.material.button.MaterialButton
    private lateinit var previewPlaceholder: View
    private lateinit var chipResumen: ChipGroup

    private var repository: ReposicionRepository? = null

    private val materialList = mutableListOf(
        "Float 4mm",
        "Float 6mm",
        "Laminado 3+3",
        "Laminado 4+4"
    )

    private val responsableList = mutableListOf(
        "Juan",
        "Claudia",
        "Carlos"
    )

    private val sectorList = mutableListOf(
        "Corte",
        "Armado",
        "Templado"
    )

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initFecha()
        initDatePicker()
        loadMastersFromCsv()
        setupDropdowns()
        renderPreview(null)

        btnPreviewPdf.setOnClickListener { lifecycleScope.launch { onPreviewPdf() } }
        btnGuardarEnviar.setOnClickListener { lifecycleScope.launch { onGuardarYEnviar() } }

        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_new -> {
                    clearForm()
                    Toast.makeText(this, "Formulario listo para nuevo pedido", Toast.LENGTH_SHORT)
                        .show()
                    true
                }

                R.id.action_history -> {
                    if (ensureRepository() != null) {
                        startActivity(Intent(this, RecordsActivity::class.java))
                    }
                    true
                }

                R.id.action_exit -> {
                    finish()
                    true
                }

                else -> false
            }
        }
    }

    private fun bindViews() {
        topAppBar = findViewById(R.id.topAppBar)
        tilFecha = findViewById(R.id.tilFecha)
        tilNumeroPedido = findViewById(R.id.tilNumeroPedido)
        tilResponsable = findViewById(R.id.tilResponsable)
        tilSector = findViewById(R.id.tilSector)
        tilMaterial = findViewById(R.id.tilMaterial)
        tilAlto = findViewById(R.id.tilAlto)
        tilAncho = findViewById(R.id.tilAncho)
        tilCara1 = findViewById(R.id.tilCara1)
        tilCara2 = findViewById(R.id.tilCara2)
        tilMotivo = findViewById(R.id.tilMotivo)

        etFecha = findViewById(R.id.etFecha)
        etNumeroPedido = findViewById(R.id.etNumeroPedido)
        etAlto = findViewById(R.id.etAlto)
        etAncho = findViewById(R.id.etAncho)
        etCara1 = findViewById(R.id.etCara1)
        etCara2 = findViewById(R.id.etCara2)
        etMotivo = findViewById(R.id.etMotivo)
        actvResponsable = findViewById(R.id.actvResponsable)
        actvSector = findViewById(R.id.actvSector)
        actvMaterial = findViewById(R.id.actvMaterial)

        chipGroupCara1 = findViewById(R.id.chipGroupCara1)
        chipGroupCara2 = findViewById(R.id.chipGroupCara2)
        chipPulido = findViewById(R.id.chipPulido)
        chipTemplado = findViewById(R.id.chipTemplado)
        chipPulidoCara2 = findViewById(R.id.chipPulidoCara2)
        chipTempladoCara2 = findViewById(R.id.chipTempladoCara2)
        cbDvh = findViewById(R.id.cbDvh)
        rgOrigenCorte = findViewById(R.id.rgOrigenCorte)
        rbFloat = findViewById(R.id.rbFloat)
        rbLaminado = findViewById(R.id.rbLaminado)

        btnGuardarEnviar = findViewById(R.id.btnGuardarEnviar)
        btnPreviewPdf = findViewById(R.id.btnPreviewPdf)
        previewPlaceholder = findViewById(R.id.previewPlaceholder)
        chipResumen = findViewById(R.id.chipResumen)
    }

    private fun initFecha() {
        val today = Calendar.getInstance().time
        etFecha.setText(dateFormat.format(today))
    }

    private fun initDatePicker() {
        etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dp = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth)
                    etFecha.setText(dateFormat.format(cal.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dp.show()
        }
    }

    private fun loadMastersFromCsv() {
        try {
            val materials = LinkedHashSet<String>(materialList)
            val responsables = LinkedHashSet<String>(responsableList)
            val sectores = LinkedHashSet<String>(sectorList)

            assets.open("maestros_reposicion.csv").bufferedReader().useLines { lines ->
                lines.forEachIndexed { index, rawLine ->
                    val line = rawLine.trim()
                    if (line.isEmpty()) return@forEachIndexed
                    if (index == 0 && line.contains("Material", ignoreCase = true)) return@forEachIndexed

                    val parts = line.split(';', ',').map { it.trim() }
                    val material = parts.getOrNull(0)
                    val responsable = parts.getOrNull(1)
                    val sector = parts.getOrNull(2)

                    if (!material.isNullOrEmpty()) materials.add(material)
                    if (!responsable.isNullOrEmpty()) responsables.add(responsable)
                    if (!sector.isNullOrEmpty()) sectores.add(sector)
                }
            }

            materialList.clear(); materialList.addAll(materials)
            responsableList.clear(); responsableList.addAll(responsables)
            sectorList.clear(); sectorList.addAll(sectores)
        } catch (t: Throwable) {
            t.printStackTrace()
            Toast.makeText(
                this,
                "No se pudo leer maestros_reposicion.csv, se usan valores fijos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupDropdowns() {
        actvMaterial.setAdapter(
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                materialList
            )
        )
        actvResponsable.setAdapter(
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                responsableList
            )
        )
        actvSector.setAdapter(
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                sectorList
            )
        )
    }

    private suspend fun onGuardarYEnviar() {
        val repo = ensureRepository() ?: return
        val record = gatherRecord() ?: return

        if (repo.isDuplicated(record.fecha, record.numeroPedido)) {
            tilNumeroPedido.error = "Ya existe un pedido con esa fecha y número"
            return
        }

        val pdfFile = PdfGenerator.generate(this, record)
        val savedId = repo.save(record.copy(pdfPath = pdfFile?.absolutePath))
        if (savedId > 0) {
            Toast.makeText(this, "Registro #$savedId guardado", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            return
        }

        pdfFile?.let { promptSendWithChooser(it) }
        clearForm()
        Toast.makeText(this, "Formulario listo para un nuevo registro", Toast.LENGTH_SHORT)
            .show()
    }

    private suspend fun onPreviewPdf() {
        val record = gatherRecord() ?: return
        val pdfFile = PdfGenerator.generate(this, record)
        if (pdfFile != null) {
            openPdfPreview(pdfFile)
        } else {
            Toast.makeText(this, "No se pudo generar la vista previa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun gatherRecord(): ReposicionRecord? {
        clearErrors()
        val fecha = etFecha.text?.toString()?.trim().orEmpty()
        val numeroPedido = etNumeroPedido.text?.toString()?.trim().orEmpty()

        if (fecha.isEmpty() || !isValidDate(fecha)) {
            tilFecha.error = "Fecha inválida"
            return null
        }

        if (numeroPedido.isEmpty()) {
            tilNumeroPedido.error = "Obligatorio"
            return null
        }

        val responsable = actvResponsable.text?.toString()?.trim().orEmpty()
        val sector = actvSector.text?.toString()?.trim().orEmpty()
        val material = actvMaterial.text?.toString()?.trim().orEmpty()
        val motivo = etMotivo.text?.toString()?.trim()
        val alto = etAlto.text?.toString()?.trim().orEmpty()
        val ancho = etAncho.text?.toString()?.trim().orEmpty()
        val cara1 = etCara1.text?.toString()?.trim()
        val cara2 = etCara2.text?.toString()?.trim()

        if (responsable.isEmpty()) tilResponsable.error = "Elegí un responsable"
        if (sector.isEmpty()) tilSector.error = "Elegí el sector"
        if (material.isEmpty()) tilMaterial.error = "Elegí el material"
        if (responsable.isEmpty() || sector.isEmpty() || material.isEmpty()) return null

        val pulidoCara1 = chipPulido.isChecked
        val templadoCara1 = chipTemplado.isChecked
        val pulidoCara2 = chipPulidoCara2.isChecked
        val templadoCara2 = chipTempladoCara2.isChecked
        val yaEsDvh = cbDvh.isChecked

        val requiresDimensions = pulidoCara1 || templadoCara1 || pulidoCara2 || templadoCara2 ||
                (!cara1.isNullOrBlank()) || (!cara2.isNullOrBlank())

        if (requiresDimensions) {
            if (alto.toDoubleOrNull() == null) tilAlto.error = "Completar alto"
            if (ancho.toDoubleOrNull() == null) tilAncho.error = "Completar ancho"
            if (tilAlto.error != null || tilAncho.error != null) return null
        }

        val origenCorte = when (rgOrigenCorte.checkedRadioButtonId) {
            rbFloat.id -> "Cortar de Float"
            rbLaminado.id -> "Cortar de Laminado"
            else -> "Cortar de Laminado"
        }

        val record = ReposicionRecord(
            fecha = fecha,
            numeroPedido = numeroPedido,
            responsable = responsable,
            sector = sector,
            material = material,
            alto = alto,
            ancho = ancho,
            cara1 = cara1,
            cara2 = cara2,
            motivo = motivo,
            pulidoCara1 = pulidoCara1,
            templadoCara1 = templadoCara1,
            pulidoCara2 = pulidoCara2,
            templadoCara2 = templadoCara2,
            yaEsDvh = yaEsDvh,
            origenCorte = origenCorte
        )

        renderPreview(record)
        return record
    }

    private fun clearForm() {
        initFecha()
        etNumeroPedido.text?.clear()
        etMotivo.text?.clear()
        etAlto.text?.clear()
        etAncho.text?.clear()
        etCara1.text?.clear()
        etCara2.text?.clear()
        actvResponsable.text = null
        actvSector.text = null
        actvMaterial.text = null

        chipGroupCara1.clearCheck()
        chipGroupCara2.clearCheck()
        cbDvh.isChecked = false
        rbLaminado.isChecked = true

        clearErrors()
        renderPreview(null)
    }

    private fun clearErrors() {
        listOf(
            tilFecha,
            tilNumeroPedido,
            tilResponsable,
            tilSector,
            tilMaterial,
            tilAlto,
            tilAncho,
            tilCara1,
            tilCara2,
            tilMotivo
        ).forEach { it.error = null }
    }

    private fun isValidDate(value: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(value) != null
        } catch (ex: Exception) {
            false
        }
    }

    private fun renderPreview(record: ReposicionRecord?) {
        chipResumen.removeAllViews()
        if (record == null) {
            chipResumen.visibility = View.GONE
            previewPlaceholder.visibility = View.VISIBLE
            return
        }

        previewPlaceholder.visibility = View.GONE
        chipResumen.visibility = View.VISIBLE

        val medidas = listOfNotNull(
            record.alto?.takeIf { it.isNotBlank() },
            record.ancho?.takeIf { it.isNotBlank() }
        ).joinToString(" x ")

        val resumenItems = listOf(
            "${record.fecha} · Pedido ${record.numeroPedido}",
            "Material: ${record.material}",
            "Medidas: ${if (medidas.isNotBlank()) medidas else "-"}",
            "Origen: ${record.origenCorte}",
            "Vidrio 1: " + listOfNotNull(
                record.pulidoCara1.takeIf { it }?.let { "Pulido" },
                record.templadoCara1.takeIf { it }?.let { "Templado" },
                record.cara1?.takeIf { it.isNotBlank() }
            ).ifEmpty { listOf("Sin procesos") }.joinToString(" · "),
            "Vidrio 2: " + listOfNotNull(
                record.pulidoCara2.takeIf { it }?.let { "Pulido" },
                record.templadoCara2.takeIf { it }?.let { "Templado" },
                record.cara2?.takeIf { it.isNotBlank() }
            ).ifEmpty { listOf("Sin procesos") }.joinToString(" · "),
            "DVH: ${if (record.yaEsDvh) "Sí" else "No"}",
            "Resp: ${record.responsable} · Sector: ${record.sector}",
            "Motivo: ${record.motivo?.ifEmpty { "-" } ?: "-"}"
        )

        resumenItems.forEach { text ->
            val chip = Chip(this).apply {
                this.text = text
                isCheckable = false
                isCloseIconVisible = false
            }
            chipResumen.addView(chip)
        }
    }

    private fun promptSendWithChooser(file: File) {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 16, 32, 0)
        }

        val subjectInput = TextInputEditText(this).apply {
            setText("Reposición interna de cristales")
            hint = "Asunto"
        }
        val toInput = TextInputEditText(this).apply {
            setText("claudia@fontela.com.ar")
            hint = "Destinatario"
        }
        layout.addView(subjectInput)
        layout.addView(toInput)

        AlertDialog.Builder(this)
            .setTitle("Enviar PDF")
            .setView(layout)
            .setPositiveButton("Continuar") { _, _ ->
                shareFile(file, subjectInput.text?.toString().orEmpty(), toInput.text?.toString().orEmpty())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun shareFile(file: File, subject: String, to: String) {
        val authority = "${applicationContext.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(this, authority, file)

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject.ifBlank { "Reposición interna de cristales" })
            putExtra(
                Intent.EXTRA_TEXT,
                "Se adjunta reposición interna generada desde la tablet."
            )
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(emailIntent, "Enviar o compartir..."))
    }

    private fun openPdfPreview(file: File) {
        val intent = Intent(this, PdfPreviewActivity::class.java).apply {
            putExtra(PdfPreviewActivity.EXTRA_PDF_PATH, file.absolutePath)
        }
        startActivity(intent)
    }

    private fun ensureRepository(): ReposicionRepository? {
        if (repository != null) return repository

        return try {
            ReposicionRepository.getInstance(this).also { repository = it }
        } catch (error: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir la base de datos. Reintentá o revisá el almacenamiento.",
                Toast.LENGTH_LONG
            ).show()
            null
        }
    }
}
