package com.nurullahakinci.myhealthcouch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nurullahakinci.myhealthcouch.R
import com.nurullahakinci.myhealthcouch.notifications.NotificationItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NotificationAdapter(
    private var notifications: List<NotificationItem>,
    private val onNotificationClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.notificationTitle)
        val message: TextView = view.findViewById(R.id.notificationMessage)
        val time: TextView = view.findViewById(R.id.notificationTime)
        val unreadIndicator: View = view.findViewById(R.id.unreadIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        
        holder.title.text = notification.title
        holder.message.text = notification.message
        holder.time.text = getFormattedTime(notification.timestamp)
        holder.unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE
        
        holder.itemView.setOnClickListener {
            onNotificationClick(notification)
        }
    }

    private fun getFormattedTime(timestamp: LocalDateTime?): String {
        try {
            if (timestamp == null) return ""
            
            val now = LocalDateTime.now()
            
            // Negatif değerleri önlemek için kontrol ekleyelim
            if (timestamp.isAfter(now)) return "Şimdi"
            
            return try {
                val minutes = ChronoUnit.MINUTES.between(timestamp, now)
                val hours = ChronoUnit.HOURS.between(timestamp, now)
                val days = ChronoUnit.DAYS.between(timestamp, now)

                when {
                    minutes < 1 -> "Şimdi"
                    minutes < 60 -> "$minutes dakika önce"
                    hours < 24 -> "$hours saat önce"
                    days < 7 -> "$days gün önce"
                    else -> timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                }
            } catch (e: Exception) {
                "Geçersiz tarih"
            }
        } catch (e: Exception) {
            return "Geçersiz tarih"
        }
    }

    override fun getItemCount() = notifications.size

    fun updateNotifications(newNotifications: List<NotificationItem>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
} 