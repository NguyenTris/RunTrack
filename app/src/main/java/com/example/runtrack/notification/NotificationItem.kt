package com.example.runtrack.notification

enum class NotificationCategory {
    ALL,
    FRIENDS,
    SYSTEM
}

enum class NotificationType {
    HEADER,
    FRIEND_REQUEST,
    FRIEND_ACTIVITY,
    FRIEND_PROCESSED,
    SYSTEM_SECURITY,
    SYSTEM_DEVICE,
    SYSTEM_POLICY
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val category: NotificationCategory,
    val title: String,
    val subtitle: String = "",
    val timeAgo: String = "",
    val badgeCount: String? = null,
    val avatarResId: Int? = null,
    val primaryActionText: String? = null,
    val secondaryActionText: String? = null,
    var isRead: Boolean = false,
    var isHandled: Boolean = false
)
