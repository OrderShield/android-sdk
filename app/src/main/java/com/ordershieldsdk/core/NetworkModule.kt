package com.ordershieldsdk.core

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class NetworkModule private constructor() {
    private var apiKey: String = ""

    companion object {
        @Volatile
        private var INSTANCE: NetworkModule? = null

        fun getInstance(): NetworkModule {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkModule().also { INSTANCE = it }
            }
        }
    }

    fun setApiKey(key: String) {
        this.apiKey = key
    }

    private val authInterceptor: Interceptor by lazy {
        Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://ordershield-api.projectbeta.biz/api/sdk") // Replace with your actual base URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    inline fun <reified T> createService(): T = retrofit.create(T::class.java)
}