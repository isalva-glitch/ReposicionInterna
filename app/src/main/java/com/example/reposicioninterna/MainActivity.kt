package com.example.reposicioninterna

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
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
    private lateinit var spTipologia: Spinner
    private lateinit var spMaterial: Spinner
    private lateinit var etAlto: EditText
    private lateinit var etAncho: EditText
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
    private lateinit var btnAgregarItem: Button
    private lateinit var tvResumen: TextView
    private lateinit var containerVidrio2: LinearLayout
    private lateinit var labelOrigenCorte: TextView

    private lateinit var dbHelper: ReposicionDbHelper

    // Listas (se completan con CSV al inicio)
    private val materialList = mutableListOf<String>()
    private val responsableList = mutableListOf<String>()
    private val sectorList = mutableListOf<String>()
    private val tipologiaList = mutableListOf<String>()

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
        spTipologia = findViewById(R.id.spTipologia)
        spMaterial = findViewById(R.id.spMaterial)
        etAlto = findViewById(R.id.etAlto)
        etAncho = findViewById(R.id.etAncho)
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
        btnAgregarItem = findViewById(R.id.btnAgregarItem)
        tvResumen = findViewById(R.id.tvResumen)
        containerVidrio2 = findViewById(R.id.containerVidrio2)
        labelOrigenCorte = findViewById(R.id.labelOrigenCorte)

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

        btnAgregarItem.setOnClickListener {
            onAgregarItem()
        }

        // Setup reactive preview updates
        setupReactivePreview()

        requestPinnedShortcutIfNeeded()
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
            val materials = LinkedHashSet<String>()
            val responsables = LinkedHashSet<String>()
            val sectores = LinkedHashSet<String>()
            val tipologias = LinkedHashSet<String>()

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
                    val tipologia = parts.getOrNull(3)

                    if (!material.isNullOrEmpty()) materials.add(material)
                    if (!responsable.isNullOrEmpty()) responsables.add(responsable)
                    if (!sector.isNullOrEmpty()) sectores.add(sector)
                    if (!tipologia.isNullOrEmpty()) tipologias.add(tipologia)
                }
            }

            materialList.clear()
            materialList.addAll(materials)

            responsableList.clear()
            responsableList.addAll(responsables)

            sectorList.clear()
            sectorList.addAll(sectores)

            tipologiaList.clear()
            tipologiaList.addAll(tipologias)

        } catch (t: Throwable) {
            t.printStackTrace()
            Toast.makeText(
                this,
                "No se pudo leer maestros_reposicion.csv",
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

        val tipologiaAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tipologiaList
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spTipologia.adapter = tipologiaAdapter
        
        // Set Float as default if available
        val floatIndex = tipologiaList.indexOfFirst { it.contains("Float", ignoreCase = true) }
        if (floatIndex >= 0) {
            spTipologia.setSelection(floatIndex)
        }
        
        // Listen for tipología changes to update UI
        spTipologia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val tipologia = tipologiaList.getOrNull(position)
                updateUIForTipologia(tipologia)
                // Trigger preview update
                tryUpdatePreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupReactivePreview() {
        // Setup listeners on all fields to update preview reactively
        val textWatcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                tryUpdatePreview()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etAlto.addTextChangedListener(textWatcher)
        etAncho.addTextChangedListener(textWatcher)
        etMotivo.addTextChangedListener(textWatcher)

        // Spinner listeners
        val spinnerListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                tryUpdatePreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spMaterial.onItemSelectedListener = spinnerListener
        spResponsable.onItemSelectedListener = spinnerListener
        spSector.onItemSelectedListener = spinnerListener

        // Checkbox listeners
        val checkboxListener = { _: android.widget.CompoundButton, _: Boolean ->
            tryUpdatePreview()
        }

        cbPulido.setOnCheckedChangeListener(checkboxListener)
        cbTemplado.setOnCheckedChangeListener(checkboxListener)
        cbPulidoCara2.setOnCheckedChangeListener(checkboxListener)
        cbTempladoCara2.setOnCheckedChangeListener(checkboxListener)
        cbDvh.setOnCheckedChangeListener(checkboxListener)

        // RadioGroup listener
        rgOrigenCorte.setOnCheckedChangeListener { _, _ ->
            tryUpdatePreview()
        }
    }

    private fun tryUpdatePreview() {
        // Silently update preview without showing errors
        try {
            val fecha = etFecha.text.toString().trim()
            val numeroPedido = etNumeroPedido.text.toString().trim()

            if (fecha.isEmpty() || numeroPedido.isEmpty()) {
                tvResumen.text = getString(R.string.preview_hint)
                return
            }

            val responsable = spResponsable.selectedItem?.toString()
            val sector = spSector.selectedItem?.toString()
            val tipologia = spTipologia.selectedItem?.toString()
            val material = spMaterial.selectedItem?.toString()
            val motivo = etMotivo.text.toString().trim()
            val alto = etAlto.text.toString().trim()
            val ancho = etAncho.text.toString().trim()

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
                tipologia = tipologia,
                material = material,
                alto = alto,
                ancho = ancho,
                motivo = motivo,
                pulidoCara1 = pulidoCara1,
                templadoCara1 = templadoCara1,
                pulidoCara2 = pulidoCara2,
                templadoCara2 = templadoCara2,
                yaEsDvh = yaEsDvh,
                origenCorte = origenCorte
            )

            updateResumen(record)
        } catch (e: Exception) {
            // Silently ignore errors during preview
        }
    }

    /**
     * Update UI visibility based on selected Tipología
     * Float: Show only Vidrio 1 processes, hide Vidrio 2 and Origen del corte
     * Laminado: Show both Vidrio 1 and Vidrio 2 processes, show Origen del corte
     */
    private fun updateUIForTipologia(tipologia: String?) {
        when {
            tipologia?.contains("Float", ignoreCase = true) == true -> {
                // For Float: hide Vidrio 2 and Origen del corte
                containerVidrio2.visibility = android.view.View.GONE
                labelOrigenCorte.visibility = android.view.View.GONE
                rgOrigenCorte.visibility = android.view.View.GONE
            }
            tipologia?.contains("Laminado", ignoreCase = true) == true -> {
                // For Laminado: show both vidrios and origen del corte
                containerVidrio2.visibility = android.view.View.VISIBLE
                labelOrigenCorte.visibility = android.view.View.VISIBLE
                rgOrigenCorte.visibility = android.view.View.VISIBLE
            }
            else -> {
                // Default: show all
                containerVidrio2.visibility = android.view.View.VISIBLE
                labelOrigenCorte.visibility = android.view.View.VISIBLE
                rgOrigenCorte.visibility = android.view.View.VISIBLE
            }
        }
    }

    // ---------- BOTONES PRINCIPALES ----------

    private fun onGuardarYEnviar() {
        val record = gatherRecord() ?: return

        val savedId = dbHelper.insertRecord(record)
        if (savedId > 0) {
            Toast.makeText(this, "Registro #$savedId guardado", Toast.LENGTH_SHORT).show()
            performDatabaseBackup()
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

    private fun onAgregarItem() {
        val record = gatherRecord() ?: return

        val savedId = dbHelper.insertRecord(record)
        if (savedId > 0) {
            Toast.makeText(this, "Registro #$savedId guardado", Toast.LENGTH_SHORT).show()
            performDatabaseBackup()
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            return
        }

        clearItemFields()
        Toast.makeText(this, "Item agregado. Podés sumar otro al mismo pedido.", Toast.LENGTH_SHORT).show()
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
        val tipologia = spTipologia.selectedItem?.toString()
        val material = spMaterial.selectedItem?.toString()

        val motivo = etMotivo.text.toString().trim()
        val alto = etAlto.text.toString().trim()
        val ancho = etAncho.text.toString().trim()
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
            tipologia = tipologia,
            material = material,
            alto = alto,
            ancho = ancho,
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
        etAlto.text.clear()
        etAncho.text.clear()
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
        if (spTipologia.adapter != null && spTipologia.adapter.count > 0) {
            spTipologia.setSelection(0)
        }

        cbPulido.isChecked = false
        cbTemplado.isChecked = false
        cbPulidoCara2.isChecked = false
        cbTempladoCara2.isChecked = false
        cbDvh.isChecked = false

        rbFloat.isChecked = true


        tvResumen.text = getString(R.string.preview_hint)
    }

    private fun clearItemFields() {
        etAlto.text.clear()
        etAncho.text.clear()
        etMotivo.text.clear()

        if (spTipologia.adapter != null && spTipologia.adapter.count > 0) {
            spTipologia.setSelection(0)
        }
        if (spMaterial.adapter != null && spMaterial.adapter.count > 0) {
            spMaterial.setSelection(0)
        }

        cbPulido.isChecked = false
        cbTemplado.isChecked = false
        cbPulidoCara2.isChecked = false
        cbTempladoCara2.isChecked = false
        cbDvh.isChecked = false

        rbFloat.isChecked = true
        
        // No limpiamos nro pedido, responsable, sector ni fecha
        tvResumen.text = getString(R.string.preview_hint_item) ?: "Esperando nuevo item..."
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
            canvas.drawText("Tipología: ${record.tipologia ?: ""}", 40f, y, paint); y += 18f
            canvas.drawText("Material: ${record.material ?: ""}", 40f, y, paint); y += 18f
            if (!record.alto.isNullOrBlank() || !record.ancho.isNullOrBlank()) {
                canvas.drawText(
                    "Medidas: ${record.alto.orEmpty()} x ${record.ancho.orEmpty()}",
                    40f,
                    y,
                    paint
                );
                y += 24f
            } else {
                y += 6f
            }

            // Procesos por vidrio
            canvas.drawText(
                "Vidrio 1 - Pulido: ${if (record.pulidoCara1) "SI" else "NO"}  " +
                        "Templado: ${if (record.templadoCara1) "SI" else "NO"}",
                40f, y, paint
            ); y += 20f

            canvas.drawText(
                "Vidrio 2 - Pulido: ${if (record.pulidoCara2) "SI" else "NO"}  " +
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
            putExtra(Intent.EXTRA_CC, arrayOf("mfontela@fontela.com.ar", "isalva@fontela.com.ar"))
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

        val medidas = listOfNotNull(
            record.alto?.takeIf { it.isNotBlank() },
            record.ancho?.takeIf { it.isNotBlank() }
        ).joinToString(" x ")

        val resumen = """
                ${record.fecha} · Pedido ${record.numeroPedido}
                ${record.tipologia ?: ""} · ${record.material ?: ""}${if (medidas.isNotEmpty()) " · $medidas" else ""} (${record.origenCorte})
                Vidrio 1: $procCara1 | Vidrio 2: $procCara2
                ${if (record.yaEsDvh) "Ya es DVH" else "Sin DVH"} · Resp: ${record.responsable ?: ""} · Sector: ${record.sector ?: ""}
                Motivo: ${record.motivo?.ifEmpty { "-" } ?: "-"}
            """.trimIndent()

        tvResumen.text = resumen
    }

    private fun performDatabaseBackup() {
        try {
            val dbFile = getDatabasePath("reposicion.db")
            if (!dbFile.exists()) return

            val backupDir = File(getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val backupFile = File(backupDir, "reposicion_backup.db")
            
            dbFile.inputStream().use { input ->
                backupFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            // También guardamos una copia con fecha para historial de hoy
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val historyFile = File(backupDir, "reposicion_$dateStr.db")
            backupFile.copyTo(historyFile, overwrite = true)

        } catch (e: Exception) {
            e.printStackTrace()
            // Error silencioso en backup para no interrumpir el flujo principal
        }
    }

    private fun requestPinnedShortcutIfNeeded() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_PINNED_SHORTCUT_REQUESTED, false)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            if (shortcutManager?.isRequestPinShortcutSupported == true) {
                val shortcut = ShortcutInfo.Builder(this, "reposicion_home")
                    .setShortLabel(getString(R.string.shortcut_label))
                    .setLongLabel(getString(R.string.shortcut_label))
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                    .setIntent(Intent(this, MainActivity::class.java).setAction(Intent.ACTION_VIEW))
                    .build()
                shortcutManager.requestPinShortcut(shortcut, null)
                prefs.edit { putBoolean(KEY_PINNED_SHORTCUT_REQUESTED, true) }
            }
        } else {
            @Suppress("DEPRECATION")
            val shortcutIntent = Intent(this, MainActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)

            @Suppress("DEPRECATION")
            val installer = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_label))
                putExtra("duplicate", false)
                putExtra(
                    Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this@MainActivity, R.mipmap.ic_launcher)
                )
            }
            sendBroadcast(installer)
            prefs.edit { putBoolean(KEY_PINNED_SHORTCUT_REQUESTED, true) }
        }
    }

    companion object {
        private const val PREFS_NAME = "reposicion_prefs"
        private const val KEY_PINNED_SHORTCUT_REQUESTED = "pinned_shortcut_requested"
    }
}
