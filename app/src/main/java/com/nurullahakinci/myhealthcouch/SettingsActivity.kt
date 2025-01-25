package com.nurullahakinci.myhealthcouch

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.card.MaterialCardView
import android.content.SharedPreferences
import android.content.Intent

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setupToolbar()
        setupPreferences()
        setupViews()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Ayarlar"
    }
    
    private fun setupPreferences() {
        prefs = getSharedPreferences("MyHealthCouch", MODE_PRIVATE)
    }
    
    private fun setupViews() {
        // Profile Settings
        findViewById<MaterialCardView>(R.id.profileCard).setOnClickListener {
            startActivity(Intent(this, ProfileSettingsActivity::class.java))
        }
        
        // Goals Settings
        setupGoalsSettings()
        
        // Notification Settings
        setupNotificationSettings()
        
        // App Settings
        setupAppSettings()
    }
    
    private fun setupGoalsSettings() {
        // Daily Step Goal
        val stepGoalInput = findViewById<TextInputEditText>(R.id.stepGoalInput)
        stepGoalInput.setText(prefs.getInt("step_goal", 10000).toString())
        stepGoalInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val newGoal = stepGoalInput.text.toString().toIntOrNull() ?: 10000
                prefs.edit().putInt("step_goal", newGoal).apply()
            }
        }
        
        // Water Goal
        val waterGoalInput = findViewById<TextInputEditText>(R.id.waterGoalInput)
        waterGoalInput.setText(prefs.getInt("water_goal", 2000).toString())
        waterGoalInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val newGoal = waterGoalInput.text.toString().toIntOrNull() ?: 2000
                prefs.edit().putInt("water_goal", newGoal).apply()
            }
        }
    }
    
    private fun setupNotificationSettings() {
        // Daily Reminders
        val dailyRemindersSwitch = findViewById<SwitchMaterial>(R.id.dailyRemindersSwitch)
        dailyRemindersSwitch.isChecked = prefs.getBoolean("daily_reminders", true)
        dailyRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("daily_reminders", isChecked).apply()
            updateNotificationSettings(isChecked)
        }
        
        // Goal Alerts
        val goalAlertsSwitch = findViewById<SwitchMaterial>(R.id.goalAlertsSwitch)
        goalAlertsSwitch.isChecked = prefs.getBoolean("goal_alerts", true)
        goalAlertsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("goal_alerts", isChecked).apply()
        }
    }
    
    private fun setupAppSettings() {
        // Dark Mode
        val darkModeSwitch = findViewById<SwitchMaterial>(R.id.darkModeSwitch)
        darkModeSwitch.isChecked = prefs.getBoolean("dark_mode", false)
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        
        // Unit Preference
        val metricSwitch = findViewById<SwitchMaterial>(R.id.metricSwitch)
        metricSwitch.isChecked = prefs.getBoolean("use_metric", true)
        metricSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("use_metric", isChecked).apply()
        }
    }
    
    private fun updateNotificationSettings(enabled: Boolean) {
        // Bildirim ayarlarını güncelle
        if (enabled) {
            // Bildirimleri aktifleştir
        } else {
            // Bildirimleri devre dışı bırak
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 