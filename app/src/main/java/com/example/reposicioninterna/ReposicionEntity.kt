package com.example.reposicioninterna

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reposicion")
data class ReposicionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val numeroPedido: String,
    val responsable: String?,
    val sector: String?,
    val tipologia: String? = null,
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
) {
    fun toDomain(): ReposicionRecord = ReposicionRecord(
        id = id,
        fecha = fecha,
        numeroPedido = numeroPedido,
        responsable = responsable,
        sector = sector,
        tipologia = tipologia,
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
        origenCorte = origenCorte,
        pdfPath = pdfPath,
        timestamp = timestamp
    )

    companion object {
        fun from(record: ReposicionRecord): ReposicionEntity = ReposicionEntity(
            id = record.id,
            fecha = record.fecha,
            numeroPedido = record.numeroPedido,
            responsable = record.responsable,
            sector = record.sector,
            tipologia = record.tipologia,
            material = record.material,
            alto = record.alto,
            ancho = record.ancho,
            cara1 = record.cara1,
            cara2 = record.cara2,
            motivo = record.motivo,
            pulidoCara1 = record.pulidoCara1,
            templadoCara1 = record.templadoCara1,
            pulidoCara2 = record.pulidoCara2,
            templadoCara2 = record.templadoCara2,
            yaEsDvh = record.yaEsDvh,
            origenCorte = record.origenCorte,
            pdfPath = record.pdfPath,
            timestamp = record.timestamp
        )
    }
}
