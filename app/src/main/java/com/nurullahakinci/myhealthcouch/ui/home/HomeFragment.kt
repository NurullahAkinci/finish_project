package com.nurullahakinci.myhealthcouch.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.nurullahakinci.myhealthcouch.R
import com.nurullahakinci.myhealthcouch.HeartRateDetailActivity
import com.nurullahakinci.myhealthcouch.BreathingExerciseActivity
import com.nurullahakinci.myhealthcouch.StepCounterDetailActivity
import com.nurullahakinci.myhealthcouch.WaterConsumptionDetailActivity

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCards()
    }

    private fun setupCards() {
        view?.apply {
            // Heart Rate Card
            findViewById<MaterialCardView>(R.id.heartRateCard)?.setOnClickListener {
                startActivity(Intent(requireContext(), HeartRateDetailActivity::class.java))
            }
            
            // Breathing Exercise Card
            findViewById<MaterialCardView>(R.id.breathingCard)?.setOnClickListener {
                startActivity(Intent(requireContext(), BreathingExerciseActivity::class.java))
            }
            
            // Step Counter Card
            findViewById<MaterialCardView>(R.id.stepsCard)?.setOnClickListener {
                startActivity(Intent(requireContext(), StepCounterDetailActivity::class.java))
            }
            
            // Water Consumption Card
            findViewById<MaterialCardView>(R.id.waterCard)?.setOnClickListener {
                startActivity(Intent(requireContext(), WaterConsumptionDetailActivity::class.java))
            }
        }
    }
} 