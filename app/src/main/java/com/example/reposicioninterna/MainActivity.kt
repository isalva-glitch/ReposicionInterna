package com.example.reposicioninterna

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // Controles
    private lateinit var etFecha: EditText
    private lateinit var etNumeroPedido: EditText
    private lateinit var spResponsable: Spinner
    private lateinit var spSector: Spinner
    private lateinit var spMaterial: Spinner
    private lateinit var etCara1: EditText
    private lateinit var etCara2: EditText
    private lateinit var etMotivo: EditText
    private lateinit var cbPulido: CheckBox
    private lateinit var cbTemplado: CheckBox
    private lateinit var cbPulidoCara2: CheckBox
    private lateinit var cbTempladoCara2: CheckBox
    private lateinit var cbDvh: CheckBox
    private lateinit var rgOrigenCorte: RadioGroup
    private lateinit var rbFloat: RadioButton
    private lateinit var rbLaminado: RadioButton
    private lateinit var btnGuardarEnviar: Button
    private lateinit var btnPreviewPdf: Button
    private lateinit var btnNuevoPedido: Button
    private lateinit var btnVerRegistros: Button
    private lateinit var btnSalir: Button
    private lateinit var tvResumen: TextView

    private lateinit var dbHelper: ReposicionDbHelper

    // Listas (valores por defecto + se completan con CSV si existe)
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

        dbHelper = ReposicionDbHelper(this)

        // Vincular vistas
        etFecha = findViewById(R.id.etFecha)
        etNumeroPedido = findViewById(R.id.etNumeroPedido)
        spResponsable = findViewById(R.id.spResponsable)
        spSector = findViewById(R.id.spSector)
        spMaterial = findViewById(R.id.spMaterial)
        etCara1 = findViewById(R.id.etCara1)
        etCara2 = findViewById(R.id.etCara2)
        etMotivo = findViewById(R.id.etMotivo)
        cbPulido = findViewById(R.id.cbPulido)
        cbTemplado = findViewById(R.id.cbTemplado)
        cbPulidoCara2 = findViewById(R.id.cbPulidoCara2)
        cbTempladoCara2 = findViewById(R.id.cbTempladoCara2)
        cbDvh = findViewById(R.id.cbDvh)
        rgOrigenCorte = findViewById(R.id.rgOrigenCorte)
        rbFloat = findViewById(R.id.rbFloat)
        rbLaminado = findViewById(R.id.rbLaminado)
        btnGuardarEnviar = findViewById(R.id.btnGuardarEnviar)
        btnPreviewPdf = findViewById(R.id.btnPreviewPdf)
        btnNuevoPedido = findViewById(R.id.btnNuevoPedido)
        btnVerRegistros = findViewById(R.id.btnVerRegistros)
        btnSalir = findViewById(R.id.btnSalir)
        tvResumen = findViewById(R.id.tvResumen)

        initFecha()
        initDatePicker()
        tvResumen.text = getString(R.string.preview_hint)

        // Cargar maestros desde CSV (si está ok)
        loadMastersFromCsv()
        setupSpinners()

        // BOTONES
        btnPreviewPdf.setOnClickListener { onPreviewPdf() }
        btnGuardarEnviar.setOnClickListener { onGuardarYEnviar() }

        btnNuevoPedido.setOnClickListener {
            clearForm()
            Toast.makeText(this, "Formulario listo para nuevo pedido", Toast.LENGTH_SHORT).show()
        }

        btnVerRegistros.setOnClickListener {
            startActivity(Intent(this, RecordsActivity::class.java))
        }

        btnSalir.setOnClickListener {
            finishAffinity()
        }
    }

    // ---------- INICIALIZACIÓN FECHA ----------

    private fun initFecha() {
        etFecha.setText(dateFormat.format(Date()))
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

    // ---------- LECTURA CSV MAESTROS ----------

    /**
     * Lee maestros_reposicion.csv desde assets y arma las listas
     * de Material, Responsable y Sector.
     *
     * Ubicación en el proyecto:
     *   app/src/main/assets/maestros_reposicion.csv
     *
     * Formato:
     *   Material;Responsable;Sector
     *   Float 4mm;Juan;Corte
     *   ...
     */
    private fun loadMastersFromCsv() {
        try {
            val materials = LinkedHashSet<String>(materialList)
            val responsables = LinkedHashSet<String>(responsableList)
            val sectores = LinkedHashSet<String>(sectorList)

            assets.open("maestros_reposicion.csv").bufferedReader().useLines { lines ->
                lines.forEachIndexed { index, rawLine ->
                    val line = rawLine.trim()
                    if (line.isEmpty()) return@forEachIndexed

                    // Saltar cabecera si es primera línea con "Material"
                    if (index == 0 && line.contains("Material", ignoreCase = true)) {
                        return@forEachIndexed
                    }

                    // Soportar ; o ,
                    val parts = line.split(';', ',').map { it.trim() }
                    if (parts.isEmpty()) return@forEachIndexed

                    val material = parts.getOrNull(0)
                    val responsable = parts.getOrNull(1)
                    val sector = parts.getOrNull(2)

                    if (!material.isNullOrEmpty()) materials.add(material)
                    if (!responsable.isNullOrEmpty()) responsables.add(responsable)
                    if (!sector.isNullOrEmpty()) sectores.add(sector)
                }
            }

            materialList.clear()
            materialList.addAll(materials)

            responsableList.clear()
            responsableList.addAll(responsables)

            sectorList.clear()
            sectorList.addAll(sectores)

        } catch (t: Throwable) {
            t.printStackTrace()
            Toast.makeText(
                this,
                "No se pudo leer maestros_reposicion.csv, se usan valores fijos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupSpinners() {
        val materialAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            materialList
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spMaterial.adapter = materialAdapter

        val respAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            responsableList
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spResponsable.adapter = respAdapter

        val sectorAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sectorList
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spSector.adapter = sectorAdapter
    }

    // ---------- BOTONES PRINCIPALES ----------

    private fun onGuardarYEnviar() {
        val record = gatherRecord() ?: return

        val savedId = dbHelper.insertRecord(record)
        if (savedId > 0) {
            Toast.makeText(this, "Registro #$savedId guardado", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            return
        }


        val pdfFile = generatePdfForRecord(record)
        if (pdfFile != null) {
            sendEmailWithAttachment(pdfFile, "application/pdf")
        } else {
            Toast.makeText(this, "No se pudo generar el PDF", Toast.LENGTH_SHORT).show()
        }

        clearForm()
        Toast.makeText(this, "Formulario listo para un nuevo registro", Toast.LENGTH_SHORT)
            .show()
    }

    private fun onPreviewPdf() {
        val record = gatherRecord() ?: return
        val pdfFile = generatePdfForRecord(record)
        if (pdfFile != null) {
            openPdfPreview(pdfFile)
        } else {
            Toast.makeText(this, "No se pudo generar la vista previa", Toast.LENGTH_SHORT).show()
        }
    }
    // ---------- ARMADO DEL REGISTRO ----------

    private fun gatherRecord(): ReposicionRecord? {
        val fecha = etFecha.text.toString().trim()
        val numeroPedido = etNumeroPedido.text.toString().trim()

        if (fecha.isEmpty() || !isValidDate(fecha)) {
            etFecha.error = "Fecha inválida"
            Toast.makeText(this, "Indicá la fecha con formato dd/MM/yyyy", Toast.LENGTH_SHORT)
                .show()
            return null
        }


        if (numeroPedido.isEmpty()) {
            etNumeroPedido.error = "Obligatorio"
            Toast.makeText(this, "Ingresar N° de pedido", Toast.LENGTH_SHORT).show()
            return null
        }

        val responsable = spResponsable.selectedItem?.toString()
        val sector = spSector.selectedItem?.toString()
        val material = spMaterial.selectedItem?.toString()

        val cara1 = etCara1.text.toString().trim()
        val cara2 = etCara2.text.toString().trim()
        val motivo = etMotivo.text.toString().trim()
        if (responsable.isNullOrBlank()) {
            Toast.makeText(this, "Elegí un responsable", Toast.LENGTH_SHORT).show()
            return null
        }

        if (sector.isNullOrBlank()) {
            Toast.makeText(this, "Elegí el sector", Toast.LENGTH_SHORT).show()
            return null
        }

        if (material.isNullOrBlank()) {
            Toast.makeText(this, "Elegí el material", Toast.LENGTH_SHORT).show()
            return null
        }

        if (cara1.isEmpty() && cara2.isEmpty() && motivo.isEmpty()) {
            etCara1.error = "Completar al menos una cara"
            etCara2.error = "Completar al menos una cara"
            etMotivo.error = "Indicar motivo"
            Toast.makeText(this, "Detallá la reposición o el motivo", Toast.LENGTH_SHORT)
                .show()
            return null
        }

        val pulidoCara1 = cbPulido.isChecked
        val templadoCara1 = cbTemplado.isChecked
        val pulidoCara2 = cbPulidoCara2.isChecked
        val templadoCara2 = cbTempladoCara2.isChecked
        val yaEsDvh = cbDvh.isChecked

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

        updateResumen(record)
        return record
    }

    // ---------- LIMPIAR FORMULARIO (NUEVO PEDIDO) ----------

    private fun clearForm() {
        initFecha()
        etNumeroPedido.text.clear()
        etCara1.text.clear()
        etCara2.text.clear()
        etMotivo.text.clear()

        if (spResponsable.adapter != null && spResponsable.adapter.count > 0) {
            spResponsable.setSelection(0)
        }
        if (spSector.adapter != null && spSector.adapter.count > 0) {
            spSector.setSelection(0)
        }
        if (spMaterial.adapter != null && spMaterial.adapter.count > 0) {
            spMaterial.setSelection(0)
        }

        cbPulido.isChecked = false
        cbTemplado.isChecked = false
        cbPulidoCara2.isChecked = false
        cbTempladoCara2.isChecked = false
        cbDvh.isChecked = false

        rbLaminado.isChecked = true


        tvResumen.text = getString(R.string.preview_hint)
    }

    // ---------- GENERACIÓN DE PDF ----------

    private fun generatePdfForRecord(record: ReposicionRecord): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 aprox
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 12f
            }
            val titlePaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var y = 40f

            // Título
            canvas.drawText("Fontela Cristales - Reposición interna", 40f, y, titlePaint)
            y += 30f

            // Datos generales
            canvas.drawText("Fecha: ${record.fecha}", 40f, y, paint); y += 18f
            canvas.drawText("N° Pedido: ${record.numeroPedido}", 40f, y, paint); y += 18f
            canvas.drawText("Responsable: ${record.responsable ?: ""}", 40f, y, paint); y += 18f
            canvas.drawText("Sector: ${record.sector ?: ""}", 40f, y, paint); y += 18f
            canvas.drawText("Material: ${record.material ?: ""}", 40f, y, paint); y += 24f

            // Cara 1
            canvas.drawText("Cara 1:", 40f, y, paint); y += 16f
            canvas.drawText("  Texto: ${record.cara1 ?: ""}", 40f, y, paint); y += 16f
            canvas.drawText(
                "  Pulido: ${if (record.pulidoCara1) "SI" else "NO"}  " +
                        "Templado: ${if (record.templadoCara1) "SI" else "NO"}",
                40f, y, paint
            ); y += 24f

            // Cara 2
            canvas.drawText("Cara 2:", 40f, y, paint); y += 16f
            canvas.drawText("  Texto: ${record.cara2 ?: ""}", 40f, y, paint); y += 16f
            canvas.drawText(
                "  Pulido: ${if (record.pulidoCara2) "SI" else "NO"}  " +
                        "Templado: ${if (record.templadoCara2) "SI" else "NO"}",
                40f, y, paint
            ); y += 24f

            // Motivo
            canvas.drawText("Motivo:", 40f, y, paint); y += 16f
            canvas.drawText("  ${record.motivo ?: ""}", 40f, y, paint); y += 24f

            // Otros
            canvas.drawText(
                "Ya es DVH: ${if (record.yaEsDvh) "SI" else "NO"}",
                40f,
                y,
                paint
            ); y += 18f
            canvas.drawText("Origen corte: ${record.origenCorte}", 40f, y, paint); y += 18f

            pdfDocument.finishPage(page)

            val fileName = "reposicion_${record.numeroPedido}_${System.currentTimeMillis()}.pdf"
            val file = File(cacheDir, fileName)
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ---------- ENVÍO EMAIL CON ADJUNTO ----------

    private fun sendEmailWithAttachment(file: File, mimeType: String) {
        val authority = "${applicationContext.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(this, authority, file)

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_EMAIL, arrayOf("claudia@fontela.com.ar"))
            putExtra(Intent.EXTRA_SUBJECT, "Reposición interna de cristales")
            putExtra(
                Intent.EXTRA_TEXT,
                "Se adjunta reposición interna generada desde la tablet."
            )
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(emailIntent, "Enviar email..."))
    }


    private fun openPdfPreview(file: File) {
        val intent = Intent(this, PdfPreviewActivity::class.java).apply {
            putExtra(PdfPreviewActivity.EXTRA_PDF_PATH, file.absolutePath)
        }

        startActivity(intent)
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

    private fun updateResumen(record: ReposicionRecord) {
        val procCara1 = listOfNotNull(
            if (record.pulidoCara1) "Pulido" else null,
            if (record.templadoCara1) "Templado" else null
        ).joinToString(" · ").ifEmpty { "Sin procesos" }

        val procCara2 = listOfNotNull(
            if (record.pulidoCara2) "Pulido" else null,
            if (record.templadoCara2) "Templado" else null
        ).joinToString(" · ").ifEmpty { "Sin procesos" }

        val resumen = """
                ${record.fecha} · Pedido ${record.numeroPedido}
                ${record.material ?: ""} (${record.origenCorte})
                Cara 1: $procCara1 | Cara 2: $procCara2
                ${if (record.yaEsDvh) "Ya es DVH" else "Sin DVH"} · Resp: ${record.responsable ?: ""} · Sector: ${record.sector ?: ""}
                Motivo: ${record.motivo?.ifEmpty { "-" } ?: "-"}
            """.trimIndent()

        tvResumen.text = resumen
    }
}
