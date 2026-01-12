package com.example.reposicioninterna

import android.content.Context
import java.util.LinkedHashSet

object MasterDataManager {
    val materialList = mutableListOf<String>()
    val responsableList = mutableListOf<String>()
    val sectorList = mutableListOf<String>()

    fun load(context: Context) {
        materialList.clear()
        responsableList.clear()
        sectorList.clear()

        // Default values in case load fails or lists are empty
        val defaultMaterials = listOf("Float 4mm", "Float 6mm", "Laminado 3+3", "Laminado 4+4")
        val defaultResponsables = listOf("Juan", "Claudia", "Carlos")
        val defaultSectores = listOf("Corte", "Armado", "Templado")

        try {
            val materials = LinkedHashSet<String>()
            val responsables = LinkedHashSet<String>()
            val sectores = LinkedHashSet<String>()

            context.assets.open("maestros_reposicion.csv").bufferedReader().useLines { lines ->
                lines.forEachIndexed { index, rawLine ->
                    val line = rawLine.trim()
                    if (line.isEmpty()) return@forEachIndexed
                    if (index == 0 && line.contains("Material", ignoreCase = true)) return@forEachIndexed

                    val parts = line.split(';', ',').map { it.trim() }
                    val material = parts.getOrNull(0)
                    val responsable = parts.getOrNull(1)
                    val sector = parts.getOrNull(2)

                    if (!material.isNullOrEmpty()) materials.add(material)
                    if (!responsable.isNullOrEmpty()) responsables.add(responsable)
                    if (!sector.isNullOrEmpty()) sectores.add(sector)
                }
            }
            
            materialList.addAll(materials)
            responsableList.addAll(responsables)
            sectorList.addAll(sectores)
            
        } catch (t: Throwable) {
            // Log error but don't crash, will use defaults
            StartupLog.log(context, "MasterDataManager: Error cargando CSV", t)
        }

        // Apply defaults if empty
        if (materialList.isEmpty()) materialList.addAll(defaultMaterials)
        if (responsableList.isEmpty()) responsableList.addAll(defaultResponsables)
        if (sectorList.isEmpty()) sectorList.addAll(defaultSectores)
        
        StartupLog.log(context, "MasterDataManager: Carga completada. Mat: ${materialList.size}, Resp: ${responsableList.size}")
    }
}
