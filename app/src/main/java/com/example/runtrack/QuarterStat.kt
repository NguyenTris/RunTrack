package com.example.runtrack

data class QuarterStat(
    val quarterName: String,
    val distanceKm: Float,
    val maxKm: Float = 400f,
    val isCurrentQuarter: Boolean = false
)
