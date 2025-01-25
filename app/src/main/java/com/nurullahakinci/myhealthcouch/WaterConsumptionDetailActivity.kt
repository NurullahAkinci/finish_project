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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_consumption_detail)

        setupToolbar()
        setupPreferences()
        setupViews()
        setupChart()
        setupHealthTips()
        updateUI()
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

    private fun addWater(amount: Int) {
        currentAmount += amount
        saveCurrentAmount()
        updateUI()
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

class HealthTipsAdapter(private val tips: List<String>) : 
    RecyclerView.Adapter<HealthTipsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = tips[position]
    }

    override fun getItemCount() = tips.size
} 