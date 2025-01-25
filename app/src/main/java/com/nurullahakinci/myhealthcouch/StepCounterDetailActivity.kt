package com.nurullahakinci.myhealthcouch

import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StepCounterDetailActivity : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var stepsTextView: TextView
    private lateinit var goalTextView: TextView
    private lateinit var caloriesTextView: TextView
    private lateinit var distanceTextView: TextView
    
    private val dailyGoal = 10000 // Günlük hedef adım sayısı
    private var currentSteps = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_counter_detail)
        
        // Handle IME insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(0, 0, 0, imeInsets.bottom)
            insets
        }
        
        setupToolbar()
        setupViews()
        setupChart()
        loadDummyData() // Gerçek sensör verisi için değiştirilecek
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Step Counter"
    }
    
    private fun setupViews() {
        progressIndicator = findViewById(R.id.progressIndicator)
        stepsTextView = findViewById(R.id.stepsText)
        goalTextView = findViewById(R.id.goalText)
        caloriesTextView = findViewById(R.id.caloriesText)
        distanceTextView = findViewById(R.id.distanceText)
        
        progressIndicator.max = dailyGoal
        goalTextView.text = "Goal: $dailyGoal steps"
    }
    
    private fun setupChart() {
        barChart = findViewById(R.id.stepChart)
        barChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)
            setDrawGridBackground(false)
            
            onChartGestureListener = object : OnChartGestureListener {
                override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
                    parent.requestDisallowInterceptTouchEvent(false)
                }
                override fun onChartLongPressed(me: MotionEvent?) {}
                override fun onChartDoubleTapped(me: MotionEvent?) {
                    animateY(500)
                }
                override fun onChartSingleTapped(me: MotionEvent?) {}
                override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}
                override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
            }
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = ContextCompat.getColor(context, R.color.chart_text)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (value.toInt()) {
                            0 -> "Mon"
                            1 -> "Tue"
                            2 -> "Wed"
                            3 -> "Thu"
                            4 -> "Fri"
                            5 -> "Sat"
                            6 -> "Sun"
                            else -> ""
                        }
                    }
                }
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(context, R.color.chart_grid)
                textColor = ContextCompat.getColor(context, R.color.chart_text)
                axisMinimum = 0f
                axisMaximum = dailyGoal * 1.2f
            }
            axisRight.isEnabled = false
            
            legend.apply {
                isEnabled = true
                textColor = ContextCompat.getColor(context, R.color.chart_text)
                textSize = 12f
            }
            
            animateY(1000)
        }
    }
    
    private fun loadDummyData() {
        // Dummy veriler (gerçek sensör verisi ile değiştirilecek)
        currentSteps = 6500
        updateProgress(currentSteps)
        updateStats(currentSteps)
        updateChart()
    }
    
    private fun updateProgress(steps: Int) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.progress_anim)
        progressIndicator.startAnimation(animation)
        progressIndicator.setProgressCompat(steps, true)
        stepsTextView.text = String.format("%,d", steps)
    }
    
    private fun updateStats(steps: Int) {
        // Yaklaşık kalori hesabı (ortalama bir değer)
        val calories = (steps * 0.04).toInt()
        // Yaklaşık mesafe hesabı (ortalama adım uzunluğu 0.762 metre)
        val distance = (steps * 0.762 / 1000).toFloat()
        
        caloriesTextView.text = String.format("%,d kcal", calories)
        distanceTextView.text = String.format("%.2f km", distance)
    }
    
    private fun updateChart() {
        // Dummy haftalık veriler
        val entries = listOf(
            BarEntry(0f, 8500f),
            BarEntry(1f, 7200f),
            BarEntry(2f, 9100f),
            BarEntry(3f, 6800f),
            BarEntry(4f, 7900f),
            BarEntry(5f, 5500f),
            BarEntry(6f, currentSteps.toFloat())
        )
        
        val dataSet = BarDataSet(entries, "Daily Steps").apply {
            color = ContextCompat.getColor(this@StepCounterDetailActivity, R.color.primary)
            setDrawValues(true)
            valueTextSize = 10f
            valueTextColor = ContextCompat.getColor(this@StepCounterDetailActivity, R.color.chart_text)
        }
        
        barChart.data = BarData(dataSet)
        barChart.invalidate()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 