package com.example.runtrack.network.model.search

data class FriendResponse(
    val user_id: Int,
    val full_name: String,
    val avatar_url: String = "",
    val mutual_friends: Int = 0,
    val friend_status: String = "none"
)

data class ClubResponse(
    val club_id: Int,
    val name: String,
    val description: String = "",
    val member_count: Int = 0,
    val is_joined: Boolean = false
)
