package com.example.runtrack

data class Equipment(
    val id: String,
    val name: String,
    val usageText: String,
    val progressValue: Int,
    val maxProgress: Int,
    val isProgressVisible: Boolean,
    val subtext: String?,
    val isCheckVisible: Boolean,
    val iconResId: Int
)
