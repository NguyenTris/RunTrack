package com.example.runtrack.settings

enum class FriendRequestScope {
    EVERYONE,
    FRIENDS_OF_FRIENDS,
    NO_ONE
}

data class FriendRequestConfig(
    var scope: FriendRequestScope = FriendRequestScope.EVERYONE,
    var allowSearchByContact: Boolean = true,
    var suggestToClubMembers: Boolean = true,
    var autoAcceptClubMembers: Boolean = false
)
