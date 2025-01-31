package com.nurullahakinci.myhealthcouch

import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.nurullahakinci.myhealthcouch.data.HeartRateDatabase
import com.nurullahakinci.myhealthcouch.data.HeartRateEntry
import android.widget.ImageButton
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.SharedPreferences
import com.nurullahakinci.myhealthcouch.formatter.DateAxisValueFormatter
import java.time.ZoneOffset
import java.time.Instant
import com.nurullahakinci.myhealthcouch.adapters.HealthTipsAdapter
import com.nurullahakinci.myhealthcouch.models.HealthTip

class HeartRateDetailActivity : AppCompatActivity() {
    private lateinit var heartRateChart: LineChart
    private lateinit var tipsRecyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: HeartRateDatabase
    private var heartRateData = mutableListOf<HeartRateEntry>()
    private lateinit var averageTextView: TextView
    private lateinit var maxTextView: TextView
    private lateinit var minTextView: TextView
    private lateinit var dailyAverageTextView: TextView
    private lateinit var weeklyAverageTextView: TextView
    private lateinit var monthlyAverageTextView: TextView
    private var selectedTimeRange = TimeRange.DAILY
    private var timestamps = mutableListOf<LocalDateTime>()

    enum class TimeRange {
        DAILY, WEEKLY, MONTHLY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate_detail)
        
        setupToolbar()
        initializeViews()
        setupChart()
        setupTipsRecyclerView()
        updateStats()
        
        database = HeartRateDatabase(this)
        loadDataFromDatabase()
        
