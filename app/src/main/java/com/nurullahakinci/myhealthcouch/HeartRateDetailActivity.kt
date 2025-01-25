package com.nurullahakinci.myhealthcouch

import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
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

class HeartRateDetailActivity : AppCompatActivity() {
    private lateinit var lineChart: LineChart
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
        setupViews()
        setupChart()
        
        database = HeartRateDatabase(this)
        loadDataFromDatabase()
        
        setupAddButton()
        setupTimeRangeCards()
    }
    
    private fun setupViews() {
        averageTextView = findViewById(R.id.averageHeartRate)
        maxTextView = findViewById(R.id.maxHeartRate)
        minTextView = findViewById(R.id.minHeartRate)
        dailyAverageTextView = findViewById(R.id.dailyAverage)
        weeklyAverageTextView = findViewById(R.id.weeklyAverage)
        monthlyAverageTextView = findViewById(R.id.monthlyAverage)
        updateStats()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Heart Rate Details"
    }
    
    private fun setupChart() {
        lineChart = findViewById(R.id.heartRateChart)
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            
            // Enable double tap to zoom
            isDoubleTapToZoomEnabled = true
            
            // Set minimum scale factor
            setScaleMinima(1f, 1f)
            
            // Set gesture listener
            onChartGestureListener = object : OnChartGestureListener {
                override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
                    // Gesture başladığında yapılacak işlemler
                }
                override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
                    // Gesture bittiğinde yapılacak işlemler
                }
                override fun onChartLongPressed(me: MotionEvent?) {
                    // Uzun basıldığında yapılacak işlemler
                }
                override fun onChartDoubleTapped(me: MotionEvent?) {
                    // Çift tıklandığında yapılacak işlemler
                    animateX(500)
                }
                override fun onChartSingleTapped(me: MotionEvent?) {
                    // Tek tıklandığında yapılacak işlemler
                    animateY(500)
                }
                override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
                    // Fling hareketi yapıldığında yapılacak işlemler
                }
                override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
                    // Ölçeklendirme yapıldığında yapılacak işlemler
                }
                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                    // Grafik kaydırıldığında yapılacak işlemler
                }
            }
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelRotationAngle = -45f
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toInt() in timestamps.indices) {
                            val formatter = when (selectedTimeRange) {
                                TimeRange.DAILY -> DateTimeFormatter.ofPattern("HH:mm")
                                TimeRange.WEEKLY -> DateTimeFormatter.ofPattern("MM/dd HH:mm")
                                TimeRange.MONTHLY -> DateTimeFormatter.ofPattern("MM/dd")
                            }
                            timestamps[value.toInt()].format(formatter)
                        } else ""
                    }
                }
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 40f
                axisMaximum = 120f
            }
            axisRight.isEnabled = false

            legend.apply {
                textSize = 12f
                formSize = 12f
                isEnabled = true
            }
        }

        // Chart card'ına tıklama işlevi ekle
        findViewById<MaterialCardView>(R.id.chartCard).setOnClickListener {
            lineChart.animateX(1000)
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
        if (!this::lineChart.isInitialized) return
        
        val filteredData = getFilteredData()
        timestamps.clear()
        val entries = filteredData.mapIndexed { index, entry ->
            timestamps.add(entry.timestamp)
            Entry(index.toFloat(), entry.value)
        }

        val dataSet = LineDataSet(entries, "Heart Rate").apply {
            color = ContextCompat.getColor(this@HeartRateDetailActivity, R.color.primary)
            setCircleColor(ContextCompat.getColor(this@HeartRateDetailActivity, R.color.primary))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(true)
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            
            // Highlight settings
            highLightColor = ContextCompat.getColor(this@HeartRateDetailActivity, R.color.primary)
            setDrawHighlightIndicators(true)
            highlightLineWidth = 1.5f
        }
        
        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
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
                    val timestamp = LocalDateTime.now()
                    database.insertHeartRate(heartRate, timestamp)
                    heartRateData.add(HeartRateEntry(timestamp, heartRate))
                    updateChartData()
                    updateStats()
                }
                dialog.dismiss()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 