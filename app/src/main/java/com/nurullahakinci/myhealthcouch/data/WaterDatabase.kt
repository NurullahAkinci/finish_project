package com.nurullahakinci.myhealthcouch.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nurullahakinci.myhealthcouch.utils.LocalDateTimeAdapter
import java.time.LocalDateTime

class WaterDatabase(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("water_db", Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    fun insertWater(amount: Int, timestamp: LocalDateTime = LocalDateTime.now()) {
        val entries = getAllEntries().toMutableList()
        entries.add(WaterEntry(timestamp, amount))
        saveEntries(entries)
    }

    fun getAllEntries(): List<WaterEntry> {
        val json = sharedPreferences.getString("water_entries", "[]")
        val type = object : TypeToken<List<WaterEntry>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveEntries(entries: List<WaterEntry>) {
        val json = gson.toJson(entries)
        sharedPreferences.edit().putString("water_entries", json).apply()
    }

    fun getDailyGoal(): Int {
        return sharedPreferences.getInt("daily_goal", 2000) // Varsayılan 2000ml
    }

    fun setDailyGoal(goal: Int) {
        sharedPreferences.edit().putInt("daily_goal", goal).apply()
    }

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
} 