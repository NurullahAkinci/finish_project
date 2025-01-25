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
                    // Handle home navigation
                    true
                }
                R.id.navigation_analytics -> {
                    // Handle analytics navigation
                    true
                }
                R.id.navigation_settings -> {
                    // Handle settings navigation
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
            // Handle breathing exercise card click
        }
        
        findViewById<MaterialCardView>(R.id.stepCounterCard).setOnClickListener {
            // Handle step counter card click
        }
        
        findViewById<MaterialCardView>(R.id.waterCard).setOnClickListener {
            // Handle water consumption card click
        }
    }
}