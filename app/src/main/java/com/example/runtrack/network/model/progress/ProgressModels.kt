package com.example.runtrack.network.model.progress

import com.google.gson.annotations.SerializedName

data class ProgressDayDto(
    val day: Int = 0,
    val distance_km: Double = 0.0,
    val heat_level: Int = 0,
    val activities_count: Int = 0
)

data class ProgressRecordDto(
    val id: String = "",
    @SerializedName(value = "title", alternate = ["label"])
    val title: String = "",
    @SerializedName(value = "subtitle", alternate = ["date"])
    val subtitle: String = "",
    @SerializedName(value = "value", alternate = ["record_value"])
    val value: String = ""
)

data class MonthProgressResponse(
    val year: Int = 2026,
    val month: Int = 1,
    val first_weekday: Int = 0,
    val days_in_month: Int = 30,
    val total_distance_km: Double = 0.0,
    val total_activities: Int = 0,
    val total_duration_minutes: Int = 0,
    val days: List<ProgressDayDto> = emptyList(),
    val records: List<ProgressRecordDto> = emptyList()
)

data class QuarterStatDto(
    @SerializedName(value = "quarter", alternate = ["quarter_name"])
    val quarter: String = "",
    val distance_km: Double = 0.0,
    @SerializedName(value = "target_km", alternate = ["max_km"])
    val target_km: Double = 400.0,
    @SerializedName(value = "is_current", alternate = ["is_current_quarter"])
    val is_current: Boolean = false
)

data class YearAchievementDto(
    val id: String = "",
    val title: String = "",
    val icon_key: String = "trophy",
    @SerializedName(value = "is_unlocked", alternate = ["unlocked"])
    val is_unlocked: Boolean = false
)

data class YearProgressResponse(
    val year: Int = 2026,
    val total_distance_km: Double = 0.0,
    val total_activities: Int = 0,
    val quarters: List<QuarterStatDto> = emptyList(),
    val records: List<ProgressRecordDto> = emptyList(),
    val achievements: List<YearAchievementDto> = emptyList()
)

data class LeaderboardEntryDto(
    val rank: Int = 0,
    val user_id: Int = 0,
    val full_name: String = "",
    val total_distance_km: Double = 0.0,
    @SerializedName(value = "avg_pace_display", alternate = ["pace_display"])
    val avg_pace_display: String = "",
    @SerializedName(value = "activities_count", alternate = ["total_activities"])
    val activities_count: Int = 0,
    val is_current_user: Boolean = false
)

data class LeaderboardResponse(
    val scope: String = "global",
    val total_runners: Int = 0,
    val current_user_rank: Int? = null,
    val entries: List<LeaderboardEntryDto> = emptyList()
)
