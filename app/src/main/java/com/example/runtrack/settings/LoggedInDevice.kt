package com.example.runtrack.settings

enum class DeviceType {
    MOBILE,
    DESKTOP,
    TABLET,
    WATCH
}

data class LoggedInDevice(
    val id: String,
    val name: String,
    val deviceType: DeviceType,
    val location: String,
    val clientApp: String,
    val ipAddress: String,
    val lastActive: String,
    val isCurrent: Boolean = false
)
