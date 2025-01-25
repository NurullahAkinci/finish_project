package com.nurullahakinci.myhealthcouch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.nurullahakinci.myhealthcouch.R

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupBottomNavigation()
        setupCards()
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
            // Nefes egzersizi detay sayfasına yönlendirme
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
            // Su tüketimi detay sayfasına yönlendirme
        }
    }
}