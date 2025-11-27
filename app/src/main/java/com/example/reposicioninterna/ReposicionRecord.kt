
package com.example.reposicioninterna

data class ReposicionRecord(
    val fecha: String,
    val numeroPedido: String,
    val responsable: String?,
    val sector: String?,
    val material: String?,
    val cara1: String?,
    val cara2: String?,
    val motivo: String?,

    val pulidoCara1: Boolean,
    val templadoCara1: Boolean,
    val pulidoCara2: Boolean,
    val templadoCara2: Boolean,

    val yaEsDvh: Boolean,
    val origenCorte: String
)
