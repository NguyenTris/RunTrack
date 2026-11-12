package com.example.runtrack.network

import android.content.Context
import android.util.Log
import com.example.runtrack.SessionManager
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class RecoveryProfile(
    val fullName: String,
    val recoveryPercentage: Int,
    val recoveryColor: String, // "green", "yellow", "red"
    val recoveryLabel: String, // "SẴN SÀNG", "TRUNG BÌNH", "CẦN NGHỈ"
    val displayText: String    // "Tối ưu (24h)" hoặc "Còn Xh nữa"
)

/**
 * Quản lý tính toán phục hồi (24 giờ = 100%) và đồng bộ với Server Backend RunTrack trên Azure VPS
 */
object UserProfileManager {
    private const val TAG = "UserProfileManager"
    private const val PREFS_NAME = "runtrack_user_recovery"
    private const val KEY_LAST_WORKOUT_TIME = "last_workout_time_millis"

    fun loadProfileAndRecovery(
        context: Context,
        onSuccess: (RecoveryProfile) -> Unit
    ) {
        Thread {
            // 1. Lấy token từ SessionManager để gọi API xác thực trên VPS
            val token = SessionManager.getAuthToken(context)
            val authHeader = if (!token.isNullOrEmpty()) "Bearer $token" else null
            val profileUrl = ApiConfig.getUserProfileUrl()

            try {
                Log.d(TAG, "Đang kết nối API hồ sơ thực tế VPS: $profileUrl")
                val responseStr = httpGet(profileUrl, timeoutMs = 4000, authHeader = authHeader)
                if (responseStr != null) {
                    val json = JSONObject(responseStr)
                    val rawName = json.optString("full_name", "")
                    val fullName = if (rawName.isNotBlank()) rawName else SessionManager.getUsername(context)

                    // Tính toán phục hồi từ backend (hoặc tự suy diễn nếu VPS chưa có trường mở rộng)
                    val recoveryHoursRemaining = json.optDouble("recovery_hours_remaining", 1.9)
                    val hoursPassed = Math.max(0.0, 24.0 - recoveryHoursRemaining)
                    val calculatedPct = Math.min(100, Math.round((hoursPassed / 24.0) * 100).toInt())

                    val recoveryPercentage = if (json.has("recovery_percentage")) {
                        json.optInt("recovery_percentage", calculatedPct)
                    } else {
                        calculatedPct
                    }

                    val calculatedColor = when {
                        recoveryPercentage > 85 -> "green"
                        recoveryPercentage > 60 -> "yellow"
                        else -> "red"
                    }
                    val calculatedLabel = when {
                        recoveryPercentage > 85 -> "SẴN SÀNG"
                        recoveryPercentage > 60 -> "TRUNG BÌNH"
                        else -> "CẦN NGHỈ"
                    }
                    val calculatedDisplay = when {
                        recoveryPercentage > 85 -> "Tối ưu (24h)"
                        else -> "Còn %.1fh (%d%%)".format(recoveryHoursRemaining, recoveryPercentage)
                    }

                    val profile = RecoveryProfile(
                        fullName = fullName,
                        recoveryPercentage = recoveryPercentage,
                        recoveryColor = if (json.has("recovery_status_color")) json.optString("recovery_status_color", calculatedColor) else calculatedColor,
                        recoveryLabel = if (json.has("recovery_status_label")) json.optString("recovery_status_label", calculatedLabel) else calculatedLabel,
                        displayText = if (json.has("recovery_display_text")) json.optString("recovery_display_text", calculatedDisplay) else calculatedDisplay
                    )
                    Log.d(TAG, "Lấy profile từ Backend VPS thành công: $profile")
                    onSuccess(profile)
                    return@Thread
                }
            } catch (e: Exception) {
                Log.w(TAG, "Không thể kết nối Backend VPS Profile (${e.message}), chuyển sang tính toán cục bộ.")
            }

            // 2. Tính toán cục bộ dựa trên dữ liệu buổi chạy trước nếu VPS chưa phản hồi
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val nowMillis = System.currentTimeMillis()
            val lastWorkoutTime = prefs.getLong(KEY_LAST_WORKOUT_TIME, 0L)

            val hoursPassed = if (lastWorkoutTime > 0L) {
                (nowMillis - lastWorkoutTime).toDouble() / (3600 * 1000)
            } else {
                25.0 // Chưa từng chạy buổi nào -> lớn hơn 24h -> 100%
            }

            val recoveryPercentage = Math.min(100, Math.round((hoursPassed / 24.0) * 100).toInt())
            val remainingHours = Math.max(0.0, 24.0 - hoursPassed)

            val color = when {
                recoveryPercentage > 85 -> "green"
                recoveryPercentage > 60 -> "yellow"
                else -> "red"
            }
            val label = when {
                recoveryPercentage > 85 -> "SẴN SÀNG"
                recoveryPercentage > 60 -> "TRUNG BÌNH"
                else -> "CẦN NGHỈ"
            }
            val displayText = when {
                recoveryPercentage >= 100 -> "Tối ưu (24h)"
                else -> "Còn %.1fh (%d%%)".format(remainingHours, recoveryPercentage)
            }

            val localProfile = RecoveryProfile(
                fullName = SessionManager.getUsername(context),
                recoveryPercentage = recoveryPercentage,
                recoveryColor = color,
                recoveryLabel = label,
                displayText = displayText
            )
            onSuccess(localProfile)
        }.start()
    }

    fun saveLastWorkoutTime(context: Context, timestampMillis: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_WORKOUT_TIME, timestampMillis).apply()
    }

    private fun httpGet(urlString: String, timeoutMs: Int, authHeader: String? = null): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.setRequestProperty("Accept", "application/json")
            if (!authHeader.isNullOrEmpty()) {
                conn.setRequestProperty("Authorization", authHeader)
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                sb.toString()
            } else {
                null
            }
        } finally {
            conn?.disconnect()
        }
    }
}

