package com.example.reposicioninterna

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ReposicionDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_REPOSICION (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FECHA TEXT,
                $COL_NUM_PEDIDO TEXT NOT NULL,
                $COL_RESPONSABLE TEXT,
                $COL_SECTOR TEXT,
                $COL_MATERIAL TEXT,
                $COL_CARA1 TEXT,
                $COL_CARA2 TEXT,
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
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REPOSICION")
        onCreate(db)
    }

    fun insertRecord(record: ReposicionRecord): Long {
        val values = ContentValues().apply {
            put(COL_FECHA, record.fecha)
            put(COL_NUM_PEDIDO, record.numeroPedido)
            put(COL_RESPONSABLE, record.responsable)
            put(COL_SECTOR, record.sector)
            put(COL_MATERIAL, record.material)
            put(COL_CARA1, record.cara1)
            put(COL_CARA2, record.cara2)
            put(COL_MOTIVO, record.motivo)
            put(COL_PULIDO_C1, if (record.pulidoCara1) 1 else 0)
            put(COL_TEMPLADO_C1, if (record.templadoCara1) 1 else 0)
            put(COL_PULIDO_C2, if (record.pulidoCara2) 1 else 0)
            put(COL_TEMPLADO_C2, if (record.templadoCara2) 1 else 0)
            put(COL_DVH, if (record.yaEsDvh) 1 else 0)
            put(COL_ORIGEN, record.origenCorte)
            put(COL_TIMESTAMP, System.currentTimeMillis())
        }
        return writableDatabase.insert(TABLE_REPOSICION, null, values)
    }

    fun getAllRecords(): List<ReposicionRecord> {
        val out = mutableListOf<ReposicionRecord>()
        val c = readableDatabase.query(
            TABLE_REPOSICION,
            null,
            null,
            null,
            null,
            null,
            "$COL_TIMESTAMP DESC"
        )

        c.use { cur ->
            while (cur.moveToNext()) {
                fun s(col: String): String? = cur.getString(cur.getColumnIndexOrThrow(col))
                fun b(col: String): Boolean = cur.getInt(cur.getColumnIndexOrThrow(col)) == 1

                out.add(
                    ReposicionRecord(
                        fecha = s(COL_FECHA) ?: "",
                        numeroPedido = s(COL_NUM_PEDIDO) ?: "",
                        responsable = s(COL_RESPONSABLE),
                        sector = s(COL_SECTOR),
                        material = s(COL_MATERIAL),
                        cara1 = s(COL_CARA1),
                        cara2 = s(COL_CARA2),
                        motivo = s(COL_MOTIVO),
                        pulidoCara1 = b(COL_PULIDO_C1),
                        templadoCara1 = b(COL_TEMPLADO_C1),
                        pulidoCara2 = b(COL_PULIDO_C2),
                        templadoCara2 = b(COL_TEMPLADO_C2),
                        yaEsDvh = b(COL_DVH),
                        origenCorte = s(COL_ORIGEN) ?: ""
                    )
                )
            }
        }
        return out
    }

    companion object {
        private const val DATABASE_NAME = "reposicion.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_REPOSICION = "reposicion"
        const val COL_ID = "id"
        const val COL_FECHA = "fecha"
        const val COL_NUM_PEDIDO = "numero_pedido"
        const val COL_RESPONSABLE = "responsable"
        const val COL_SECTOR = "sector"
        const val COL_MATERIAL = "material"
        const val COL_CARA1 = "cara1"
        const val COL_CARA2 = "cara2"
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
