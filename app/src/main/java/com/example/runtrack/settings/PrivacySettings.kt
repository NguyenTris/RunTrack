package com.example.runtrack.settings

enum class ActivityPrivacyScope {
    PUBLIC,
    FRIENDS_ONLY,
    ONLY_ME
}

data class ActivityPrivacyConfig(
    var scope: ActivityPrivacyScope = ActivityPrivacyScope.FRIENDS_ONLY,
    var hideStartEndRadius: Boolean = true,
    var hideHeartRateData: Boolean = false
)
