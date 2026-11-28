package com.example.runtrack.network.model.user

data class WeeklyProgressDto(
    val monday: Boolean = false,
    val tuesday: Boolean = false,
    val wednesday: Boolean = false,
    val thursday: Boolean = false,
    val friday: Boolean = false,
    val saturday: Boolean = false,
    val sunday: Boolean = false
)

data class UserResponse(
    val id: Int = 0,
    val email: String = "",
    val full_name: String? = null,
    val avatar_url: String? = null
)

data class UserProfileDto(
    val id: Int? = null,
    val email: String? = null,
    val full_name: String = "Runner",
    val avatar_url: String? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val weekly_goal_km: Float = 40.0f,
    val last_workout_time: String? = null,
    val recovery_completed: Boolean = false,
    val recovery_hours_remaining: Double = 0.0,
    val recovery_percentage: Int = 92,
    val recovery_status_color: String = "green",
    val recovery_status_label: String = "SẴN SÀNG",
    val recovery_display_text: String = "Tối ưu (24h)",
    val weekly_progress: WeeklyProgressDto? = null,
    val progress_bar_percentage: Int = 0
)

typealias UserProfileResponse = UserProfileDto

data class UserProfileUpdate(
    val weekly_goal_km: Double? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val full_name: String? = null
)
