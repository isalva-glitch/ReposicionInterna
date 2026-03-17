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
    private lateinit var etCliente: EditText
    private lateinit var spResponsable: Spinner
    private lateinit var spSector: Spinner
    private lateinit var spSectorDestino: Spinner
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
    private lateinit var cbAtencionForma: CheckBox
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
    private lateinit var containerItemsPreview: LinearLayout
    private lateinit var btnEliminarItem: Button
    private lateinit var containerVidrio2: LinearLayout
    private lateinit var labelOrigenCorte: TextView
    private lateinit var etCodigoForma: EditText

    private var selectedItemId: Long? = null

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
        etCliente = findViewById(R.id.etCliente)
        spResponsable = findViewById(R.id.spResponsable)
        spSector = findViewById(R.id.spSector)
        spSectorDestino = findViewById(R.id.spSectorDestino)
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
        cbAtencionForma = findViewById(R.id.cbAtencionForma)
        rgOrigenCorte = findViewById(R.id.rgOrigenCorte)
        rbFloat = findViewById(R.id.rbFloat)
        rbLaminado = findViewById(R.id.rbLaminado)
        btnGuardarEnviar = findViewById(R.id.btnGuardarEnviar)
        btnPreviewPdf = findViewById(R.id.btnPreviewPdf)
        btnNuevoPedido = findViewById(R.id.btnNuevoPedido)
        btnVerRegistros = findViewById(R.id.btnVerRegistros)
        btnSalir = findViewById(R.id.btnSalir)
        btnAgregarItem = findViewById(R.id.btnAgregarItem)
        btnAgregarItem = findViewById(R.id.btnAgregarItem)
        tvResumen = findViewById(R.id.tvResumen)
        containerItemsPreview = findViewById(R.id.containerItemsPreview)
        btnEliminarItem = findViewById(R.id.btnEliminarItem)
        containerVidrio2 = findViewById(R.id.containerVidrio2)
        labelOrigenCorte = findViewById(R.id.labelOrigenCorte)
        etCodigoForma = findViewById(R.id.etCodigoForma)

        initFecha()
        initDatePicker()
        tvResumen.text = getString(R.string.preview_hint)

        // Cargar maestros desde CSV (si está ok)
        loadMastersFromCsv()
        setupSpinners()

        // BOTONES
        btnPreviewPdf.setOnClickListener { onPreviewPdf() }
        btnGuardarEnviar.setOnClickListener { onGuardarYEnviar() }
        btnEliminarItem.setOnClickListener { onDeleteSelectedItem() }

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

        // Check if launched from RecordsActivity to load a specific order
        intent.getStringExtra(EXTRA_LOAD_PEDIDO)?.let { numeroPedido ->
            loadOrderIntoForm(numeroPedido)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(EXTRA_LOAD_PEDIDO)?.let { numeroPedido ->
            loadOrderIntoForm(numeroPedido)
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

        val sectorDestinoAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sectorList
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spSectorDestino.adapter = sectorDestinoAdapter

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
        etCliente.addTextChangedListener(textWatcher)
        etCodigoForma.addTextChangedListener(textWatcher)

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
        spSectorDestino.onItemSelectedListener = spinnerListener

        // Checkbox listeners
        val checkboxListener = { _: android.widget.CompoundButton, _: Boolean ->
            tryUpdatePreview()
        }

        cbPulido.setOnCheckedChangeListener(checkboxListener)
        cbTemplado.setOnCheckedChangeListener(checkboxListener)
        cbPulidoCara2.setOnCheckedChangeListener(checkboxListener)
        cbTempladoCara2.setOnCheckedChangeListener(checkboxListener)
        cbDvh.setOnCheckedChangeListener(checkboxListener)
        cbAtencionForma.setOnCheckedChangeListener(checkboxListener)

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
            val sectorDestino = spSectorDestino.selectedItem?.toString()
            val tipologia = spTipologia.selectedItem?.toString()
            val material = spMaterial.selectedItem?.toString()
            val motivo = etMotivo.text.toString().trim()
            val alto = etAlto.text.toString().trim()
            val ancho = etAncho.text.toString().trim()
            val cliente = etCliente.text.toString().trim()

            val pulidoCara1 = cbPulido.isChecked
            val templadoCara1 = cbTemplado.isChecked
            val pulidoCara2 = cbPulidoCara2.isChecked
            val templadoCara2 = cbTempladoCara2.isChecked
            val yaEsDvh = cbDvh.isChecked
            val atencionForma = cbAtencionForma.isChecked
            val codigoForma = etCodigoForma.text.toString().trim()

            val origenCorte = when (rgOrigenCorte.checkedRadioButtonId) {
                rbFloat.id -> "Cortar de Float"
                rbLaminado.id -> "Cortar de Laminado"
                else -> "Cortar de Laminado"
            }

            val record = ReposicionRecord(
                fecha = fecha,
                numeroPedido = numeroPedido,
                cliente = cliente,
                responsable = responsable,
                sector = sector,
                sectorDestino = sectorDestino,
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
                atencionVidrioForma = atencionForma,
                codigoForma = codigoForma,
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

    private fun updateResumen(record: ReposicionRecord) {
        // Count existing items for this order
        val existingItems = dbHelper.getItemsByOrderNumber(record.numeroPedido)

        // Clear container
        containerItemsPreview.removeAllViews()
        selectedItemId = null
        btnEliminarItem.isEnabled = false

        if (existingItems.isNotEmpty()) {
            existingItems.forEachIndexed { index, item ->
                // Inflate a simple TextView or custom view
                val itemLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(16, 16, 16, 16)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    isClickable = true
                    isFocusable = true
                    background = android.graphics.drawable.ColorDrawable(0x00000000) // Transparent default
                }

                val title = TextView(this).apply {
                    text = "#${index + 1} · ${item.tipologia ?: "-"} · ${item.material}"
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(resources.getColor(R.color.accent_text, null))
                    textSize = 14f
                }
                
                val dim = if (!item.ancho.isNullOrBlank()) "${item.ancho}x${item.alto}" else ""
                val formaText = if (item.atencionVidrioForma) {
                    " · ¡FORMA!${if (!item.codigoForma.isNullOrBlank()) " (${item.codigoForma})" else ""}"
                } else ""
                val subtitle = TextView(this).apply {
                    text = "$dim ${item.origenCorte}$formaText"
                    setTextColor(resources.getColor(R.color.muted_text, null))
                    textSize = 12f
                }

                itemLayout.addView(title)
                itemLayout.addView(subtitle)

                itemLayout.setOnClickListener {
                    // Update selection logic
                    selectedItemId = item.id
                    btnEliminarItem.isEnabled = true
                    
                    // Visual feedback (reset others, highlight this)
                    for (i in 0 until containerItemsPreview.childCount) {
                        val child = containerItemsPreview.getChildAt(i)
                        child.setBackgroundColor(0x00000000)
                    }
                    itemLayout.setBackgroundColor(0xFFE0F2F1.toInt()) // Light teal highlight
                }

                containerItemsPreview.addView(itemLayout)
            }
        }
        
        // Current item (Footer)
        val sb = StringBuilder()
        sb.append("NUEVO ITEM (Editando):\n")
        
        val procCara1 = listOfNotNull(
            if (record.pulidoCara1) "Pulido" else null,
            if (record.templadoCara1) "Templado" else null
        ).joinToString(" · ").ifEmpty { "Sin procesos" }

        val procCara2 = listOfNotNull(
            if (record.pulidoCara2) "Pulido" else null,
            if (record.templadoCara2) "Templado" else null
        ).joinToString(" · ").ifEmpty { "Sin procesos" }

        val medidas = listOfNotNull(
            record.ancho?.takeIf { it.isNotBlank() },
            record.alto?.takeIf { it.isNotBlank() }
        ).joinToString(" x ")
        
        sb.append("${record.tipologia ?: ""} · ${record.material ?: ""}${if (medidas.isNotEmpty()) " · $medidas" else ""} (${record.origenCorte})\n")
        sb.append("Cliente: ${record.cliente ?: "-"}\n")
        sb.append("Vidrio 1: $procCara1 | Vidrio 2: $procCara2\n")
        val formaTexto = if (record.atencionVidrioForma) "¡ATENCIÓN C/FORMA!${if (!record.codigoForma.isNullOrBlank()) " Cod: ${record.codigoForma}" else ""}" else "Recto"
        sb.append("${if (record.yaEsDvh) "Ya es DVH" else "Sin DVH"} · $formaTexto · Resp: ${record.responsable ?: ""} · Sector: ${record.sector ?: ""}\n")
        sb.append("Sector Destino: ${record.sectorDestino ?: "-"}\n")
        sb.append("Motivo: ${record.motivo?.ifEmpty { "-" } ?: "-"}")

        tvResumen.text = sb.toString()
    }

    private fun onDeleteSelectedItem() {
        val id = selectedItemId ?: return
        if (id > 0) {
            dbHelper.deleteRecord(id)
            Toast.makeText(this, "Item eliminado", Toast.LENGTH_SHORT).show()
            tryUpdatePreview()
        }
    }

    // ---------- BOTONES PRINCIPALES ----------

    private fun onGuardarYEnviar() {
        val numeroPedido = etNumeroPedido.text.toString().trim()
        if (numeroPedido.isEmpty()) {
            Toast.makeText(this, "Ingresar N° de pedido", Toast.LENGTH_SHORT).show()
            return
        }

        // Get all items for this order
        val allItems = dbHelper.getItemsByOrderNumber(numeroPedido)
        
        if (allItems.isEmpty()) {
            Toast.makeText(this, "No hay items en este pedido", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate PDF with all items
        val pdfFile = generatePdfForOrder(allItems)
        if (pdfFile != null) {
            sendEmailWithAttachment(pdfFile, "application/pdf")
            Toast.makeText(this, "Pedido #$numeroPedido enviado con ${allItems.size} item(s)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No se pudo generar el PDF", Toast.LENGTH_SHORT).show()
            return
        }

        clearForm()
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
        val numeroPedido = etNumeroPedido.text.toString().trim()
        if (numeroPedido.isEmpty()) {
            Toast.makeText(this, "Ingresar N° de pedido", Toast.LENGTH_SHORT).show()
            return
        }

        val allItems = dbHelper.getItemsByOrderNumber(numeroPedido)

        if (allItems.isEmpty()) {
            Toast.makeText(this, "No hay items guardados para previsualizar", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfFile = generatePdfForOrder(allItems)
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
        val sectorDestino = spSectorDestino.selectedItem?.toString()
        val tipologia = spTipologia.selectedItem?.toString()
        val material = spMaterial.selectedItem?.toString()

        val motivo = etMotivo.text.toString().trim()
        val alto = etAlto.text.toString().trim()
        val ancho = etAncho.text.toString().trim()
        val cliente = etCliente.text.toString().trim()
        val codigoForma = etCodigoForma.text.toString().trim()
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
        val atencionForma = cbAtencionForma.isChecked

        val origenCorte = when (rgOrigenCorte.checkedRadioButtonId) {
            rbFloat.id -> "Cortar de Float"
            rbLaminado.id -> "Cortar de Laminado"
            else -> "Cortar de Laminado"
        }

        val record = ReposicionRecord(
            fecha = fecha,
            numeroPedido = numeroPedido,
            cliente = cliente,
            responsable = responsable,
            sector = sector,
            sectorDestino = sectorDestino,
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
            atencionVidrioForma = atencionForma,
            codigoForma = codigoForma,
            origenCorte = origenCorte
        )

        updateResumen(record)
        return record
    }

    // ---------- LIMPIAR FORMULARIO (NUEVO PEDIDO) ----------

    private fun clearForm() {
        initFecha()
        etNumeroPedido.text.clear()
        etCliente.text.clear()
        etAlto.text.clear()
        etAncho.text.clear()
        etMotivo.text.clear()
        etCodigoForma.text.clear()

        if (spResponsable.adapter != null && spResponsable.adapter.count > 0) {
            spResponsable.setSelection(0)
        }
        if (spSector.adapter != null && spSector.adapter.count > 0) {
            spSector.setSelection(0)
        }
        if (spMaterial.adapter != null && spMaterial.adapter.count > 0) {
            spMaterial.setSelection(0)
        }
        if (spSectorDestino.adapter != null && spSectorDestino.adapter.count > 0) {
            spSectorDestino.setSelection(0)
        }
        if (spTipologia.adapter != null && spTipologia.adapter.count > 0) {
            spTipologia.setSelection(0)
        }

        cbPulido.isChecked = false
        cbTemplado.isChecked = false
        cbPulidoCara2.isChecked = false
        cbTempladoCara2.isChecked = false
        cbDvh.isChecked = false
        cbAtencionForma.isChecked = false

        rbFloat.isChecked = true

        // Limpiar vista previa de items y resumen
        containerItemsPreview.removeAllViews()
        selectedItemId = null
        btnEliminarItem.isEnabled = false
        tvResumen.text = getString(R.string.preview_hint)
    }

    private fun clearItemFields() {
        etAlto.text.clear()
        etAncho.text.clear()
        etMotivo.text.clear()
        etCodigoForma.text.clear()

        if (spSectorDestino.adapter != null && spSectorDestino.adapter.count > 0) {
            spSectorDestino.setSelection(0)
        }
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
        cbAtencionForma.isChecked = false

        rbFloat.isChecked = true
        
        // No limpiamos nro pedido, responsable, sector ni fecha
        // Update preview to show saved items and empty current item
        tryUpdatePreview()
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
            canvas.drawText("Cliente: ${record.cliente ?: "-"}", 40f, y, paint); y += 18f
            canvas.drawText("Responsable: ${record.responsable ?: ""}", 40f, y, paint); y += 18f
            canvas.drawText("Sector: ${record.sector ?: ""}", 40f, y, paint); y += 18f
            canvas.drawText("Sector Destino: ${record.sectorDestino ?: ""}", 40f, y, paint); y += 18f
            canvas.drawText("Tipología: ${record.tipologia ?: ""}", 40f, y, paint); y += 18f
            canvas.drawText("Material: ${record.material ?: ""}", 40f, y, paint); y += 18f
            if (!record.alto.isNullOrBlank() || !record.ancho.isNullOrBlank()) {
                canvas.drawText(
                    "Medidas: ${record.ancho.orEmpty()} x ${record.alto.orEmpty()}",
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
                40f, y, paint
            ); y += 18f
            
            if (record.atencionVidrioForma) {
                val txt = if (!record.codigoForma.isNullOrBlank()) "¡ATENCIÓN C/FORMA! Cod: ${record.codigoForma}" else "¡ATENCIÓN C/FORMA!"
                canvas.drawText(txt, 40f, y, Paint(paint).apply { 
                    color = android.graphics.Color.RED
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 14f
                })
                y += 24f
            }
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

    private fun generatePdfForOrder(items: List<ReposicionRecord>): File? {
        if (items.isEmpty()) return null
        
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 11f
            }
            val titlePaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subtitlePaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var y = 40f
            val firstItem = items.first()

            canvas.drawText("Fontela Cristales - Reposición interna", 40f, y, titlePaint)
            y += 25f

            canvas.drawText("Pedido #${firstItem.numeroPedido} - ${firstItem.fecha}", 40f, y, subtitlePaint)
            y += 18f
            canvas.drawText("Cliente: ${firstItem.cliente ?: "-"}", 40f, y, paint)
            y += 18f
            canvas.drawText("Responsable: ${firstItem.responsable ?: ""} | Sector: ${firstItem.sector ?: ""}", 40f, y, paint)
            y += 20f
            
            // Note: Sector Destino is per item, so it goes in item list below

            canvas.drawText("Items (${items.size}):", 40f, y, subtitlePaint)
            y += 15f

            items.forEachIndexed { index, item ->
                if (y > 750f) {
                    // Finish current page and start a new one
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    canvas.drawText("Fontela Cristales - Continuación", 40f, 30f, titlePaint)
                    y = 70f
                }

                canvas.drawText("${index + 1}. ${item.tipologia ?: ""} - ${item.material ?: ""}", 50f, y, subtitlePaint)
                y += 14f

                if (!item.alto.isNullOrBlank() || !item.ancho.isNullOrBlank()) {
                    canvas.drawText("   Medidas: ${item.ancho.orEmpty()} x ${item.alto.orEmpty()}", 50f, y, paint)
                    y += 14f
                }

                if (!item.sectorDestino.isNullOrBlank()) {
                    canvas.drawText("   Sector Destino: ${item.sectorDestino}", 50f, y, paint)
                    y += 14f
                }

                val procV1 = listOfNotNull(
                    if (item.pulidoCara1) "Pulido" else null,
                    if (item.templadoCara1) "Templado" else null
                ).joinToString(", ")
                
                if (procV1.isNotEmpty()) {
                    canvas.drawText("   Vidrio 1: $procV1", 50f, y, paint)
                    y += 14f
                }

                val procV2 = listOfNotNull(
                    if (item.pulidoCara2) "Pulido" else null,
                    if (item.templadoCara2) "Templado" else null
                ).joinToString(", ")
                
                if (procV2.isNotEmpty()) {
                    canvas.drawText("   Vidrio 2: $procV2", 50f, y, paint)
                    y += 14f
                }

                if (item.yaEsDvh) {
                    canvas.drawText("   Ya es DVH", 50f, y, paint)
                    y += 14f
                }

                canvas.drawText("   ${item.origenCorte}", 50f, y, paint)
                y += 14f

                if (item.atencionVidrioForma) {
                    val txt = if (!item.codigoForma.isNullOrBlank()) "   ¡ATENCIÓN C/FORMA! Cod: ${item.codigoForma}" else "   ¡ATENCIÓN C/FORMA!"
                    canvas.drawText(txt, 50f, y, Paint(paint).apply { color = android.graphics.Color.RED; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
                    y += 14f
                }

                if (!item.motivo.isNullOrBlank()) {
                    canvas.drawText("   Motivo: ${item.motivo}", 50f, y, paint)
                    y += 14f
                }

                y += 8f
            }

            pdfDocument.finishPage(page)

            val fileName = "reposicion_${firstItem.numeroPedido}_${System.currentTimeMillis()}.pdf"
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

        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar email..."))
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "No hay app de email instalada", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir el email: ${e.message}", Toast.LENGTH_LONG).show()
        }
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

    private fun updateResumenLegacy(record: ReposicionRecord) {
        // Count existing items for this order
        val existingItems = dbHelper.getItemsByOrderNumber(record.numeroPedido)

        val sb = StringBuilder()
        
        // Header
        sb.append("${record.fecha} · Pedido ${record.numeroPedido}")
        if (existingItems.isNotEmpty()) {
            sb.append(" (${existingItems.size} guardados)")
        }
        sb.append("\n")

        // List existing items first (compact view)
        if (existingItems.isNotEmpty()) {
            sb.append("--------------------------------------------------\n")
            existingItems.forEachIndexed { index, item ->
                val procV1 = if (item.pulidoCara1 || item.templadoCara1) "V1:Procesado" else "V1:Std"
                val procV2 = if (item.pulidoCara2 || item.templadoCara2) "V2:Procesado" else "V2:Std"
                val dim = if (!item.ancho.isNullOrBlank()) "${item.ancho}x${item.alto}" else ""
                
                sb.append("#${index + 1} · ${item.tipologia ?: "-"} · ${item.material} $dim\n")
            }
            sb.append("--------------------------------------------------\n")
            sb.append("NUEVO ITEM (Editando):\n")
        }

        // Current item details
        val procCara1 = listOfNotNull(
            if (record.pulidoCara1) "Pulido" else null,
            if (record.templadoCara1) "Templado" else null
        ).joinToString(" · ").ifEmpty { "Sin procesos" }

        val procCara2 = listOfNotNull(
            if (record.pulidoCara2) "Pulido" else null,
            if (record.templadoCara2) "Templado" else null
        ).joinToString(" · ").ifEmpty { "Sin procesos" }

        val medidas = listOfNotNull(
            record.ancho?.takeIf { it.isNotBlank() },
            record.alto?.takeIf { it.isNotBlank() }
        ).joinToString(" x ")
        
        sb.append("${record.tipologia ?: ""} · ${record.material ?: ""}${if (medidas.isNotEmpty()) " · $medidas" else ""} (${record.origenCorte})\n")
        sb.append("Vidrio 1: $procCara1 | Vidrio 2: $procCara2\n")
        sb.append("${if (record.yaEsDvh) "Ya es DVH" else "Sin DVH"} · Resp: ${record.responsable ?: ""} · Sector: ${record.sector ?: ""}\n")
        sb.append("Motivo: ${record.motivo?.ifEmpty { "-" } ?: "-"}")

        tvResumen.text = sb.toString()
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

    /**
     * Carga un pedido guardado en el formulario principal a partir del número de pedido.
     * Toma los campos de encabezado del primer item (fecha, nro, cliente, responsable, sector)
     * y actualiza el preview con todos los items del pedido.
     */
    private fun loadOrderIntoForm(numeroPedido: String) {
        val items = dbHelper.getItemsByOrderNumber(numeroPedido)
        if (items.isEmpty()) {
            Toast.makeText(this, "No se encontraron items para el pedido #$numeroPedido", Toast.LENGTH_SHORT).show()
            return
        }

        val first = items.first()

        // Populate header fields from the first item
        etFecha.setText(first.fecha)
        etNumeroPedido.setText(first.numeroPedido)
        etCliente.setText(first.cliente ?: "")

        // Set spinners by matching value
        val respIdx = responsableList.indexOfFirst { it == first.responsable }
        if (respIdx >= 0) spResponsable.setSelection(respIdx)

        val sectorIdx = sectorList.indexOfFirst { it == first.sector }
        if (sectorIdx >= 0) spSector.setSelection(sectorIdx)

        // Update preview to show all saved items
        tryUpdatePreview()

        Toast.makeText(
            this,
            "Pedido #$numeroPedido cargado (${items.size} item${if (items.size != 1) "s" else ""}). Podés generar y enviar el PDF.",
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        private const val PREFS_NAME = "reposicion_prefs"
        private const val KEY_PINNED_SHORTCUT_REQUESTED = "pinned_shortcut_requested"
        const val EXTRA_LOAD_PEDIDO = "extra_load_pedido"
    }
}

