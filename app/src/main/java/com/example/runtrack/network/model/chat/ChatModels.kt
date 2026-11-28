package com.example.runtrack.network.model.chat

import com.google.gson.annotations.SerializedName

data class ConversationDto(
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("name") val name: String,
    @SerializedName("last_message") val lastMessage: String,
    @SerializedName("time") val time: String,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("unread_count") val unreadCount: Int = 0,
    @SerializedName("is_online") val isOnline: Boolean = false,
    @SerializedName("is_group") val isGroup: Boolean = false,
    @SerializedName("category") val category: String = "friends",
    @SerializedName("receiver_id") val receiverId: Int? = null,
    @SerializedName("club_id") val clubId: Int? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class ChatMessageDto(
    @SerializedName("id") val id: String,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("receiver_id") val receiverId: Int? = null,
    @SerializedName("club_id") val clubId: Int? = null,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_from_me") val isFromMe: Boolean,
    @SerializedName("time") val time: String,
    @SerializedName("sender_avatar_url") val senderAvatarUrl: String? = null
)

data class SendMessageRequest(
    @SerializedName("conversation_id") val conversationId: String? = null,
    @SerializedName("receiver_id") val receiverId: Int? = null,
    @SerializedName("club_id") val clubId: Int? = null,
    @SerializedName("content") val content: String
)
