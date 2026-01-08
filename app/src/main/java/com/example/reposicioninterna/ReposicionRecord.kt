package com.example.reposicioninterna

data class ReposicionRecord(
    val id: Long = 0,
    val fecha: String,
    val numeroPedido: String,
    val responsable: String?,
    val sector: String?,
    val material: String?,
    val alto: String? = null,
    val ancho: String? = null,
    val cara1: String? = null,
    val cara2: String? = null,
    val motivo: String?,
    val pulidoCara1: Boolean,
    val templadoCara1: Boolean,
    val pulidoCara2: Boolean,
    val templadoCara2: Boolean,
    val yaEsDvh: Boolean,
    val origenCorte: String,
    val pdfPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
