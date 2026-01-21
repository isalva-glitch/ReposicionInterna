package com.example.reposicioninterna

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class RecordsAdapter(
    context: Context,
    records: List<ReposicionRecord>
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
        
        // Ensure visibility
        tvMaterial.visibility = View.VISIBLE
        tvProcesos.visibility = View.VISIBLE
        tvMotivo.visibility = View.VISIBLE

        // Check if previous item is from same order
        val previousRecord = if (position > 0) getItem(position - 1) else null
        val isSameOrder = previousRecord != null && previousRecord.numeroPedido == record?.numeroPedido

        if (isSameOrder) {
            tvPedido.visibility = View.GONE
            tvResponsable.visibility = View.GONE
        } else {
            tvPedido.visibility = View.VISIBLE
            tvResponsable.visibility = View.VISIBLE
            tvPedido.text = "${record?.fecha ?: ""} · Pedido ${record?.numeroPedido ?: ""}"
            tvResponsable.text = "Resp: ${record?.responsable ?: "-"} · Sector: ${record?.sector ?: "-"}"
        }

        tvMaterial.text = listOfNotNull(
            record?.tipologia,
            record?.material,
            record?.origenCorte,
            listOfNotNull(record?.alto, record?.ancho).takeIf { !it.filter { v -> !v.isNullOrBlank() }.isNullOrEmpty() }?.joinToString(" x ")
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
        tvMotivo.text = "Motivo: ${record?.motivo?.ifEmpty { "-" } ?: "-"}"

        return view
    }
}