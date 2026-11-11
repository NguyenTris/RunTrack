package com.example.runtrack.network

/**
 * Cấu hình tập trung địa chỉ Server Backend và External APIs cho RunTrack
 */
object ApiConfig {
    /**
     * ĐỊA CHỈ PRIMARY NIC PUBLIC IP CỦA AZURE VPS
     * - Khi test trên Azure VPS: "http://20.6.128.94:8000"
     * - Khi test máy ảo Emulator: "http://10.0.2.2:8000"
     */
    var BASE_URL: String = "http://20.6.128.94:8000"

    // Endpoints backend
    fun getWeatherUrl(lat: Double, lon: Double): String = "$BASE_URL/weather?lat=$lat&lon=$lon"
    fun getUserProfileUrl(): String = "$BASE_URL/user/profile"

    // OpenWeatherMap API Direct (Fallback khi server backend bảo trì hoặc chưa bật)
    const val OWM_API_KEY = "4a537541384c5250c53db97422d7a283"
    fun getOwmDirectWeatherUrl(lat: Double, lon: Double): String =
        "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$OWM_API_KEY&units=metric&lang=vi"
    fun getOwmDirectAirPollutionUrl(lat: Double, lon: Double): String =
        "https://api.openweathermap.org/data/2.5/air_pollution?lat=$lat&lon=$lon&appid=$OWM_API_KEY"
}

