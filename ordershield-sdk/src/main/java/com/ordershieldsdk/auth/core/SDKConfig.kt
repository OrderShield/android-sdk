package com.ordershieldsdk.auth.core

import android.content.Context

/**
 * Configuration class for OrderShield SDK
 */
data class SDKConfig(
    val apiKey: String,
    val timeoutSeconds: Long = 30L,
    val enableLogging: Boolean = false,
    /** Optional context; required for session persistence and register-device/session during init */
    val context: Context? = null
) {
    init {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
    }
    
    companion object {
        // Base URL for OrderShield API
        const val BASE_URL = "https://api.ordershield.ai/"
    }
}
