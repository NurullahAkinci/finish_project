package com.nurullahakinci.myhealthcouch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import de.hdodenhof.circleimageview.CircleImageView
import com.nurullahakinci.myhealthcouch.R
import com.nurullahakinci.myhealthcouch.BreathingExerciseActivity

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var profileImage: CircleImageView
    private lateinit var userName: TextView
    private lateinit var welcomeText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupPreferences()
        setupViews()
        setupBottomNavigation()
        setupCards()
    }

    private fun setupPreferences() {
        prefs = getSharedPreferences("MyHealthCouch", MODE_PRIVATE)
    }
    
    private fun setupViews() {
        profileImage = findViewById(R.id.profileImage)
        userName = findViewById(R.id.userName)
        welcomeText = findViewById(R.id.welcomeText)
        updateProfileInfo()
    }
    
    private fun updateProfileInfo() {
        // Kullanıcı adını güncelle
        val name = prefs.getString("user_name", "Kullanıcı")
        userName.text = name ?: "User"
        welcomeText.text = "Welcome Back!"
        
        // Profil resmini güncelle
        val savedImageUri = prefs.getString("profile_image", null)
        if (savedImageUri != null) {
            try {
                profileImage.setImageURI(Uri.parse(savedImageUri))
            } catch (e: Exception) {
                profileImage.setImageResource(R.drawable.default_profile)
            }
        } else {
            profileImage.setImageResource(R.drawable.default_profile)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Aktivite tekrar görünür olduğunda profil bilgilerini güncelle
        setupPreferences() // SharedPreferences'ı yeniden başlat
        updateProfileInfo() // Profil bilgilerini güncelle
    }
    
    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottomNav).setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_home -> {
                    // Ana sayfa zaten açık
                    true
                }
                R.id.navigation_analytics -> {
                    // Analytics sayfasına yönlendirme
                    true
                }
                R.id.navigation_settings -> {
                    // Ayarlar sayfasına yönlendirme
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupCards() {
        findViewById<MaterialCardView>(R.id.heartRateCard).setOnClickListener {
            startActivity(Intent(this, HeartRateDetailActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.breathingCard).setOnClickListener {
            startActivity(Intent(this, BreathingExerciseActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.stepsCard).setOnClickListener {
            try {
                val intent = Intent(this, StepCounterDetailActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        findViewById<MaterialCardView>(R.id.waterCard).setOnClickListener {
            startActivity(Intent(this, WaterConsumptionDetailActivity::class.java))
        }
    }
}