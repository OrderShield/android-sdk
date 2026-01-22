package com.ordershieldsdk.auth.core

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network module for handling Retrofit setup and API service creation
 */
internal class NetworkModule private constructor() {
    
    private var config: SDKConfig? = null

    companion object {
        @Volatile
        private var INSTANCE: NetworkModule? = null

        fun getInstance(): NetworkModule {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkModule().also { INSTANCE = it }
            }
        }
    }

    /**
     * Initialize network module with SDK configuration
     */
    fun initialize(config: SDKConfig) {
        this.config = config
        // Reset retrofit instance to use new config
        _retrofit = null
    }

    /**
     * Authentication interceptor to add API key to all requests
     */
    private val authInterceptor: Interceptor by lazy {
        Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
            
            config?.apiKey?.let { apiKey ->
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }
            
            chain.proceed(requestBuilder.build())
        }
    }

    /**
     * HTTP logging interceptor for debugging
     */
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (config?.enableLogging == true) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * OkHttpClient with interceptors and timeout configuration
     */
    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(config?.timeoutSeconds ?: 30L, TimeUnit.SECONDS)
            .readTimeout(config?.timeoutSeconds ?: 30L, TimeUnit.SECONDS)
            .writeTimeout(config?.timeoutSeconds ?: 30L, TimeUnit.SECONDS)
        
        if (config?.enableLogging == true) {
            builder.addInterceptor(loggingInterceptor)
        }
        
        builder.build()
    }

    /**
     * Retrofit instance with base URL and converters
     */
    @Volatile
    private var _retrofit: Retrofit? = null
    
    val retrofit: Retrofit
        get() {
            return _retrofit ?: synchronized(this) {
                _retrofit ?: Retrofit.Builder()
                    .baseUrl(SDKConfig.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .also { _retrofit = it }
            }
        }

    /**
     * Create API service instance
     */
    inline fun <reified T> createService(): T = retrofit.create(T::class.java)
}
