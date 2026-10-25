package com.example.runtrack.chat

data class ChatItem(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isGroup: Boolean = false,
    val avatarResId: Int? = null,
    val category: ChatFilterType = ChatFilterType.FRIENDS
)
