package com.nurullahakinci.myhealthcouch

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.ImageButton
import android.content.SharedPreferences
import com.google.android.material.switchmaterial.SwitchMaterial
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import java.util.*
import android.widget.RadioGroup

class BreathingExerciseActivity : AppCompatActivity() {
    private lateinit var breathingCircle: ImageView
    private lateinit var instructionText: TextView
    private lateinit var timerText: TextView
    private lateinit var startButton: Button
    private lateinit var exerciseCards: List<CardView>
    private lateinit var sharedPreferences: SharedPreferences
    private var currentExercise: BreathingExercise? = null
    private var currentAnimator: ValueAnimator? = null
    private var isExerciseRunning = false
    private lateinit var exerciseInfoText: TextView
    private var breathCount = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breathing_exercise)
        
        sharedPreferences = getSharedPreferences("breathing_preferences", MODE_PRIVATE)
        
        setupToolbar()
        initializeViews()
        setupExerciseCards()
        setupCustomizationOptions()
        updateStats() // İlk açılışta istatistikleri göster
        
        // Kayıtlı rengi uygula
        val savedColor = sharedPreferences.getInt("circle_color", ContextCompat.getColor(this, R.color.purple_500))
        breathingCircle.setColorFilter(savedColor)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Nefes Egzersizleri"
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initializeViews() {
        breathingCircle = findViewById(R.id.breathingCircle)
        instructionText = findViewById(R.id.instructionText)
        timerText = findViewById(R.id.timerText)
        startButton = findViewById(R.id.startButton)
        exerciseInfoText = findViewById(R.id.exerciseInfoText)
        
        startButton.setOnClickListener {
            if (isExerciseRunning) {
                stopExercise()
            } else {
                startExercise()
            }
        }
        
        findViewById<Button>(R.id.resetStatsButton).setOnClickListener {
            showResetConfirmationDialog()
        }
    }
    
    private fun setupExerciseCards() {
        exerciseCards = listOf(
            findViewById(R.id.relaxingBreathCard),
            findViewById(R.id.stressReliefCard),
            findViewById(R.id.sleepBreathCard),
            findViewById(R.id.focusBreathCard)
        )
        
        exerciseCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                selectExercise(index)
            }
        }
    }
    
    private fun selectExercise(index: Int) {
        currentExercise = when(index) {
            0 -> BreathingExercise(
                4, 7, 8,
                "4-7-8 Relaxing Breath",
                "This technique helps reduce stress and anxiety.\n" +
                "Inhale for 4 seconds\nHold for 7 seconds\nExhale for 8 seconds"
            )
            1 -> BreathingExercise(
                4, 4, 4,
                "Stress Relief Breath",
                "Also known as box breathing technique.\n" +
                "Inhale for 4 seconds\nHold for 4 seconds\nExhale for 4 seconds"
            )
            2 -> BreathingExercise(
                4, 7, 8,
                "Sleep Breath",
                "Helps you fall asleep easier.\n" +
                "Inhale for 4 seconds\nHold for 7 seconds\nExhale for 8 seconds"
            )
            3 -> BreathingExercise(
                4, 0, 4,
                "Focus Breath",
                "Helps improve concentration.\n" +
                "Inhale for 4 seconds\nExhale for 4 seconds"
            )
            else -> null
        }
        
        exerciseCards.forEach { it.alpha = 0.5f }
        exerciseCards[index].alpha = 1.0f
        startButton.isEnabled = true
        
        // Egzersiz bilgilerini göster
        currentExercise?.let {
            exerciseInfoText.text = it.description
            exerciseInfoText.visibility = View.VISIBLE
        }
    }
    
    private fun startExercise() {
        isExerciseRunning = true
        startButton.text = "Durdur"
        breathCount = 0
        currentExercise?.let { exercise ->
            startBreathingAnimation(exercise)
        }
        updateStatistics()
        updateStats() // İstatistikleri güncelle
    }
    
    private fun stopExercise() {
        isExerciseRunning = false
        startButton.text = "Başla"
        currentAnimator?.cancel()
        currentAnimator = null
        instructionText.text = "Hazır"
        breathCount = 0
        updateBreathCount()
        breathingCircle.scaleX = 1f
        breathingCircle.scaleY = 1f
        updateStats() // İstatistikleri güncelle
    }
    
    private fun updateBreathCount() {
        timerText.text = if (breathCount > 0) {
            "Tamamlanan nefes döngüsü: $breathCount"
        } else {
            ""
        }
    }
    
    private fun startBreathingAnimation(exercise: BreathingExercise) {
        currentAnimator?.cancel()
        
        currentAnimator = ValueAnimator.ofFloat(1f, 2f, 1f).apply {
            duration = ((exercise.inhaleTime + exercise.holdTime + exercise.exhaleTime) * 1000).toLong()
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            
            addUpdateListener { animation ->
                if (isExerciseRunning) {
                    breathingCircle.scaleX = animation.animatedValue as Float
                    breathingCircle.scaleY = animation.animatedValue as Float
                    updateInstructions(animation.animatedFraction, exercise)
                }
            }
            
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    if (isExerciseRunning) {
                        breathCount++
                        updateBreathCount()
                    }
                }
            })
        }
        
        currentAnimator?.start()
    }
    
    private fun updateInstructions(fraction: Float, exercise: BreathingExercise) {
        val totalTime = exercise.inhaleTime + exercise.holdTime + exercise.exhaleTime
        val currentTime = fraction * totalTime
        
        instructionText.text = when {
            currentTime < exercise.inhaleTime -> "Nefes Al"
            currentTime < exercise.inhaleTime + exercise.holdTime -> "Tut"
            else -> "Nefes Ver"
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        currentAnimator?.cancel()
    }
    
    private fun setupCustomizationOptions() {
        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            showSettingsDialog()
        }
    }
    
    private fun showSettingsDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_breathing_settings, null)
        
        // Renk seçimi
        val colorGroup = view.findViewById<RadioGroup>(R.id.colorGroup)
        val savedColor = sharedPreferences.getInt("circle_color", ContextCompat.getColor(this, R.color.purple_500))
        
        when (savedColor) {
            ContextCompat.getColor(this, R.color.purple_500) -> colorGroup.check(R.id.colorPurple)
            ContextCompat.getColor(this, R.color.blue_500) -> colorGroup.check(R.id.colorBlue)
            ContextCompat.getColor(this, R.color.green_500) -> colorGroup.check(R.id.colorGreen)
        }
        
        colorGroup.setOnCheckedChangeListener { _, checkedId ->
            val color = when (checkedId) {
                R.id.colorPurple -> ContextCompat.getColor(this, R.color.purple_500)
                R.id.colorBlue -> ContextCompat.getColor(this, R.color.blue_500)
                R.id.colorGreen -> ContextCompat.getColor(this, R.color.green_500)
                else -> ContextCompat.getColor(this, R.color.purple_500)
            }
            breathingCircle.setColorFilter(color)
            sharedPreferences.edit().putInt("circle_color", color).apply()
        }
        
        // Reminder settings
        val reminderSwitch = view.findViewById<SwitchMaterial>(R.id.reminderSwitch)
        val reminderTime = view.findViewById<TextView>(R.id.reminderTime)
        
        reminderSwitch.isChecked = sharedPreferences.getBoolean("reminder_enabled", false)
        reminderTime.text = sharedPreferences.getString("reminder_time", "10:00")
        
        reminderTime.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                reminderTime.text = time
                if (reminderSwitch.isChecked) {
                    scheduleReminder(hour, minute)
                }
            }
        }
        
        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val time = reminderTime.text.toString().split(":")
                scheduleReminder(time[0].toInt(), time[1].toInt())
            } else {
                cancelReminder()
            }
            sharedPreferences.edit().putBoolean("reminder_enabled", isChecked).apply()
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Settings")
            .setView(view)
            .setPositiveButton("Okay", null)
            .show()
    }

    private fun showTimePickerDialog(onTimeSelected: (Int, Int) -> Unit) {
        val currentTime = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                onTimeSelected(hour, minute)
                sharedPreferences.edit().putString("reminder_time", String.format("%02d:%02d", hour, minute)).apply()
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun scheduleReminder(hour: Int, minute: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BreathingReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BreathingReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    private fun updateStatistics() {
        with(sharedPreferences.edit()) {
            // Daily Statistic
            val today = Calendar.getInstance().timeInMillis
            val exerciseStats = getExerciseStatsForDay(today)
            putInt("daily_breaths_${today}", exerciseStats.first + breathCount)
            putLong("daily_duration_${today}", exerciseStats.second + 
                (currentExercise?.let { it.inhaleTime + it.holdTime + it.exhaleTime } ?: 0))

            // The most using Exercise
            currentExercise?.let { exercise ->
                val exerciseCount = sharedPreferences.getInt("exercise_count_${exercise.name}", 0)
                putInt("exercise_count_${exercise.name}", exerciseCount + 1)
            }

            // The long time exer.
            val currentDuration = breathCount * (currentExercise?.let { it.inhaleTime + it.holdTime + it.exhaleTime } ?: 0)
            val longestSession = sharedPreferences.getLong("longest_session", 0)
            if (currentDuration > longestSession) {
                putLong("longest_session", currentDuration.toLong())
            }

            apply()
        }
    }

    private fun getExerciseStatsForDay(timestamp: Long): Pair<Int, Long> {
        val breaths = sharedPreferences.getInt("daily_breaths_$timestamp", 0)
        val duration = sharedPreferences.getLong("daily_duration_$timestamp", 0)
        return Pair(breaths, duration)
    }

    private fun getWeeklyStats(): Triple<Int, Long, Float> {
        var totalBreaths = 0
        var totalDuration = 0L
        var daysWithExercise = 0

        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        
        // collect statistics of the last 7 days
        for (i in 0..6) {
            calendar.timeInMillis = today - (i * 24 * 60 * 60 * 1000)
            val stats = getExerciseStatsForDay(calendar.timeInMillis)
            if (stats.first > 0) {
                totalBreaths += stats.first
                totalDuration += stats.second
                daysWithExercise++
            }
        }

        val averageDailyDuration = if (daysWithExercise > 0) 
            totalDuration.toFloat() / daysWithExercise else 0f

        return Triple(totalBreaths, totalDuration, averageDailyDuration)
    }

    private fun getMonthlyStats(): Triple<Int, Long, Int> {
        var totalBreaths = 0
        var totalDuration = 0L
        var daysWithExercise = 0

        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        
        // Collect statistics of the last 30 days
        for (i in 0..29) {
            calendar.timeInMillis = today - (i * 24 * 60 * 60 * 1000)
            val stats = getExerciseStatsForDay(calendar.timeInMillis)
            if (stats.first > 0) {
                totalBreaths += stats.first
                totalDuration += stats.second
                daysWithExercise++
            }
        }

        return Triple(totalBreaths, totalDuration, daysWithExercise)
    }

    private fun getExerciseTypeStats(): Map<String, Int> {
        val exerciseTypes = listOf(
            "4-7-8 Relaxing Breath",
            "Stress Relief Breath",
            "Sleep Breath",
            "Focus Breath"
        )
        
        return exerciseTypes.associateWith { type ->
            sharedPreferences.getInt("exercise_count_$type", 0)
        }
    }

    private fun calculateGoalProgress(): Float {
        val targetDailyMinutes = 10 // Günlük hedef dakika
        val monthlyStats = getMonthlyStats()
        val averageMinutesPerDay = monthlyStats.second / (1000 * 60) / 30f
        return (averageMinutesPerDay / targetDailyMinutes) * 100
    }

    private fun getMostUsedExercise(): String {
        var maxCount = 0
        var mostUsedExercise = "Henüz veri yok"

        listOf(
            "4-7-8 Relaxing Breath",
            "Stress Relief Breath",
            "Sleep Breath",
            "Focus Breath"
        ).forEach { exerciseName ->
            val count = sharedPreferences.getInt("exercise_count_$exerciseName", 0)
            if (count > maxCount) {
                maxCount = count
                mostUsedExercise = exerciseName
            }
        }

        return mostUsedExercise
    }
    
    data class BreathingExercise(
        val inhaleTime: Int,
        val holdTime: Int,
        val exhaleTime: Int,
        val name: String,
        val description: String
    )

    // İstatistikleri güncellemek için yeni bir fonksiyon
    private fun updateStats() {
        val weeklyStats = getWeeklyStats()
        val monthlyStats = getMonthlyStats()
        val mostUsedExercise = getMostUsedExercise()
        val longestSession = sharedPreferences.getLong("longest_session", 0) / 60
        val exerciseTypeStats = getExerciseTypeStats()
        val goalProgress = calculateGoalProgress()
        
        // Haftalık İstatistikler
        findViewById<TextView>(R.id.weeklyStatsText).text = """
            • Total: ${weeklyStats.first} cycles
            • Time: ${weeklyStats.second / 60} min
            • Avg.: ${String.format("%.1f", weeklyStats.third / 60)} min/day
        """.trimIndent()
        
        // Aylık İstatistikler
        findViewById<TextView>(R.id.monthlyStatsText).text = """
            • Total: ${monthlyStats.first} cycles
            • Time: ${monthlyStats.second / 60} min
            • Active: ${monthlyStats.third} days
            • Goal: ${String.format("%.1f", goalProgress)}%
        """.trimIndent()
        
        // Genel İstatistikler
        findViewById<TextView>(R.id.generalStatsText).text = """
            • Favorite: $mostUsedExercise
            • Longest: $longestSession min
            • Total: ${sharedPreferences.getInt("total_breaths", 0)} cycles
        """.trimIndent()
        
        // Egzersiz Türü Dağılımı
        findViewById<TextView>(R.id.exerciseTypeStatsText).text = """
            • 4-7-8: ${exerciseTypeStats["4-7-8 Relaxing Breath"]} times
            • Stress: ${exerciseTypeStats["Stress Relief Breath"]} times
            • Sleep: ${exerciseTypeStats["Sleep Breath"]} times
            • Focus: ${exerciseTypeStats["Focus Breath"]} times
        """.trimIndent()
    }

    private fun showResetConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Statistics")
            .setMessage("Are you sure you want to reset all breathing exercise statistics? This action cannot be undone.")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Reset") { _, _ ->
                resetAllStatistics()
            }
            .show()
    }

    private fun resetAllStatistics() {
        with(sharedPreferences.edit()) {
            // Reset daily stats
            val calendar = Calendar.getInstance()
            val today = calendar.timeInMillis
            for (i in 0..30) {
                calendar.timeInMillis = today - (i * 24 * 60 * 60 * 1000)
                remove("daily_breaths_${calendar.timeInMillis}")
                remove("daily_duration_${calendar.timeInMillis}")
            }

            // Reset exercise type counts
            listOf(
                "4-7-8 Relaxing Breath",
                "Stress Relief Breath",
                "Sleep Breath",
                "Focus Breath"
            ).forEach { type ->
                remove("exercise_count_$type")
            }

            // Reset general stats
            remove("longest_session")
            remove("total_breaths")

            apply()
        }
        
        // Update UI
        updateStats()
    }
} 