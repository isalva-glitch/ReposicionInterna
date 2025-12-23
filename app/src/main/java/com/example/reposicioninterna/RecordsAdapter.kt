package com.example.reposicioninterna

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.android.material.button.MaterialButton

class RecordsAdapter(
    context: Context,
    records: List<ReposicionRecord>,
    private val onDetalle: (ReposicionRecord) -> Unit,
    private val onReenviar: (ReposicionRecord) -> Unit
) : ArrayAdapter<ReposicionRecord>(context, 0, records) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_record, parent, false)

        val record = getItem(position)

        val tvPedido = view.findViewById<TextView>(R.id.tvPedido)
        val tvMaterial = view.findViewById<TextView>(R.id.tvMaterial)
        val tvProcesos = view.findViewById<TextView>(R.id.tvProcesos)
        val tvResponsable = view.findViewById<TextView>(R.id.tvResponsable)
        val tvMotivo = view.findViewById<TextView>(R.id.tvMotivo)
        val btnDetalle = view.findViewById<MaterialButton>(R.id.btnDetalle)
        val btnReenviar = view.findViewById<MaterialButton>(R.id.btnReenviar)

        tvPedido.text = "${record?.fecha ?: ""} · Pedido ${record?.numeroPedido ?: ""}"
        tvMaterial.text = listOfNotNull(
            record?.material,
            record?.origenCorte,
            listOfNotNull(record?.alto, record?.ancho).takeIf { !it.isNullOrEmpty() }?.joinToString(" x ")
        ).joinToString(" · ")
        val cara1 = listOfNotNull(
            "V1",
            if (record?.pulidoCara1 == true) "Pulido" else null,
            if (record?.templadoCara1 == true) "Templado" else null
        ).joinToString(" • ")

        val cara2 = listOfNotNull(
            "V2",
            if (record?.pulidoCara2 == true) "Pulido" else null,
            if (record?.templadoCara2 == true) "Templado" else null
        ).joinToString(" • ")

        tvProcesos.text = "$cara1   |   $cara2"
        tvResponsable.text = "Resp: ${record?.responsable ?: "-"} · Sector: ${record?.sector ?: "-"}"
        tvMotivo.text = "Motivo: ${record?.motivo?.ifEmpty { "-" } ?: "-"}"

        btnDetalle.setOnClickListener { record?.let(onDetalle) }
        btnReenviar.setOnClickListener { record?.let(onReenviar) }

        return view
    }
}
