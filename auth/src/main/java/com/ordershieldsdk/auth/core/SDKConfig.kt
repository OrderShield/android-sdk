package com.ordershieldsdk.auth.core

/**
 * Configuration class for OrderShield SDK
 */
data class SDKConfig(
    val apiKey: String,
    val timeoutSeconds: Long = 30L,
    val enableLogging: Boolean = false
) {
    init {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
    }
    
    companion object {
        // Base URL is fixed in auth module
        // TODO: Replace with your actual base URL
        const val BASE_URL = "https://your-api-base-url.com/api/"
    }
}
