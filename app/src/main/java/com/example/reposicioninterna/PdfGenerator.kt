package com.example.reposicioninterna

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    fun generate(context: Context, record: ReposicionRecord): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
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

            canvas.drawText("Fontela Cristales - Reposición interna", 40f, y, titlePaint)
            y += 30f

            canvas.drawText("Fecha: ${record.fecha}", 40f, y, paint); y += 18f
            canvas.drawText("N° Pedido: ${record.numeroPedido}", 40f, y, paint); y += 18f
            canvas.drawText("Responsable: ${record.responsable ?: ""}", 40f, y, paint); y += 18f
            canvas.drawText("Sector: ${record.sector ?: ""}", 40f, y, paint); y += 18f
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

            if (!record.cara1.isNullOrBlank()) {
                canvas.drawText("Notas Vidrio 1:", 40f, y, paint); y += 16f
                canvas.drawText("  ${record.cara1}", 40f, y, paint); y += 12f
            }
            if (!record.cara2.isNullOrBlank()) {
                canvas.drawText("Notas Vidrio 2:", 40f, y, paint); y += 16f
                canvas.drawText("  ${record.cara2}", 40f, y, paint); y += 12f
            }

            canvas.drawText("Motivo:", 40f, y, paint); y += 16f
            canvas.drawText("  ${record.motivo ?: ""}", 40f, y, paint); y += 24f

            canvas.drawText(
                "Ya es DVH: ${if (record.yaEsDvh) "SI" else "NO"}",
                40f,
                y,
                paint
            ); y += 18f
            canvas.drawText("Origen corte: ${record.origenCorte}", 40f, y, paint); y += 18f

            pdfDocument.finishPage(page)

            val safePedido = record.numeroPedido.ifBlank { "sin_numero" }
            val fileName = "reposicion_${safePedido}_${record.timestamp}.pdf"
            val pdfDir = File(context.filesDir, "pdfs")
            if (!pdfDir.exists()) pdfDir.mkdirs()
            val file = File(pdfDir, fileName)
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
}
