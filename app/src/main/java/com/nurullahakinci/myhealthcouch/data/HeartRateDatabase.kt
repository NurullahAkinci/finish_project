package com.nurullahakinci.myhealthcouch.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HeartRateDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "heartrate.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "heart_rate_entries"
        private const val COLUMN_ID = "id"
        private const val COLUMN_VALUE = "value"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_VALUE REAL NOT NULL,
                $COLUMN_TIMESTAMP TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertHeartRate(value: Float, timestamp: LocalDateTime) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_VALUE, value)
            put(COLUMN_TIMESTAMP, timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllHeartRates(): List<HeartRateEntry> {
        val entries = mutableListOf<HeartRateEntry>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_VALUE, COLUMN_TIMESTAMP),
            null,
            null,
            null,
            null,
            "$COLUMN_TIMESTAMP DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val value = getFloat(getColumnIndexOrThrow(COLUMN_VALUE))
                val timestamp = LocalDateTime.parse(
                    getString(getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                entries.add(HeartRateEntry(timestamp, value))
            }
        }
        cursor.close()
        db.close()
        return entries
    }
}

data class HeartRateEntry(
    val timestamp: LocalDateTime,
    val value: Float
) 