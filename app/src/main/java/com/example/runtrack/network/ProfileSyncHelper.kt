package com.example.runtrack.network

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.runtrack.network.model.user.UserProfileDto
import com.example.runtrack.network.model.user.UserProfileUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ProfileSyncHelper {

    interface ProfileCallback {
        fun onSuccess(profile: UserProfileDto)
        fun onError(message: String)
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    @JvmStatic
    fun fetchProfile(context: Context, callback: ProfileCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getAuthenticatedService(context).getUserProfile()
                mainHandler.post {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        callback.onSuccess(body)
                    } else {
                        callback.onError("Không thể tải hồ sơ (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    callback.onError("Lỗi kết nối máy chủ: ${e.localizedMessage ?: "offline"}")
                }
            }
        }
    }

    @JvmStatic
    fun updateProfile(
        context: Context,
        height: Double?,
        weight: Double?,
        weeklyGoalKm: Double?,
        callback: ProfileCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UserProfileUpdate(
                    weekly_goal_km = weeklyGoalKm,
                    height = height,
                    weight = weight
                )
                val response = ApiClient.getAuthenticatedService(context).updateUserProfile(request)
                mainHandler.post {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        callback.onSuccess(body)
                    } else {
                        callback.onError("Cập nhật hồ sơ thất bại (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    callback.onError("Lỗi kết nối máy chủ: ${e.localizedMessage ?: "offline"}")
                }
            }
        }
    }
}
