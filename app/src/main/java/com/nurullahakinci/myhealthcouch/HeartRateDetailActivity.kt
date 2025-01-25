package com.nurullahakinci.myhealthcouch

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class HeartRateDetailActivity : AppCompatActivity() {
    private lateinit var lineChart: LineChart
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate_detail)
        
        setupToolbar()
        setupChart()
        setupAddButton()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupChart() {
        lineChart = findViewById(R.id.heartRateChart)
        // Chart'ı özelleştir
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            
            // X ekseni ayarları
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }
            
            // Y ekseni ayarları
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 40f
                axisMaximum = 120f
            }
            axisRight.isEnabled = false
            
            // Örnek veri ekle
            updateChartData()
        }
    }
    
    private fun updateChartData() {
        // Örnek veri
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 70f))
        entries.add(Entry(1f, 75f))
        entries.add(Entry(2f, 72f))
        // ... daha fazla veri ekle
        
        val dataSet = LineDataSet(entries, "Heart Rate").apply {
            color = ContextCompat.getColor(this@HeartRateDetailActivity, R.color.primary)
            setCircleColor(ContextCompat.getColor(this@HeartRateDetailActivity, R.color.primary))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        
        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }
    
    private fun setupAddButton() {
        findViewById<MaterialCardView>(R.id.addMeasurementCard).setOnClickListener {
            showAddDataDialog()
        }
    }
    
    private fun showAddDataDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(R.layout.dialog_add_heart_rate)
            .create()
        
        dialog.show()
        
        dialog.findViewById<MaterialButton>(R.id.addButton)?.setOnClickListener {
            val input = dialog.findViewById<TextInputEditText>(R.id.heartRateInput)?.text.toString()
            if (input.isNotEmpty()) {
                // Veriyi kaydet ve grafiği güncelle
                updateChartData()
                dialog.dismiss()
            }
        }
        
        dialog.findViewById<MaterialButton>(R.id.cancelButton)?.setOnClickListener {
            dialog.dismiss()
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