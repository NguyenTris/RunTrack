package com.example.runtrack

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Thay đổi BASE_URL tùy theo phương thức bạn đang test:
    // 1. Dùng IP Public của VPS: "http://20.6.128.94:8000/"
    // 2. Dùng máy ảo Emulator: "http://10.0.2.2:8000/"
    private const val BASE_URL = "http://20.6.128.94:8000/"

    // Tăng thời gian chờ (timeout) lên 30 giây để tránh lỗi ngắt kết nối đột ngột
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}