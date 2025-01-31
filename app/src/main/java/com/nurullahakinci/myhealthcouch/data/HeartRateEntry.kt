package com.nurullahakinci.myhealthcouch.data

import java.time.LocalDateTime

data class HeartRateEntry(
    val timestamp: LocalDateTime,
    val value: Float
) 