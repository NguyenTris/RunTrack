package com.example.runtrack.club

data class ClubInviteFriend(
    val id: String,
    val name: String,
    val location: String,
    var isInvited: Boolean = false
)
