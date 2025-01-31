package com.nurullahakinci.myhealthcouch

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.textfield.TextInputEditText
import java.util.*
import kotlin.math.roundToInt

class WaterConsumptionDetailActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var progressText: TextView
    private lateinit var remainingText: TextView
    private lateinit var chart: BarChart
    private lateinit var customAmountInput: TextInputEditText
    private lateinit var tipsRecyclerView: RecyclerView
    private lateinit var achievementsRecyclerView: RecyclerView
    private lateinit var monthlyAverageText: TextView
    private lateinit var bestDayText: TextView
    private lateinit var achievementRateText: TextView
    private var dailyGoal: Int = 2000 // Default 2000ml
    private var currentAmount: Int = 0

    private val healthTips = listOf(
        "Drink water first thing in the morning to boost metabolism",
        "Keep a water bottle with you throughout the day",
        "Drink water before, during, and after exercise",
        "Set reminders to drink water every hour",
        "Drink a glass of water before each meal",
        "Add natural flavors like lemon or cucumber to your water",
        "Replace sugary drinks with water",
        "Monitor your urine color - pale yellow indicates good hydration"
    )

    private val achievements = listOf(
        Achievement("First Drop", "Add water for the first time", 1),
        Achievement("Hydration Rookie", "Reach 50% of daily goal", 5),
        Achievement("Water Master", "Reach 100% of daily goal", 10),
        Achievement("Perfect Week", "Reach daily goal for 7 days", 20),
        Achievement("Hydration Expert", "Reach daily goal for 30 days", 50),
        Achievement("Water Champion", "Drink more than 3000ml in a day", 15),
        Achievement("Early Bird", "Drink water before 8 AM", 5),
        Achievement("Night Owl", "Drink water after 10 PM", 5)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_consumption_detail)

        setupToolbar()
        setupPreferences()
        setupViews()
        setupChart()
        setupHealthTips()
        setupAchievements()
        updateUI()
        updateStatistics()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Water Consumption"
    }

    private fun setupPreferences() {
        prefs = getSharedPreferences("MyHealthCouch", MODE_PRIVATE)
        dailyGoal = prefs.getInt("water_goal", 2000)
        currentAmount = prefs.getInt("water_current_${getCurrentDate()}", 0)
    }

    private fun setupViews() {
        progressText = findViewById(R.id.progressText)
        remainingText = findViewById(R.id.remainingText)
        customAmountInput = findViewById(R.id.customAmountInput)
        monthlyAverageText = findViewById(R.id.monthlyAverageText)
        bestDayText = findViewById(R.id.bestDayText)
        achievementRateText = findViewById(R.id.achievementRateText)

        // Quick Add Buttons
        findViewById<MaterialCardView>(R.id.add100ml).setOnClickListener { addWater(100) }
        findViewById<MaterialCardView>(R.id.add200ml).setOnClickListener { addWater(200) }
        findViewById<MaterialCardView>(R.id.add300ml).setOnClickListener { addWater(300) }
        findViewById<MaterialCardView>(R.id.add500ml).setOnClickListener { addWater(500) }

        // Custom Amount Button
        findViewById<MaterialButton>(R.id.addCustomAmount).setOnClickListener {
            val amount = customAmountInput.text.toString().toIntOrNull()
            if (amount != null && amount > 0) {
                addWater(amount)
                customAmountInput.text?.clear()
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        // Reset Button
        findViewById<MaterialButton>(R.id.resetButton).setOnClickListener {
            currentAmount = 0
            saveCurrentAmount()
            updateUI()
            Toast.makeText(this, "Progress reset for today", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupHealthTips() {
        tipsRecyclerView = findViewById(R.id.tipsRecyclerView)
        tipsRecyclerView.layoutManager = LinearLayoutManager(this)
        tipsRecyclerView.adapter = HealthTipsAdapter(healthTips)
    }

    private fun setupAchievements() {
        achievementsRecyclerView = findViewById(R.id.achievementsRecyclerView)
        achievementsRecyclerView.layoutManager = LinearLayoutManager(this)
        achievementsRecyclerView.adapter = AchievementsAdapter(achievements, checkAchievements())
    }

    private fun setupChart() {
        chart = findViewById(R.id.waterChart)
        
        // Chart styling
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)
        
        val entries = ArrayList<BarEntry>()
        // Son 7 günün verilerini yükle
        for (i in 6 downTo 0) {
            val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = getDateString(date)
            val amount = prefs.getInt("water_current_$dateStr", 0)
            entries.add(BarEntry((6-i).toFloat(), amount.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Water Consumption (ml)")
        dataSet.color = resources.getColor(R.color.primary, theme)

        val data = BarData(dataSet)
        data.setValueTextSize(12f)
        
        chart.data = data
        chart.invalidate()
    }

    private fun updateStatistics() {
        // Monthly Average
        val monthlyTotal = getMonthlyTotal()
        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthlyAverage = monthlyTotal / daysInMonth
        monthlyAverageText.text = "${monthlyAverage}ml"

        // Best Day
        val bestDay = getBestDay()
        bestDayText.text = "${bestDay}ml"

        // Achievement Rate
        val achievementRate = calculateAchievementRate()
        achievementRateText.text = "$achievementRate%"
    }

    private fun getMonthlyTotal(): Int {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        var total = 0

        for (day in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = getDateString(calendar)
            total += prefs.getInt("water_current_$dateStr", 0)
        }

        return total
    }

    private fun getBestDay(): Int {
        val calendar = Calendar.getInstance()
        var bestAmount = 0

        for (day in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = getDateString(calendar)
            val amount = prefs.getInt("water_current_$dateStr", 0)
            if (amount > bestAmount) {
                bestAmount = amount
            }
        }

        return bestAmount
    }

    private fun calculateAchievementRate(): Int {
        val calendar = Calendar.getInstance()
        var daysAchieved = 0
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = getDateString(calendar)
            val amount = prefs.getInt("water_current_$dateStr", 0)
            if (amount >= dailyGoal) {
                daysAchieved++
            }
        }

        return ((daysAchieved.toFloat() / daysInMonth.toFloat()) * 100).roundToInt()
    }

    private fun checkAchievements(): List<Boolean> {
        return achievements.map { achievement ->
            when (achievement.title) {
                "First Drop" -> currentAmount > 0
                "Hydration Rookie" -> (currentAmount.toFloat() / dailyGoal.toFloat()) >= 0.5f
                "Water Master" -> currentAmount >= dailyGoal
                "Perfect Week" -> hasAchievedGoalForDays(7)
                "Hydration Expert" -> hasAchievedGoalForDays(30)
                "Water Champion" -> currentAmount > 3000
                "Early Bird" -> hasAddedWaterBeforeTime(8)
                "Night Owl" -> hasAddedWaterAfterTime(22)
                else -> false
            }
        }
    }

    private fun hasAchievedGoalForDays(days: Int): Boolean {
        val calendar = Calendar.getInstance()
        var consecutiveDays = 0

        for (i in 0 until days) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val dateStr = getDateString(calendar)
            val amount = prefs.getInt("water_current_$dateStr", 0)
            if (amount >= dailyGoal) {
                consecutiveDays++
            } else {
                break
            }
        }

        return consecutiveDays >= days
    }

    private fun hasAddedWaterBeforeTime(hour: Int): Boolean {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < hour && currentAmount > 0
    }

    private fun hasAddedWaterAfterTime(hour: Int): Boolean {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= hour && currentAmount > 0
    }

    private fun addWater(amount: Int) {
        currentAmount += amount
        saveCurrentAmount()
        updateUI()
        updateStatistics()
        setupAchievements() // Başarıları güncelle
        Toast.makeText(this, "${amount}ml added", Toast.LENGTH_SHORT).show()
    }

    private fun saveCurrentAmount() {
        prefs.edit().putInt("water_current_${getCurrentDate()}", currentAmount).apply()
        setupChart() // Grafiği güncelle
    }

    private fun updateUI() {
        val progress = (currentAmount.toFloat() / dailyGoal.toFloat() * 100).roundToInt()
        progressText.text = "${currentAmount}ml / ${dailyGoal}ml"
        remainingText.text = "Progress: $progress%"
    }

    private fun getCurrentDate(): String {
        return getDateString(Calendar.getInstance())
    }

    private fun getDateString(calendar: Calendar): String {
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

data class Achievement(
    val title: String,
    val description: String,
    val points: Int
)

class AchievementsAdapter(
    private val achievements: List<Achievement>,
    private val achieved: List<Boolean>
) : RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.achievementTitle)
        val descriptionText: TextView = view.findViewById(R.id.achievementDescription)
        val pointsText: TextView = view.findViewById(R.id.pointsText)
        val icon: ImageView = view.findViewById(R.id.achievementIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.titleText.text = achievement.title
        holder.descriptionText.text = achievement.description
        holder.pointsText.text = "${achievement.points}p"
        
        if (achieved[position]) {
            holder.icon.setColorFilter(holder.itemView.context.getColor(R.color.primary))
            holder.itemView.alpha = 1.0f
        } else {
            holder.icon.setColorFilter(holder.itemView.context.getColor(R.color.text_secondary))
            holder.itemView.alpha = 0.6f
        }
    }

    override fun getItemCount() = achievements.size
}

class HealthTipsAdapter(private val tips: List<String>) : 
    RecyclerView.Adapter<HealthTipsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tipText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_tip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = tips[position]
    }

    override fun getItemCount() = tips.size
} 