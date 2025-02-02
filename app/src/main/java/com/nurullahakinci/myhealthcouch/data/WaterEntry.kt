package com.nurullahakinci.myhealthcouch.data

import java.time.LocalDateTime

data class WaterEntry(
    val timestamp: LocalDateTime,
    val amount: Int // ml cinsinden
) 