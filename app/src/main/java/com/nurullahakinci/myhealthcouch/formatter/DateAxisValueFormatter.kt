package com.nurullahakinci.myhealthcouch.formatter

import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class DateAxisValueFormatter : ValueFormatter() {
    private val dateFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun getFormattedValue(value: Float): String {
        return try {
            val instant = Instant.ofEpochSecond(value.toLong())
            val dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
            dateFormatter.format(dateTime)
        } catch (e: Exception) {
            value.toString()
        }
    }
} 