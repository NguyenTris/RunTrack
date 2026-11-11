package com.example.runtrack

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

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

data class PasswordChange(
    val old_password: String,
    val new_password: String
)

data class WeeklyProgress(
    val monday: Boolean,
    val tuesday: Boolean,
    val wednesday: Boolean,
    val thursday: Boolean,
    val friday: Boolean,
    val saturday: Boolean,
    val sunday: Boolean
)

data class UserProfileResponse(
    val full_name: String,
    val last_workout_time: String?,
    val recovery_completed: Boolean,
    val recovery_hours_remaining: Double,
    val weekly_progress: WeeklyProgress,
    val progress_bar_percentage: Int
)

data class UserProfileUpdate(
    val full_name: String?,
    val height: Double?,
    val weight: Double?,
    val weekly_goal_km: Double?
)

data class HomeStatsResponse(
    val total_distance_km: Double,
    val weekly_goal_km: Double,
    val completion_percentage: Int
)

data class ActivityItem(
    val id: Int,
    val distance_km: Double,
    val duration_minutes: Int,
    val run_date: String
)

data class WeatherResponse(
    val temperature: Double,
    val condition: String,
    val icon: String,
    val city: String
)

data class FriendResponse(
    val user_id: Int,
    val full_name: String,
    val avatar_url: String,
    val mutual_friends: Int,
    val friend_status: String
)

data class ClubResponse(
    val club_id: Int,
    val name: String,
    val description: String,
    val member_count: Int,
    val is_joined: Boolean
)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Any>

    @PUT("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: PasswordChange
    ): Response<Any>

    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>

    @PUT("user/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body request: UserProfileUpdate
    ): Response<UserProfileResponse>

    @GET("dashboard/home-stats")
    suspend fun getHomeStats(
        @Header("Authorization") token: String
    ): Response<HomeStatsResponse>

    @GET("dashboard/recent-activities")
    suspend fun getRecentActivities(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 5
    ): Response<List<ActivityItem>>

    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<WeatherResponse>

    @GET("search/friends")
    suspend fun searchFriends(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<List<FriendResponse>>

    @GET("search/clubs")
    suspend fun searchClubs(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<List<ClubResponse>>
}
