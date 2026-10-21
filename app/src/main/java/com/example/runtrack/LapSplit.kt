package com.example.runtrack

data class LapSplit(
    val kmText: String,
    val paceText: String,
    val hrText: String,
    val elevationText: String,
    val isHighlighted: Boolean = false
)
