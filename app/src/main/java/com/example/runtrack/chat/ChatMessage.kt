package com.example.runtrack.chat

data class ChatMessage(
    val id: String,
    val text: String,
    val time: String,
    val isFromMe: Boolean
)
