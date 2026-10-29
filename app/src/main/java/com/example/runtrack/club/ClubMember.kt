package com.example.runtrack.club

data class ClubMember(
    val id: String,
    val name: String,
    val role: String,
    val kmWeekly: String,
    val avatarResId: Int? = null
)
