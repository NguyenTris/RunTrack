package com.example.runtrack.network

import com.example.runtrack.network.model.auth.ChangePasswordRequest
import com.example.runtrack.network.model.auth.ForgotPasswordRequest
import com.example.runtrack.network.model.auth.ForgotPasswordResponse
import com.example.runtrack.network.model.auth.LoginRequest
import com.example.runtrack.network.model.auth.LoginResponse
import com.example.runtrack.network.model.auth.MessageResponse
import com.example.runtrack.network.model.auth.RegisterRequest
import com.example.runtrack.network.model.auth.ResetPasswordRequest
import com.example.runtrack.network.model.community.ClubDetailResponse
import com.example.runtrack.network.model.community.ClubEventItemDto
import com.example.runtrack.network.model.community.GpsArtRouteDto
import com.example.runtrack.network.model.dashboard.ActivityCreateRequest
import com.example.runtrack.network.model.dashboard.ActivityItem
import com.example.runtrack.network.model.dashboard.HomeStatsResponse
import com.example.runtrack.network.model.dashboard.WeeklyActivitiesResponse
import com.example.runtrack.network.model.progress.LeaderboardResponse
import com.example.runtrack.network.model.progress.MonthProgressResponse
import com.example.runtrack.network.model.progress.YearProgressResponse
import com.example.runtrack.network.model.search.ClubResponse
import com.example.runtrack.network.model.search.FriendResponse
import com.example.runtrack.network.model.user.UserProfileDto
import com.example.runtrack.network.model.user.UserProfileResponse
import com.example.runtrack.network.model.user.UserProfileUpdate
import com.example.runtrack.network.model.user.UserResponse
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<JsonObject>

    @POST("auth/logout")
    suspend fun logout(): Response<MessageResponse>

    @PUT("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>

    @GET("progress/month")
    suspend fun getProgressMonth(
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null
    ): Response<MonthProgressResponse>

    @GET("progress/year")
    suspend fun getProgressYear(
        @Query("year") year: Int? = null
    ): Response<YearProgressResponse>

    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("scope") scope: String = "global",
        @Query("limit") limit: Int = 20
    ): Response<LeaderboardResponse>

    @GET("clubs/{club_id}/detail")
    suspend fun getClubDetail(@Path("club_id") clubId: Int): Response<ClubDetailResponse>

    @POST("clubs/events/{event_id}/toggle-register")
    suspend fun toggleClubEventRegister(@Path("event_id") eventId: Int): Response<ClubEventItemDto>

    @GET("gps-art/routes")
    suspend fun getGpsArtRoutes(): Response<List<GpsArtRouteDto>>

    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") authorization: String? = null
    ): Response<UserProfileDto>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<UserResponse>

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

    @POST("activities")
    suspend fun createActivity(@Body request: ActivityCreateRequest): Response<ActivityItem>

    @GET("activities/weekly")
    suspend fun getWeeklyActivities(): Response<WeeklyActivitiesResponse>

    @GET("search/friends")
    suspend fun searchFriends(
        @Query("q") q: String = "",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<FriendResponse>>

    @GET("search/clubs")
    suspend fun searchClubs(
        @Query("q") q: String = "",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<ClubResponse>>

    @POST("clubs/{club_id}/toggle-join")
    suspend fun toggleJoinClub(@Path("club_id") clubId: Int): Response<ClubResponse>

    @GET("chat/conversations")
    suspend fun getConversations(): Response<List<com.example.runtrack.network.model.chat.ConversationDto>>

    @GET("chat/messages")
    suspend fun getChatMessages(
        @Query("conversation_id") conversationId: String
    ): Response<List<com.example.runtrack.network.model.chat.ChatMessageDto>>

    @POST("chat/messages")
    suspend fun sendMessage(
        @Body request: com.example.runtrack.network.model.chat.SendMessageRequest
    ): Response<com.example.runtrack.network.model.chat.ChatMessageDto>

    @GET("friends")
    suspend fun getFriendsOverview(): Response<com.example.runtrack.network.model.friend.FriendsOverviewResponse>

    @GET("friends/search")
    suspend fun searchFriendsUsers(
        @Query("q") q: String = ""
    ): Response<List<com.example.runtrack.network.model.friend.FriendSearchUserDto>>

    @POST("friends/request/{target_user_id}")
    suspend fun sendFriendRequest(
        @Path("target_user_id") targetUserId: Int
    ): Response<com.example.runtrack.network.model.friend.FriendActionResponse>

    @POST("friends/accept/{requester_id}")
    suspend fun acceptFriendRequest(
        @Path("requester_id") requesterId: Int
    ): Response<com.example.runtrack.network.model.friend.FriendActionResponse>

    @DELETE("friends/{target_user_id}")
    suspend fun removeFriendship(
        @Path("target_user_id") targetUserId: Int
    ): Response<com.example.runtrack.network.model.friend.FriendActionResponse>
}
