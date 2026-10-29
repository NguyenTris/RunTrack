package com.example.runtrack.club

data class ClubEvent(
    val id: String,
    val title: String,
    val time: String,
    val location: String,
    val registeredCount: String,
    var isJoined: Boolean = false
)
