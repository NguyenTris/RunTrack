package com.example.runtrack.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val access_token: String,
    val token_type: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val full_name: String
)

data class RegisterResponse(
    val message: String,
    val user: String
)

data class WeeklyProgressDto(
    val monday: Boolean = false,
    val tuesday: Boolean = false,
    val wednesday: Boolean = false,
    val thursday: Boolean = false,
    val friday: Boolean = false,
    val saturday: Boolean = false,
    val sunday: Boolean = false
)

data class UserProfileDto(
    val full_name: String = "Runner",
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

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<JsonObject>

    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") authorization: String? = null
    ): Response<UserProfileDto>
}
