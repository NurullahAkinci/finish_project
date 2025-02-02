package com.nurullahakinci.myhealthcouch.notifications

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime

class NotificationManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addNotification(notification: NotificationItem) {
        val notifications = getNotifications().toMutableList()
        notifications.add(0, notification) // En yeni bildirimi başa ekle
        saveNotifications(notifications)
    }

    fun getNotifications(): List<NotificationItem> {
        val json = sharedPreferences.getString("notifications", "[]")
        val type = object : TypeToken<List<NotificationItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun markAsRead(id: String) {
        val notifications = getNotifications().toMutableList()
        val index = notifications.indexOfFirst { it.id == id }
        if (index != -1) {
            notifications[index] = notifications[index].copy(isRead = true)
            saveNotifications(notifications)
        }
    }

    fun markAllAsRead() {
        val notifications = getNotifications().map { it.copy(isRead = true) }
        saveNotifications(notifications)
    }

    private fun saveNotifications(notifications: List<NotificationItem>) {
        val json = gson.toJson(notifications)
        sharedPreferences.edit().putString("notifications", json).apply()
    }

    fun getUnreadCount(): Int {
        return getNotifications().count { !it.isRead }
    }

    // Örnek bildirimler
    fun addSampleNotifications() {
        val sampleNotifications = listOf(
            NotificationItem(
                "Your daily step goal achieved!",
                "Congratulations! You've reached your 10,000 steps goal today.",
                LocalDateTime.now().minusHours(1)
            ),
            NotificationItem(
                "Time for breathing exercise",
                "Take a moment to relax with a quick breathing session.",
                LocalDateTime.now().minusHours(3)
            ),
            NotificationItem(
                "Heart rate update",
                "Your resting heart rate has improved over the last week.",
                LocalDateTime.now().minusHours(5)
            ),
            NotificationItem(
                "Hydration reminder",
                "Don't forget to drink water! You're 500ml behind your daily goal.",
                LocalDateTime.now().minusHours(7)
            ),
            NotificationItem(
                "Weekly health report",
                "Your weekly health summary is ready to view.",
                LocalDateTime.now().minusDays(1)
            ),
            NotificationItem(
                "New achievement unlocked",
                "You've maintained a consistent exercise routine for 7 days!",
                LocalDateTime.now().minusDays(2)
            ),
            NotificationItem(
                "Sleep schedule reminder",
                "Time to prepare for bed to maintain your sleep schedule.",
                LocalDateTime.now().minusDays(2)
            ),
            NotificationItem(
                "Meditation milestone",
                "You've completed 10 breathing sessions this month!",
                LocalDateTime.now().minusDays(3)
            ),
            NotificationItem(
                "Health tip of the day",
                "Regular stretching can improve your posture and reduce stress.",
                LocalDateTime.now().minusDays(4)
            ),
            NotificationItem(
                "Activity suggestion",
                "Perfect weather for a walk! Why not take a 15-minute break?",
                LocalDateTime.now().minusDays(5)
            )
        )
        
        saveNotifications(sampleNotifications)
    }
} 