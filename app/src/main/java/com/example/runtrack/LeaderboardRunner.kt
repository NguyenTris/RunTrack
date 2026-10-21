package com.example.runtrack

data class LeaderboardRunner(
    val rank: String,
    val name: String,
    val location: String,
    val time: String,
    val avatarResId: Int,
    val isHighlighted: Boolean = false
)
