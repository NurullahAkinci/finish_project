package com.nurullahakinci.myhealthcouch

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.nurullahakinci.myhealthcouch.adapters.WaterTipsAdapter
import com.nurullahakinci.myhealthcouch.data.WaterDatabase
import com.nurullahakinci.myhealthcouch.data.WaterEntry
import com.nurullahakinci.myhealthcouch.models.WaterTip
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class WaterConsumptionDetailActivity : AppCompatActivity() {
    private lateinit var database: WaterDatabase
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var currentConsumptionText: TextView
    private lateinit var remainingText: TextView
    private lateinit var weeklyChart: BarChart
    private lateinit var tipsRecyclerView: RecyclerView
    private var waterEntries = mutableListOf<WaterEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_consumption_detail)
        
        setupToolbar()
        initializeViews()
        setupChips()
        setupChart()
        setupTipsRecyclerView()
        
        database = WaterDatabase(this)
        loadDataFromDatabase()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Water Consumption"
        }
    }

    private fun initializeViews() {
        progressIndicator = findViewById(R.id.waterProgressIndicator)
        currentConsumptionText = findViewById(R.id.currentConsumptionText)
        remainingText = findViewById(R.id.remainingText)
        weeklyChart = findViewById(R.id.weeklyChart)
        tipsRecyclerView = findViewById(R.id.tipsRecyclerView)

        findViewById<ExtendedFloatingActionButton>(R.id.setGoalFab).setOnClickListener {
            showSetGoalDialog()
        }

        findViewById<MaterialButton>(R.id.customAmountButton).setOnClickListener {
            showCustomAmountDialog()
        }
    }

    private fun setupChips() {
        val amounts = listOf(100, 200, 300, 500)
        amounts.forEach { amount ->
            findViewById<Chip>(resources.getIdentifier("chip${amount}ml", "id", packageName))
                ?.setOnClickListener {
                    addWater(amount)
                }
        }
    }

    private fun setupChart() {
        weeklyChart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            legend.isEnabled = false
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }
            
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(false)
        }
    }

    private fun updateProgress() {
        val dailyGoal = database.getDailyGoal()
        val todayConsumption = getTodayConsumption()
        
        progressIndicator.apply {
            progress = ((todayConsumption.toFloat() / dailyGoal.toFloat()) * 100).toInt()
            max = 100
        }
        
        currentConsumptionText.text = "$todayConsumption / $dailyGoal ml"
        remainingText.text = "${dailyGoal - todayConsumption} ml remaining"
        
        updateChart()
    }

    private fun getTodayConsumption(): Int {
        val now = LocalDateTime.now()
        return waterEntries
            .filter { ChronoUnit.DAYS.between(it.timestamp, now) == 0L }
            .sumOf { it.amount }
    }

    private fun updateChart() {
        val entries = (6 downTo 0).map { daysAgo ->
            val now = LocalDateTime.now()
            val consumption = waterEntries
                .filter { ChronoUnit.DAYS.between(it.timestamp, now) == daysAgo.toLong() }
                .sumOf { it.amount }
            BarEntry(daysAgo.toFloat(), consumption.toFloat())
        }

        val dataSet = BarDataSet(entries, "Daily Consumption").apply {
            color = ContextCompat.getColor(this@WaterConsumptionDetailActivity, R.color.blue_500)
            setDrawValues(false)
        }

        weeklyChart.data = BarData(dataSet)
        weeklyChart.invalidate()
    }

    private fun addWater(amount: Int) {
        database.insertWater(amount)
        loadDataFromDatabase()
    }

    private fun showCustomAmountDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_water, null)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Add Water")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val input = view.findViewById<TextInputEditText>(R.id.waterInput)?.text.toString()
                if (input.isNotEmpty()) {
                    val amount = input.toIntOrNull()
                    if (amount != null && amount > 0) {
                        addWater(amount)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSetGoalDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_set_water_goal, null)
        val input = view.findViewById<TextInputEditText>(R.id.goalInput)
        input?.setText(database.getDailyGoal().toString())
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Set Daily Goal")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val goalText = input?.text.toString()
                if (goalText.isNotEmpty()) {
                    val goal = goalText.toIntOrNull()
                    if (goal != null && goal > 0) {
                        database.setDailyGoal(goal)
                        updateProgress()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadDataFromDatabase() {
        waterEntries = database.getAllEntries().toMutableList()
        updateProgress()
    }

    private fun setupTipsRecyclerView() {
        tipsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WaterConsumptionDetailActivity)
            adapter = WaterTipsAdapter(getWaterTips())
        }
    }

    private fun getWaterTips(): List<WaterTip> = listOf(
        WaterTip(
            "Morning Hydration",
            "Start your day with a glass of water to boost metabolism",
            R.drawable.ic_water
        ),
        WaterTip(
            "Regular Intervals",
            "Drink water every 2 hours to maintain hydration",
            R.drawable.ic_time
        ),
        WaterTip(
            "Exercise Hydration",
            "Increase water intake during and after exercise",
            R.drawable.ic_exercise
        ),
        WaterTip(
            "Meal Times",
            "Have a glass of water before each meal",
            R.drawable.ic_food
        )
    )

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 