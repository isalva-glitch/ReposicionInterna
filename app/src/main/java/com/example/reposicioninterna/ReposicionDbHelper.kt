
package com.example.reposicioninterna

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ReposicionDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_REPOSICION (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FECHA TEXT,
                $COL_NUM_PEDIDO TEXT NOT NULL,
                $COL_RESPONSABLE TEXT,
                $COL_SECTOR TEXT,
                $COL_MATERIAL TEXT,
                $COL_ALTO TEXT,
                $COL_ANCHO TEXT,
                $COL_MOTIVO TEXT,
                $COL_PULIDO_C1 INTEGER,
                $COL_TEMPLADO_C1 INTEGER,
                $COL_PULIDO_C2 INTEGER,
                $COL_TEMPLADO_C2 INTEGER,
                $COL_DVH INTEGER,
                $COL_ORIGEN TEXT,
                $COL_TIMESTAMP INTEGER
            );
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Para simplificar, borramos y recreamos (se pierden registros anteriores)
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REPOSICION")
        onCreate(db)
    }

    fun insertRecord(record: ReposicionRecord): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FECHA, record.fecha)
            put(COL_NUM_PEDIDO, record.numeroPedido)
            put(COL_RESPONSABLE, record.responsable)
            put(COL_SECTOR, record.sector)
            put(COL_MATERIAL, record.material)
            put(COL_ALTO, record.alto)
            put(COL_ANCHO, record.ancho)
            put(COL_MOTIVO, record.motivo)

            put(COL_PULIDO_C1, if (record.pulidoCara1) 1 else 0)
            put(COL_TEMPLADO_C1, if (record.templadoCara1) 1 else 0)
            put(COL_PULIDO_C2, if (record.pulidoCara2) 1 else 0)
            put(COL_TEMPLADO_C2, if (record.templadoCara2) 1 else 0)

            put(COL_DVH, if (record.yaEsDvh) 1 else 0)
            put(COL_ORIGEN, record.origenCorte)
            put(COL_TIMESTAMP, System.currentTimeMillis())
        }
        return db.insert(TABLE_REPOSICION, null, values)
    }

    fun getAllRecords(): List<ReposicionRecord> {
        return getRecordsInternal(null)
    }

    fun getRecentRecords(limit: Int): List<ReposicionRecord> {
        return getRecordsInternal(limit)
    }

    private fun getRecordsInternal(limit: Int?): List<ReposicionRecord> {
        val result = mutableListOf<ReposicionRecord>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_REPOSICION,
            null,
            null,
            null,
            null,
            null,
            "$COL_TIMESTAMP DESC",
            limit?.toString()
        )

        cursor.use {
            while (it.moveToNext()) {
                val fecha = it.getString(it.getColumnIndexOrThrow(COL_FECHA))
                val numPedido = it.getString(it.getColumnIndexOrThrow(COL_NUM_PEDIDO))
                val responsable = it.getString(it.getColumnIndexOrThrow(COL_RESPONSABLE))
                val sector = it.getString(it.getColumnIndexOrThrow(COL_SECTOR))
                val material = it.getString(it.getColumnIndexOrThrow(COL_MATERIAL))
                val alto = it.getString(it.getColumnIndexOrThrow(COL_ALTO))
                val ancho = it.getString(it.getColumnIndexOrThrow(COL_ANCHO))
                val motivo = it.getString(it.getColumnIndexOrThrow(COL_MOTIVO))

                val pulidoC1 = it.getInt(it.getColumnIndexOrThrow(COL_PULIDO_C1)) == 1
                val templadoC1 = it.getInt(it.getColumnIndexOrThrow(COL_TEMPLADO_C1)) == 1
                val pulidoC2 = it.getInt(it.getColumnIndexOrThrow(COL_PULIDO_C2)) == 1
                val templadoC2 = it.getInt(it.getColumnIndexOrThrow(COL_TEMPLADO_C2)) == 1

                val dvh = it.getInt(it.getColumnIndexOrThrow(COL_DVH)) == 1
                val origen = it.getString(it.getColumnIndexOrThrow(COL_ORIGEN))

                result.add(
                    ReposicionRecord(
                        fecha = fecha ?: "",
                        numeroPedido = numPedido ?: "",
                        responsable = responsable,
                        sector = sector,
                        material = material,
                        alto = alto,
                        ancho = ancho,
                        motivo = motivo,
                        pulidoCara1 = pulidoC1,
                        templadoCara1 = templadoC1,
                        pulidoCara2 = pulidoC2,
                        templadoCara2 = templadoC2,
                        yaEsDvh = dvh,
                        origenCorte = origen ?: ""
                    )
                )
            }
        }

        return result
    }


    companion object {
        private const val DATABASE_NAME = "reposicion.db"
        // SUBIR VERSION PARA FORZAR RECREACIÓN
        private const val DATABASE_VERSION = 3
        const val TABLE_REPOSICION = "reposicion"
        const val COL_ID = "id"
        const val COL_FECHA = "fecha"
        const val COL_NUM_PEDIDO = "numero_pedido"
        const val COL_RESPONSABLE = "responsable"
        const val COL_SECTOR = "sector"
        const val COL_MATERIAL = "material"
        const val COL_ALTO = "alto"
        const val COL_ANCHO = "ancho"
        const val COL_MOTIVO = "motivo"

        const val COL_PULIDO_C1 = "pulido_cara1"
        const val COL_TEMPLADO_C1 = "templado_cara1"
        const val COL_PULIDO_C2 = "pulido_cara2"
        const val COL_TEMPLADO_C2 = "templado_cara2"

        const val COL_DVH = "dvh"
        const val COL_ORIGEN = "origen_corte"
        const val COL_TIMESTAMP = "ts"
    }
}