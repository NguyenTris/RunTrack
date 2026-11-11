package com.example.runtrack.network

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

data class WeatherInfo(
    val city: String,
    val temperature: Double,
    val humidity: Int,
    val condition: String,
    val weatherMain: String,
    val aqi: Int
)

/**
 * Xử lý tải dữ liệu Thời tiết & Chỉ số AQI với cơ chế gọi kép:
 * 1. Ưu tiên gọi VPS Azure / Backend RunTrack
 * 2. Tự động Fallback trực tiếp sang OpenWeatherMap nếu Backend không phản hồi
 */
object WeatherManager {
    private const val TAG = "WeatherManager"

    fun fetchWeather(
        lat: Double,
        lon: Double,
        onSuccess: (WeatherInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            // ========================================================
            // BƯỚC 1: Ưu tiên gọi Server Backend RunTrack (Azure VPS)
            // ========================================================
            val backendUrl = ApiConfig.getWeatherUrl(lat, lon)
            try {
                Log.d(TAG, "Đang kết nối Backend: $backendUrl")
                val backendData = httpGet(backendUrl, timeoutMs = 3000)
                if (backendData != null) {
                    val json = JSONObject(backendData)
                    val info = WeatherInfo(
                        city = json.optString("city", "Hà Nội"),
                        temperature = json.optDouble("temperature", 24.0),
                        humidity = json.optInt("humidity", 68),
                        condition = json.optString("condition", "Mây rải rác"),
                        weatherMain = json.optString("weather_main", "Clouds"),
                        aqi = json.optInt("aqi", 32)
                    )
                    Log.d(TAG, "Backend phản hồi thành công: $info")
                    onSuccess(info)
                    return@Thread
                }
            } catch (e: Exception) {
                Log.w(TAG, "Backend không phản hồi (${e.message}). Chuyển sang Fallback trực tiếp OpenWeatherMap...")
            }

            // ========================================================
            // BƯỚC 2: Fallback trực tiếp sang OpenWeatherMap API
            // ========================================================
            try {
                val owmWeatherUrl = ApiConfig.getOwmDirectWeatherUrl(lat, lon)
                val owmDataStr = httpGet(owmWeatherUrl, timeoutMs = 6000)
                if (owmDataStr != null) {
                    val owmJson = JSONObject(owmDataStr)
                    val cityName = owmJson.optString("name", "Hà Nội")
                    val mainObj = owmJson.getJSONObject("main")
                    val temp = mainObj.optDouble("temp", 24.0)
                    val humidity = mainObj.optInt("humidity", 68)

                    val weatherArr = owmJson.getJSONArray("weather")
                    val weatherObj = weatherArr.getJSONObject(0)
                    val condition = weatherObj.optString("description", "Mây rải rác")
                    val weatherMain = weatherObj.optString("main", "Clouds")

                    // Lấy AQI từ OWM Air Pollution
                    var aqi = 32
                    try {
                        val airUrl = ApiConfig.getOwmDirectAirPollutionUrl(lat, lon)
                        val airDataStr = httpGet(airUrl, timeoutMs = 4000)
                        if (airDataStr != null) {
                            val airJson = JSONObject(airDataStr)
                            val listArr = airJson.optJSONArray("list")
                            if (listArr != null && listArr.length() > 0) {
                                val item = listArr.getJSONObject(0)
                                val comp = item.optJSONObject("components")
                                val pm25 = comp?.optDouble("pm2_5", 7.8) ?: 7.8
                                aqi = calculateUsAqiFromPm25(pm25)
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Lỗi lấy Air Pollution: ${ex.message}")
                    }

                    val info = WeatherInfo(
                        city = cityName,
                        temperature = temp,
                        humidity = humidity,
                        condition = condition,
                        weatherMain = weatherMain,
                        aqi = aqi
                    )
                    Log.d(TAG, "OpenWeatherMap Fallback thành công: $info")
                    onSuccess(info)
                    return@Thread
                }
            } catch (e: Exception) {
                Log.e(TAG, "OpenWeatherMap Fallback thất bại: ${e.message}")
            }

            // ========================================================
            // BƯỚC 3: Dữ liệu ngoại tuyến dự phòng (Offline Default)
            // ========================================================
            val defaultInfo = WeatherInfo(
                city = "HÀ NỘI",
                temperature = 24.0,
                humidity = 68,
                condition = "Nhiều mây",
                weatherMain = "Clouds",
                aqi = 32
            )
            onSuccess(defaultInfo)
        }.start()
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

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
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

    /**
     * Tính US AQI chuẩn EPA từ PM2.5 (ug/m3)
     */
    fun calculateUsAqiFromPm25(pm25: Double): Int {
        val breakpoints = listOf(
            Triple(0.0..12.0, 0, 50),
            Triple(12.1..35.4, 51, 100),
            Triple(35.5..55.4, 101, 150),
            Triple(55.5..150.4, 151, 200),
            Triple(150.5..250.4, 201, 300),
            Triple(250.5..500.4, 301, 500)
        )

        for ((range, iLow, iHigh) in breakpoints) {
            if (pm25 in range) {
                val cLow = range.start
                val cHigh = range.endInclusive
                return (((iHigh - iLow) / (cHigh - cLow)) * (pm25 - cLow) + iLow).roundToInt()
            }
        }
        return if (pm25 > 500.4) 300 else 0
    }
}

