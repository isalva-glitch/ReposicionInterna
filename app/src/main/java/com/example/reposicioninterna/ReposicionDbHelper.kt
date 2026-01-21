
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
                $COL_CLIENTE TEXT,
                $COL_RESPONSABLE TEXT,
                $COL_SECTOR TEXT,
                $COL_SECTOR_DESTINO TEXT,
                $COL_TIPOLOGIA TEXT,
                $COL_MATERIAL TEXT,
                $COL_ALTO TEXT,
                $COL_ANCHO TEXT,
                $COL_CARA1 TEXT,
                $COL_CARA2 TEXT,
                $COL_MOTIVO TEXT,
                $COL_PULIDO_C1 INTEGER,
                $COL_TEMPLADO_C1 INTEGER,
                $COL_PULIDO_C2 INTEGER,
                $COL_TEMPLADO_C2 INTEGER,
                $COL_DVH INTEGER,
                $COL_ATENCION_FORMA INTEGER,
                $COL_ORIGEN TEXT,
                $COL_PDF_PATH TEXT,
                $COL_TIMESTAMP INTEGER
            );
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) {
            // MIGRATION: Agregamos columnas 'cliente' y 'atencion_c_forma'
            try {
                db.execSQL("ALTER TABLE $TABLE_REPOSICION ADD COLUMN $COL_CLIENTE TEXT")
            } catch (e: Exception) {
                // Ignore if exists
            }
            try {
                db.execSQL("ALTER TABLE $TABLE_REPOSICION ADD COLUMN $COL_ATENCION_FORMA INTEGER DEFAULT 0")
            } catch (e: Exception) {
                // Ignore if exists
            }
        }
        if (oldVersion < 8) {
            // MIGRATION: Agregamos columna 'sector_destino'
            try {
                db.execSQL("ALTER TABLE $TABLE_REPOSICION ADD COLUMN $COL_SECTOR_DESTINO TEXT")
            } catch (e: Exception) {
                // Ignore if exists
            }
        }
    }

    fun insertRecord(record: ReposicionRecord): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FECHA, record.fecha)
            put(COL_NUM_PEDIDO, record.numeroPedido)
            put(COL_CLIENTE, record.cliente)
            put(COL_RESPONSABLE, record.responsable)
            put(COL_SECTOR, record.sector)
            put(COL_SECTOR_DESTINO, record.sectorDestino)
            put(COL_TIPOLOGIA, record.tipologia)
            put(COL_MATERIAL, record.material)
            put(COL_ALTO, record.alto)
            put(COL_ANCHO, record.ancho)
            put(COL_CARA1, record.cara1)
            put(COL_CARA2, record.cara2)
            put(COL_MOTIVO, record.motivo)

            put(COL_PULIDO_C1, if (record.pulidoCara1) 1 else 0)
            put(COL_TEMPLADO_C1, if (record.templadoCara1) 1 else 0)
            put(COL_PULIDO_C2, if (record.pulidoCara2) 1 else 0)
            put(COL_TEMPLADO_C2, if (record.templadoCara2) 1 else 0)

            put(COL_DVH, if (record.yaEsDvh) 1 else 0)
            put(COL_ATENCION_FORMA, if (record.atencionVidrioForma) 1 else 0)
            put(COL_ORIGEN, record.origenCorte)
            put(COL_PDF_PATH, record.pdfPath)
            put(COL_TIMESTAMP, record.timestamp)
        }
        return db.insert(TABLE_REPOSICION, null, values)
    }

    fun getAllRecords(): List<ReposicionRecord> {
        return getRecordsInternal(null)
    }

    fun getRecentRecords(limit: Int): List<ReposicionRecord> {
        return getRecordsInternal(limit)
    }

    fun deleteRecord(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_REPOSICION, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun getItemsByOrderNumber(numeroPedido: String): List<ReposicionRecord> {
        val result = mutableListOf<ReposicionRecord>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_REPOSICION,
            null,
            "$COL_NUM_PEDIDO = ?",
            arrayOf(numeroPedido),
            null,
            null,
            "$COL_TIMESTAMP ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                result.add(cursorToRecord(it))
            }
        }

        return result
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
                result.add(cursorToRecord(it))
            }
        }

        return result
    }

    private fun cursorToRecord(cursor: android.database.Cursor): ReposicionRecord {
        val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA))
        val numPedido = cursor.getString(cursor.getColumnIndexOrThrow(COL_NUM_PEDIDO))
        // Soportar migracion donde la columna puede no existir si algo fallo
        val clienteIdx = cursor.getColumnIndex(COL_CLIENTE)
        val cliente = if (clienteIdx >= 0) cursor.getString(clienteIdx) else null

        val responsable = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESPONSABLE))
        val sector = cursor.getString(cursor.getColumnIndexOrThrow(COL_SECTOR))
        
        // MIGRATION v8
        val sectorDestinoIdx = cursor.getColumnIndex(COL_SECTOR_DESTINO)
        val sectorDestino = if (sectorDestinoIdx >= 0) cursor.getString(sectorDestinoIdx) else null

        val tipologia = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPOLOGIA))
        val material = cursor.getString(cursor.getColumnIndexOrThrow(COL_MATERIAL))
        val alto = cursor.getString(cursor.getColumnIndexOrThrow(COL_ALTO))
        val ancho = cursor.getString(cursor.getColumnIndexOrThrow(COL_ANCHO))
        val cara1 = cursor.getString(cursor.getColumnIndexOrThrow(COL_CARA1))
        val cara2 = cursor.getString(cursor.getColumnIndexOrThrow(COL_CARA2))
        val motivo = cursor.getString(cursor.getColumnIndexOrThrow(COL_MOTIVO))

        val pulidoC1 = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PULIDO_C1)) == 1
        val templadoC1 = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEMPLADO_C1)) == 1
        val pulidoC2 = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PULIDO_C2)) == 1
        val templadoC2 = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEMPLADO_C2)) == 1

        val dvh = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DVH)) == 1
        
        val atencionFormaIdx = cursor.getColumnIndex(COL_ATENCION_FORMA)
        val atencionForma = if (atencionFormaIdx >= 0) cursor.getInt(atencionFormaIdx) == 1 else false

        val origen = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORIGEN))
        val pdfPath = cursor.getString(cursor.getColumnIndexOrThrow(COL_PDF_PATH))
        val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))

        return ReposicionRecord(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            fecha = fecha ?: "",
            numeroPedido = numPedido ?: "",
            cliente = cliente,
            responsable = responsable,
            sector = sector,
            sectorDestino = sectorDestino,
            tipologia = tipologia,
            material = material,
            alto = alto,
            ancho = ancho,
            cara1 = cara1,
            cara2 = cara2,
            motivo = motivo,
            pulidoCara1 = pulidoC1,
            templadoCara1 = templadoC1,
            pulidoCara2 = pulidoC2,
            templadoCara2 = templadoC2,
            yaEsDvh = dvh,
            atencionVidrioForma = atencionForma,
            origenCorte = origen ?: "",
            pdfPath = pdfPath,
            timestamp = timestamp
        )
    }


    companion object {
        private const val DATABASE_NAME = "reposicion.db"
        // SUBIR VERSION PARA MIGRACION
        private const val DATABASE_VERSION = 8
        const val TABLE_REPOSICION = "reposicion"
        const val COL_ID = "id"
        const val COL_FECHA = "fecha"
        const val COL_NUM_PEDIDO = "numero_pedido"
        const val COL_CLIENTE = "cliente"
        const val COL_RESPONSABLE = "responsable"
        const val COL_SECTOR = "sector"
        const val COL_SECTOR_DESTINO = "sector_destino"
        const val COL_TIPOLOGIA = "tipologia"
        const val COL_MATERIAL = "material"
        const val COL_ALTO = "alto"
        const val COL_ANCHO = "ancho"
        const val COL_CARA1 = "cara1"
        const val COL_CARA2 = "cara2"
        const val COL_MOTIVO = "motivo"

        const val COL_PULIDO_C1 = "pulido_cara1"
        const val COL_TEMPLADO_C1 = "templado_cara1"
        const val COL_PULIDO_C2 = "pulido_cara2"
        const val COL_TEMPLADO_C2 = "templado_cara2"

        const val COL_DVH = "dvh"
        const val COL_ATENCION_FORMA = "atencion_c_forma"
        const val COL_ORIGEN = "origen_corte"
        const val COL_PDF_PATH = "pdf_path"
        const val COL_TIMESTAMP = "ts"
    }
}
