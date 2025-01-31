package com.nurullahakinci.myhealthcouch

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nurullahakinci.myhealthcouch.adapters.WaterTipsAdapter
import com.nurullahakinci.myhealthcouch.models.WaterTip

class WaterConsumptionDetailActivity : AppCompatActivity() {
    private lateinit var tipsRecyclerView: RecyclerView
    private lateinit var dailyGoalText: TextView
    private lateinit var currentConsumptionText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_consumption_detail)

        setupToolbar()
        initializeViews()
        setupTipsRecyclerView()
        setupAddButton()
        updateStats()
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
        tipsRecyclerView = findViewById(R.id.tipsRecyclerView)
        dailyGoalText = findViewById(R.id.dailyGoalText)
        currentConsumptionText = findViewById(R.id.currentConsumptionText)
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

    private fun setupAddButton() {
        findViewById<MaterialButton>(R.id.addWaterButton).setOnClickListener {
            showAddWaterDialog()
        }
    }

    private fun showAddWaterDialog() {
        val options = arrayOf("100ml", "200ml", "300ml", "500ml")
        MaterialAlertDialogBuilder(this)
            .setTitle("Add Water")
            .setItems(options) { dialog, which ->
                val amount = options[which].replace("ml", "").toInt()
                addWaterConsumption(amount)
                dialog.dismiss()
            }
            .show()
    }

    private fun addWaterConsumption(amount: Int) {
        // TODO: Implement water consumption tracking
        updateStats()
    }

    private fun updateStats() {
        // TODO: Implement statistics update
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 