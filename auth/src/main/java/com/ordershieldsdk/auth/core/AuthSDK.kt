package com.ordershieldsdk.auth.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.ordershieldsdk.auth.data.repository.AuthRepository
import com.ordershieldsdk.auth.ui.VerificationActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AuthSDK {
    private var isInitialized = false
    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Initialize SDK with configuration
     * @param context Application context
     * @param config SDK configuration containing API key, base URL, etc.
     */
    fun init(context: Context, config: SDKConfig) {
        try {
            // Initialize network module with configuration
            NetworkModule.getInstance().initialize(config)
            isInitialized = true
            
            // Call verification settings API after initialization
            fetchVerificationSettings()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize SDK: ${e.message}", e)
        }
    }
    
    /**
     * Fetch verification settings from API
     * This is called automatically after SDK initialization
     */
    private fun fetchVerificationSettings() {
        sdkScope.launch {
            try {
                val repository = AuthRepository()
                val result = repository.getVerificationSettings()
                result.onSuccess { settings ->
                    // Store settings for use in verification flow
                    VerificationSettingsManager.setSettings(settings)
                }.onFailure { exception ->
                    // Handle error if needed
                    // For now, just log or ignore as per requirement
                }
            } catch (e: Exception) {
                // Handle exception if needed
            }
        }
    }

    /**
     * Initialize SDK with API key (convenience method)
     * @param context Application context
     * @param apiKey API key for authentication
     * @param enableLogging Enable HTTP logging (default: false)
     */
    fun init(
        context: Context,
        apiKey: String,
        enableLogging: Boolean = false
    ) {
        val config = SDKConfig(
            apiKey = apiKey,
            enableLogging = enableLogging
        )
        init(context, config)
    }

    /**
     * Start the verification flow
     * @param activity Current activity
     * @param onResult Callback with verification result
     */
    fun startVerification(activity: Activity, onResult: (Boolean) -> Unit = {}) {
        if (!isInitialized) {
            throw IllegalStateException("SDK not initialized. Call AuthSDK.init() first.")
        }

        val intent = Intent(activity, VerificationActivity::class.java)
        activity.startActivity(intent)
    }
}

