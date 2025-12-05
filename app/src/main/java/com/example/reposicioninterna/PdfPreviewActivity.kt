package com.example.reposicioninterna

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class PdfPreviewActivity : AppCompatActivity() {

    private var pdfFile: File? = null
    private var parcelDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_preview)

        val previewImage: ImageView = findViewById(R.id.ivPdfPreview)
        val btnSend: Button = findViewById(R.id.btnSendPdf)
        val btnOpenExternal: Button = findViewById(R.id.btnOpenExternal)
        val btnClose: Button = findViewById(R.id.btnClosePreview)

        val path = intent.getStringExtra(EXTRA_PDF_PATH)
        if (path.isNullOrBlank()) {
            Toast.makeText(this, R.string.preview_missing_path, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val file = File(path)
        if (!file.exists()) {
            Toast.makeText(this, R.string.preview_file_missing, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        pdfFile = file
        renderFirstPage(previewImage)

        btnSend.setOnClickListener { sharePdf() }
        btnOpenExternal.setOnClickListener { openWithOtherApp() }
        btnClose.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        currentPage?.close()
        pdfRenderer?.close()
        parcelDescriptor?.close()
        super.onDestroy()
    }

    private fun renderFirstPage(target: ImageView) {
        val file = pdfFile ?: return
        try {
            parcelDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelDescriptor!!)
            if (pdfRenderer?.pageCount == 0) {
                Toast.makeText(this, R.string.preview_empty, Toast.LENGTH_SHORT).show()
                return
            }

            currentPage?.close()
            currentPage = pdfRenderer?.openPage(0)
            val page = currentPage ?: return

            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            target.setImageBitmap(bitmap)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(this, R.string.preview_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun sharePdf() {
        val file = pdfFile ?: return
        try {
            val authority = "${applicationContext.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(this, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("claudia@fontela.com.ar"))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_pdf))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body_pdf))
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, getString(R.string.email_chooser_pdf)))
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(this, R.string.preview_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun openWithOtherApp() {
        val file = pdfFile ?: return
        val authority = "${applicationContext.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(this, authority, file)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(viewIntent)
        } catch (ex: Exception) {
            Toast.makeText(this, R.string.preview_no_viewer, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val EXTRA_PDF_PATH = "extra_pdf_path"
    }
}