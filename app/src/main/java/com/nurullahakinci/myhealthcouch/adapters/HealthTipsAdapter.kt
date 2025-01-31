package com.nurullahakinci.myhealthcouch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nurullahakinci.myhealthcouch.R
import com.nurullahakinci.myhealthcouch.models.HealthTip

class HealthTipsAdapter(private val tips: List<HealthTip>) : 
    RecyclerView.Adapter<HealthTipsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.tipTitle)
        val descriptionText: TextView = view.findViewById(R.id.tipDescription)
        val iconImage: ImageView = view.findViewById(R.id.tipIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_tip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tip = tips[position]
        holder.titleText.text = tip.title
        holder.descriptionText.text = tip.description
        holder.iconImage.setImageResource(tip.iconRes)
    }

    override fun getItemCount() = tips.size
} 