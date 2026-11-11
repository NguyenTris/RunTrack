package com.example.runtrack.network

import android.content.Context
import com.example.runtrack.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val baseUrl: String
        get() = ApiConfig.BASE_URL.trimEnd('/') + "/"

    val instance: ApiService
        get() = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

    fun getAuthenticatedService(context: Context): ApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val token = SessionManager.getAuthToken(context)
                val requestBuilder = original.newBuilder()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
