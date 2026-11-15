package com.example.runtrack.network.model.dashboard

data class HomeStatsResponse(
    val total_distance_km: Float,
    val weekly_goal_km: Float,
    val completion_percentage: Int,
    val active_days: List<Int>? = null
)

data class ActivityItem(
    val id: String,
    val title: String = "Buổi chạy bộ",
    val distance_km: Float,
    val duration_minutes: Int,
    val run_date: String
)
