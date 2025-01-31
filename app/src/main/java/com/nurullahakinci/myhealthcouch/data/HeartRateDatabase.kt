package com.nurullahakinci.myhealthcouch.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import com.nurullahakinci.myhealthcouch.utils.LocalDateTimeAdapter

class HeartRateDatabase(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("heart_rate_db", Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    fun insertHeartRate(value: Float, timestamp: LocalDateTime) {
        val entries = getAllHeartRates().toMutableList()
        entries.add(HeartRateEntry(timestamp, value))
        saveEntries(entries)
    }

    fun getAllHeartRates(): List<HeartRateEntry> {
        val json = sharedPreferences.getString("heart_rate_entries", "[]")
        val type = object : TypeToken<List<HeartRateEntry>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveEntries(entries: List<HeartRateEntry>) {
        val json = gson.toJson(entries)
        sharedPreferences.edit().putString("heart_rate_entries", json).apply()
    }

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
} 