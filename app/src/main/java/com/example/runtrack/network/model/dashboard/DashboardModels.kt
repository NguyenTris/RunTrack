package com.example.runtrack.network.model.dashboard

data class HomeStatsResponse(
    val total_distance_km: Float,
    val weekly_goal_km: Float,
    val completion_percentage: Int,
    val active_days: List<Int>? = null
)

data class RoutePointDto(
    val lat: Double,
    val lng: Double
)

data class ActivityItem(
    val id: String,
    val title: String = "Buổi chạy bộ",
    val distance_km: Float,
    val duration_minutes: Int,
    val run_date: String,
    val route_points: List<RoutePointDto>? = null
)

data class ActivityCreateRequest(
    val title: String = "Buổi chạy RunTrack",
    val distance_km: Double,
    val duration_seconds: Int,
    val avg_pace_seconds: Int? = null,
    val calories_burned: Int? = null,
    val route_points: List<RoutePointDto>? = null
)

data class WeeklyActivityDay(
    val day: String,
    val date: String,
    val distance_km: Float,
    val duration_minutes: Int,
    val completed: Boolean
)

data class WeeklyActivitiesResponse(
    val week_start: String,
    val week_end: String,
    val total_distance_km: Float,
    val days: List<WeeklyActivityDay>
)