        setupAddButton()
        setupTimeRangeCards()
    }
    
    private fun initializeViews() {
        heartRateChart = findViewById(R.id.heartRateChart)
        tipsRecyclerView = findViewById(R.id.tipsRecyclerView)
        sharedPreferences = getSharedPreferences("heart_rate_prefs", MODE_PRIVATE)
        averageTextView = findViewById(R.id.averageHeartRate)
        maxTextView = findViewById(R.id.maxHeartRate)
        minTextView = findViewById(R.id.minHeartRate)
        dailyAverageTextView = findViewById(R.id.dailyAverage)
        weeklyAverageTextView = findViewById(R.id.weeklyAverage)
        monthlyAverageTextView = findViewById(R.id.monthlyAverage)

        // Reset button setup
        findViewById<MaterialButton>(R.id.resetDataButton).setOnClickListener {
            showResetConfirmationDialog()
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Heart Rate"
        }

        findViewById<ImageButton>(R.id.reminderButton).setOnClickListener {
            showReminderDialog()
        }
    }
    
    private fun setupChart() {
        heartRateChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = DateAxisValueFormatter()
            }

            axisRight.isEnabled = false
            
            legend.apply {
                form = Legend.LegendForm.LINE
                textSize = 12f
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            }

            updateChartData()
        }
    }
    
    private fun setupTimeRangeCards() {
        findViewById<MaterialCardView>(R.id.dailyCard).apply {
            setOnClickListener {
                selectedTimeRange = TimeRange.DAILY
                updateChartData()
                updateStats()
                updateCardSelection()
            }
        }

        findViewById<MaterialCardView>(R.id.weeklyCard).apply {
            setOnClickListener {
                selectedTimeRange = TimeRange.WEEKLY
                updateChartData()
                updateStats()
                updateCardSelection()
            }
        }

        findViewById<MaterialCardView>(R.id.monthlyCard).apply {
            setOnClickListener {
                selectedTimeRange = TimeRange.MONTHLY
                updateChartData()
                updateStats()
                updateCardSelection()
            }
        }

        updateCardSelection()
    }

    private fun updateCardSelection() {
        findViewById<MaterialCardView>(R.id.dailyCard).isChecked = selectedTimeRange == TimeRange.DAILY
        findViewById<MaterialCardView>(R.id.weeklyCard).isChecked = selectedTimeRange == TimeRange.WEEKLY
        findViewById<MaterialCardView>(R.id.monthlyCard).isChecked = selectedTimeRange == TimeRange.MONTHLY
    }

    private fun getFilteredData(): List<HeartRateEntry> {
        val now = LocalDateTime.now()
        return when (selectedTimeRange) {
            TimeRange.DAILY -> heartRateData.filter {
                ChronoUnit.DAYS.between(it.timestamp, now) == 0L
            }
            TimeRange.WEEKLY -> heartRateData.filter {
                ChronoUnit.DAYS.between(it.timestamp, now) <= 7
            }
            TimeRange.MONTHLY -> heartRateData.filter {
                ChronoUnit.DAYS.between(it.timestamp, now) <= 30
            }
        }
    }
    
    private fun updateChartData() {
        val entries = getFilteredData().map { heartRateEntry ->
            Entry(heartRateEntry.timestamp.toEpochSecond(ZoneOffset.UTC).toFloat(), heartRateEntry.value)
        }

        val dataSet = LineDataSet(entries, "Heart Rate").apply {
            color = ContextCompat.getColor(this@HeartRateDetailActivity, R.color.purple_500)
            setCircleColor(ContextCompat.getColor(this@HeartRateDetailActivity, R.color.purple_500))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
        }

        heartRateChart.data = LineData(dataSet)
        heartRateChart.invalidate()
    }
    
    private fun updateStats() {
        val filteredData = getFilteredData()
        if (filteredData.isNotEmpty()) {
            val values = filteredData.map { it.value }
            val avg = values.average()
            val max = values.maxOrNull() ?: 0f
            val min = values.minOrNull() ?: 0f
            
            averageTextView.text = "Average: ${String.format("%.1f", avg)} BPM"
            maxTextView.text = "Highest: $max BPM"
            minTextView.text = "Lowest: $min BPM"
        } else {
            averageTextView.text = "Average: -- BPM"
            maxTextView.text = "Highest: -- BPM"
            minTextView.text = "Lowest: -- BPM"
        }

        updateTimeRangeAverages()
    }

    private fun updateTimeRangeAverages() {
        val now = LocalDateTime.now()
        
        // Günlük ortalama
        val dailyData = heartRateData.filter { ChronoUnit.DAYS.between(it.timestamp, now) == 0L }
        val dailyAvg = dailyData.map { it.value }.average()
        dailyAverageTextView.text = if (dailyData.isNotEmpty()) String.format("%.1f BPM", dailyAvg) else "-- BPM"

        // Haftalık ortalama
        val weeklyData = heartRateData.filter { ChronoUnit.DAYS.between(it.timestamp, now) <= 7 }
        val weeklyAvg = weeklyData.map { it.value }.average()
        weeklyAverageTextView.text = if (weeklyData.isNotEmpty()) String.format("%.1f BPM", weeklyAvg) else "-- BPM"

        // Aylık ortalama
        val monthlyData = heartRateData.filter { ChronoUnit.DAYS.between(it.timestamp, now) <= 30 }
        val monthlyAvg = monthlyData.map { it.value }.average()
        monthlyAverageTextView.text = if (monthlyData.isNotEmpty()) String.format("%.1f BPM", monthlyAvg) else "-- BPM"
    }
    
    private fun setupAddButton() {
        findViewById<MaterialButton>(R.id.enterDataButton).setOnClickListener {
            showAddDataDialog()
        }
    }
    
    private fun showAddDataDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(R.layout.dialog_add_heart_rate)
            .create()
        
        dialog.show()
        
        dialog.findViewById<MaterialButton>(R.id.addButton)?.setOnClickListener {
            val input = dialog.findViewById<TextInputEditText>(R.id.heartRateInput)?.text.toString()
            if (input.isNotEmpty()) {
                val heartRate = input.toFloatOrNull()
                if (heartRate != null) {
                    when {
                        heartRate < 40 || heartRate > 250 -> {
                            MaterialAlertDialogBuilder(this)
                                .setTitle("Invalid Heart Rate")
                                .setMessage("Please enter a heart rate between 40 and 250 BPM")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                        else -> {
                            val timestamp = LocalDateTime.now()
                            database.insertHeartRate(heartRate, timestamp)
                            heartRateData.add(HeartRateEntry(timestamp, heartRate))
                            updateChartData()
                            updateStats()
                            dialog.dismiss()
                        }
                    }
                }
            }
        }
        
        dialog.findViewById<MaterialButton>(R.id.cancelButton)?.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun loadDataFromDatabase() {
        heartRateData = database.getAllHeartRates().toMutableList()
        updateChartData()
        updateStats()
    }

    private fun setupTipsRecyclerView() {
        tipsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HeartRateDetailActivity)
            adapter = HealthTipsAdapter(getHealthTips())
        }
    }

    private fun getHealthTips(): List<HealthTip> = listOf(
        HealthTip(
            "Regular Exercise",
            "Aim for at least 150 minutes of moderate exercise per week to maintain a healthy heart rate.",
            R.drawable.ic_exercise
        ),
        HealthTip(
            "Stress Management",
            "Practice relaxation techniques to help regulate your heart rate and reduce stress.",
            R.drawable.ic_stress
        ),
        HealthTip(
            "Sleep Quality",
            "Get 7-9 hours of quality sleep to help maintain a healthy resting heart rate.",
            R.drawable.ic_sleep
        ),
        HealthTip(
            "Hydration",
            "Stay well-hydrated to help your heart pump blood more efficiently.",
            R.drawable.ic_water
        )
    )

    private fun showReminderDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_heart_rate_reminder, null)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Measurement Reminders")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                saveReminderSettings(view)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveReminderSettings(view: View) {
        // Implementation for saving reminder settings
    }

    private fun showResetConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Data")
            .setMessage("Are you sure you want to delete all heart rate data? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                resetAllData()
            }
            .setNegativeButton("Cancel", null)
            .setIcon(R.drawable.ic_delete)
            .show()
    }

    private fun resetAllData() {
        database.clearAllData()
        heartRateData.clear()
        updateChartData()
        updateStats()
        
        // Show success message
        MaterialAlertDialogBuilder(this)
            .setTitle("Success")
            .setMessage("All heart rate data has been deleted.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 