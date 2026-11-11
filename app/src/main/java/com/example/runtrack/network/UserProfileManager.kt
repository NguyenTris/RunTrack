package com.example.runtrack.network

import android.content.Context
import android.util.Log
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
 * Quản lý tính toán phục hồi (24 giờ = 100%) và đồng bộ với Server Backend RunTrack
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
            // 1. Thử lấy từ Backend RunTrack (Azure VPS)
            val profileUrl = ApiConfig.getUserProfileUrl()
            try {
                val responseStr = httpGet(profileUrl, timeoutMs = 3000)
                if (responseStr != null) {
                    val json = JSONObject(responseStr)
                    val profile = RecoveryProfile(
                        fullName = json.optString("full_name", "Hùng Runner"),
                        recoveryPercentage = json.optInt("recovery_percentage", 92),
                        recoveryColor = json.optString("recovery_status_color", "green"),
                        recoveryLabel = json.optString("recovery_status_label", "SẴN SÀNG"),
                        displayText = json.optString("recovery_display_text", "Tối ưu (24h)")
                    )
                    Log.d(TAG, "Lấy profile từ Backend thành công: $profile")
                    onSuccess(profile)
                    return@Thread
                }
            } catch (e: Exception) {
                Log.w(TAG, "Không thể kết nối Backend Profile (${e.message}), chuyển sang tính toán cục bộ.")
            }

            // 2. Tính toán cục bộ dựa trên dữ liệu buổi chạy trước
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val nowMillis = System.currentTimeMillis()
            // Mặc định: lần chạy cuối cách đây 22.1 tiếng (tương ứng 92% readiness như trên thiết kế mẫu)
            val defaultLastWorkout = nowMillis - (22.1 * 3600 * 1000).toLong()
            val lastWorkoutTime = prefs.getLong(KEY_LAST_WORKOUT_TIME, defaultLastWorkout)

            val hoursPassed = (nowMillis - lastWorkoutTime).toDouble() / (3600 * 1000)
            val recoveryPercentage = Math.min(100, Math.round((hoursPassed / 24.0) * 100).toInt())
            val remainingHours = Math.max(0.0, 24.0 - hoursPassed)

            val color: String
            val label: String
            val displayText: String

            when {
                recoveryPercentage > 85 -> {
                    color = "green"
                    label = "SẴN SÀNG"
                    displayText = "Tối ưu (24h)"
                }
                recoveryPercentage > 60 -> {
                    color = "yellow"
                    label = "TRUNG BÌNH"
                    displayText = "Còn %.1fh (%d%%)".format(remainingHours, recoveryPercentage)
                }
                else -> {
                    color = "red"
                    label = "CẦN NGHỈ"
                    displayText = "Còn %.1fh (%d%%)".format(remainingHours, recoveryPercentage)
                }
            }

            val localProfile = RecoveryProfile(
                fullName = "Hùng Runner",
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

    private fun httpGet(urlString: String, timeoutMs: Int): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.setRequestProperty("Accept", "application/json")

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

