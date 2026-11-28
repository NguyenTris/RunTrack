package com.example.runtrack.network.model.friend

import com.google.gson.annotations.SerializedName

data class FriendUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class FriendsOverviewResponse(
    @SerializedName("friends") val friends: List<FriendUserDto> = emptyList(),
    @SerializedName("pending_received") val pendingReceived: List<FriendUserDto> = emptyList(),
    @SerializedName("pending_sent") val pendingSent: List<FriendUserDto> = emptyList()
)

data class FriendSearchUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("friend_status") val friendStatus: String = "none"
)

data class FriendActionResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: String? = null
)
