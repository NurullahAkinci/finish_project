package com.nurullahakinci.myhealthcouch.models

import androidx.annotation.DrawableRes

data class HealthTip(
    val title: String,
    val description: String,
    @DrawableRes val iconRes: Int
) 