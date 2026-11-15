package com.example.runtrack.network

import com.example.runtrack.network.model.auth.LoginRequest
import com.example.runtrack.network.model.auth.LoginResponse
import com.example.runtrack.network.model.auth.RegisterRequest
import com.example.runtrack.network.model.dashboard.ActivityItem
import com.example.runtrack.network.model.dashboard.HomeStatsResponse
import com.example.runtrack.network.model.dashboard.WeeklyActivitiesResponse
import com.example.runtrack.network.model.user.UserProfileDto
import com.example.runtrack.network.model.user.UserProfileResponse
import com.example.runtrack.network.model.user.UserProfileUpdate
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<JsonObject>

    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") authorization: String? = null
    ): Response<UserProfileDto>

    @PUT("user/profile")
    suspend fun updateUserProfile(
        @Body body: UserProfileUpdate
    ): Response<UserProfileResponse>

    @GET("dashboard/home-stats")
    suspend fun getHomeStats(): Response<HomeStatsResponse>

    @GET("dashboard/recent-activities")
    suspend fun getRecentActivities(
        @Query("limit") limit: Int = 5
    ): Response<List<ActivityItem>>

    @GET("activities/weekly")
    suspend fun getWeeklyActivities(): Response<WeeklyActivitiesResponse>
}
