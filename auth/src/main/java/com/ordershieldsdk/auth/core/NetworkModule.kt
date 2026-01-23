package com.ordershieldsdk.auth.core

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Network module for handling Retrofit setup and API service creation
 */
internal class NetworkModule private constructor() {
    
    private var config: SDKConfig? = null

    companion object {
        private const val LOG_TAG = "OrderShieldSDK"
        
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
        // Reset retrofit and client instances to use new config
        _retrofit = null
        _client = null
    }

    /**
     * Authentication interceptor to add API key to all requests
     */
    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
            
            config?.apiKey?.let { apiKey ->
                requestBuilder.addHeader("X-API-KEY", apiKey)
            }
            
            chain.proceed(requestBuilder.build())
        }
    }

    /**
     * Custom logging interceptor for detailed API logging
     */
    private fun createLoggingInterceptor(): Interceptor {
        return Interceptor { chain ->
            if (config?.enableLogging != true) {
                return@Interceptor chain.proceed(chain.request())
            }

            val request = chain.request()
            val requestBody = request.body
            val hasRequestBody = requestBody != null

            // Log Request URL and Method
            Log.d(LOG_TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(LOG_TAG, "→ ${request.method} ${request.url}")
            
            // Log Request Headers (mask sensitive headers)
            request.headers.forEach { header ->
                val headerValue = if (header.first.equals("X-API-KEY", ignoreCase = true)) {
                    "***" // Mask API key
                } else {
                    header.second
                }
                Log.d(LOG_TAG, "→ Header: ${header.first}: $headerValue")
            }

            // Log Request Body/Parameters
            if (hasRequestBody) {
                if (requestBody is RequestBody) {
                    val buffer = Buffer()
                    try {
                        requestBody.writeTo(buffer)
                        val requestBodyString = buffer.readUtf8()
                        Log.d(LOG_TAG, "→ Request Body: $requestBodyString")
                    } catch (e: IOException) {
                        Log.d(LOG_TAG, "→ Request Body: [Unable to read request body]")
                    }
                }
            } else {
                // For GET requests, log query parameters
                val queryString = request.url.query
                if (!queryString.isNullOrEmpty()) {
                    Log.d(LOG_TAG, "→ Query Parameters: $queryString")
                } else {
                    Log.d(LOG_TAG, "→ Request Body: [No request body]")
                }
            }

            val startTime = System.currentTimeMillis()
            val response: Response
            try {
                response = chain.proceed(request)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "→ Request failed: ${e.message}")
                throw e
            }

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            // Log Response
            val responseBody = response.body
            val responseBodyString = responseBody?.string()
            
            Log.d(LOG_TAG, "← Response Code: ${response.code} ${response.message} (${duration}ms)")
            Log.d(LOG_TAG, "← Response URL: ${response.request.url}")
            
            // Log Response Headers
            response.headers.forEach { header ->
                Log.d(LOG_TAG, "← Header: ${header.first}: ${header.second}")
            }
            
            // Log Response Body
            if (responseBodyString != null) {
                Log.d(LOG_TAG, "← Response Body: $responseBodyString")
            } else {
                Log.d(LOG_TAG, "← Response Body: [No response body]")
            }
            
            Log.d(LOG_TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Recreate response body since it was consumed
            val newResponseBody = responseBodyString?.let {
                val contentType = responseBody?.contentType()
                it.toResponseBody(contentType)
            } ?: responseBody

            response.newBuilder()
                .body(newResponseBody)
                .build()
        }
    }

    /**
     * OkHttpClient with interceptors and timeout configuration
     */
    @Volatile
    private var _client: OkHttpClient? = null
    
    private val client: OkHttpClient
        get() {
            return _client ?: synchronized(this) {
                _client ?: run {
                    val builder = OkHttpClient.Builder()
                        .addInterceptor(createAuthInterceptor())
                        .connectTimeout(config?.timeoutSeconds ?: 30L, TimeUnit.SECONDS)
                        .readTimeout(config?.timeoutSeconds ?: 30L, TimeUnit.SECONDS)
                        .writeTimeout(config?.timeoutSeconds ?: 30L, TimeUnit.SECONDS)
                    
                    if (config?.enableLogging == true) {
                        builder.addInterceptor(createLoggingInterceptor())
                    }
                    
                    builder.build().also { _client = it }
                }
            }
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
