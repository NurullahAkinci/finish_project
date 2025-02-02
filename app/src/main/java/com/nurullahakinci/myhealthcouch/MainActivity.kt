package com.nurullahakinci.myhealthcouch

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nurullahakinci.myhealthcouch.adapters.NotificationAdapter
import com.nurullahakinci.myhealthcouch.notifications.NotificationManager
import de.hdodenhof.circleimageview.CircleImageView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var profileImage: CircleImageView
    private lateinit var userName: TextView
    private lateinit var welcomeText: TextView
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationDot: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        notificationManager = NotificationManager(this)
        setupNotifications()
        setupPreferences()
        setupViews()
        setupClickListeners()
        setupBottomNavigation()
    }

    private fun setupPreferences() {
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    private fun setupViews() {
        profileImage = findViewById(R.id.profileImage)
        userName = findViewById(R.id.userName)
        welcomeText = findViewById(R.id.welcomeText)
        
        // Kullanıcı adını ayarla
        userName.text = sharedPreferences.getString("user_name", "Nurullah Akıncı")
        

        val greeting = when {
            android.text.format.DateFormat.format("HH", System.currentTimeMillis()).toString().toInt() < 12 -> "Good Morning!"
            android.text.format.DateFormat.format("HH", System.currentTimeMillis()).toString().toInt() < 18 -> "Good Day!"
            else -> "Good Night!"
        }
        welcomeText.text = greeting
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.heartRateCard).setOnClickListener {
            startActivity(Intent(this, HeartRateDetailActivity::class.java))
        }

        findViewById<View>(R.id.breathingCard).setOnClickListener {
            startActivity(Intent(this, BreathingExerciseActivity::class.java))
        }

        findViewById<View>(R.id.stepsCard).setOnClickListener {
            startActivity(Intent(this, StepCounterDetailActivity::class.java))
        }

        findViewById<View>(R.id.waterCard).setOnClickListener {
            startActivity(Intent(this, WaterConsumptionDetailActivity::class.java))
        }

        profileImage.setOnClickListener {
            startActivity(Intent(this, ProfileSettingsActivity::class.java))
        }
    }
    
    private fun setupNotifications() {
        notificationDot = findViewById(R.id.notificationDot)
        
        findViewById<View>(R.id.notificationIcon).setOnClickListener {
            showNotificationsDialog()
        }
        
        updateNotificationDot()
        
        // İlk kez açılıyorsa örnek bildirimleri ekle
        if (getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("first_run", true)) {
            notificationManager.addSampleNotifications()
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("first_run", false)
                .apply()
        }
    }
    
    private fun showNotificationsDialog() {
        try {
            val dialog = MaterialAlertDialogBuilder(this, R.style.NotificationDialog)
                .setView(R.layout.dialog_notifications)
                .show()
            
            val recyclerView = dialog.findViewById<RecyclerView>(R.id.notificationsRecyclerView)
            

            lateinit var notificationAdapter: NotificationAdapter
            
            notificationAdapter = NotificationAdapter(
                notifications = notificationManager.getNotifications()
            ) { notification ->
                notificationManager.markAsRead(notification.id)
                updateNotificationDot()
                notificationAdapter.updateNotifications(notificationManager.getNotifications())
            }
            
            recyclerView?.let { rv ->
                rv.layoutManager = LinearLayoutManager(this)
                rv.adapter = notificationAdapter
                
                dialog.findViewById<View>(R.id.markAllRead)?.setOnClickListener {
                    notificationManager.markAllAsRead()
                    updateNotificationDot()
                    notificationAdapter.updateNotifications(notificationManager.getNotifications())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

            MaterialAlertDialogBuilder(this)
                .setTitle("Hata")
                .setMessage("Bildirimler yüklenirken bir hata oluştu: ${e.message}")
                .setPositiveButton("Tamam", null)
                .show()
        }
    }
    
    private fun updateNotificationDot() {
        val unreadCount = notificationManager.getUnreadCount()
        notificationDot.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE
    }

    private fun setupBottomNavigation() {
        val bottomNav: BottomNavigationView = findViewById(R.id.nav_view)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> true
            }
        }
    }
}