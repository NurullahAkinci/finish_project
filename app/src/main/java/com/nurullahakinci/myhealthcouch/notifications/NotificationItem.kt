package com.nurullahakinci.myhealthcouch.notifications

import java.time.LocalDateTime
import java.util.UUID

data class NotificationItem(
    val title: String,
    val message: String,
    val timestamp: LocalDateTime,
    val id: String = UUID.randomUUID().toString(),
    val isRead: Boolean = false
) 