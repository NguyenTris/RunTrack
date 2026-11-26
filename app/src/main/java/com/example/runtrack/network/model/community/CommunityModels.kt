package com.example.runtrack.network.model.community

import com.example.runtrack.network.model.dashboard.RoutePointDto
import com.google.gson.annotations.SerializedName

data class ClubMemberDto(
    val user_id: Int = 0,
    val full_name: String = "",
    val role: String = "Thành viên",
    @SerializedName(value = "weekly_distance_km", alternate = ["weekly_km"])
    val weekly_distance_km: Double = 0.0
)

data class ClubEventItemDto(
    @SerializedName(value = "event_id", alternate = ["id"])
    val event_id: Int = 0,
    val title: String = "",
    @SerializedName(value = "event_time", alternate = ["schedule_text", "time", "scheduled_at"])
    val event_time: String = "",
    val location: String = "",
    val registered_count: Int = 0,
    @SerializedName(value = "is_registered", alternate = ["is_joined"])
    val is_registered: Boolean = false
)

data class ClubDetailResponse(
    val club_id: Int = 0,
    val name: String = "",
    val description: String = "",
    val member_count: Int = 0,
    val is_joined: Boolean = false,
    val members: List<ClubMemberDto> = emptyList(),
    val events: List<ClubEventItemDto> = emptyList()
)

data class GpsArtRouteDto(
    val id: String = "",
    val name: String = "",
    val distance_km: Double = 0.0,
    val city: String = "",
    val icon_key: String = "target",
    val points: List<RoutePointDto> = emptyList()
)
