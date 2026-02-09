package com.ordershieldsdk.auth.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ordershieldsdk.auth.data.repository.AuthRepository
import com.ordershieldsdk.auth.ui.VerificationActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AuthSDK {
    @Volatile
    private var isInitialized = false
    
    @Volatile
    private var isSettingsLoaded = false
    
    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Initialize SDK with configuration (internal method)
     * @param config SDK configuration containing API key, base URL, etc.
     */
    private fun initInternal(config: SDKConfig) {
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
        isSettingsLoaded = false
        sdkScope.launch {
            try {
                val repository = AuthRepository()
                val result = repository.getVerificationSettings()
                result.onSuccess { settings ->
                    // Store settings for use in verification flow
                    VerificationSettingsManager.setSettings(settings)
                    isSettingsLoaded = true
                }.onFailure { exception ->
                    // Mark as loaded even on failure to allow verification to proceed
                    // Settings will fallback to session-based steps_remaining
                    isSettingsLoaded = true
                }
            } catch (e: Exception) {
                // Mark as loaded even on exception to allow verification to proceed
                isSettingsLoaded = true
            }
        }
    }

    /**
     * Initialize SDK with API key
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
        initInternal(config)
    }
    
    /**
     * Initialize SDK with configuration
     * @param config SDK configuration containing API key, base URL, etc.
     */
    fun init(config: SDKConfig) {
        initInternal(config)
    }

    /**
     * Start the verification flow
     * @param activity Current activity
     * @param onError Callback called when verification cannot start (e.g., SDK not initialized)
     * @param onStepCompleted Callback called when each verification step completes (e.g., "selfie", "email", "sms")
     * @param onVerificationCompleted Callback called when verification is completed and user exits (for navigation)
     */
    fun startVerification(
        activity: Activity,
        onError: (String) -> Unit,
        onStepCompleted: (String) -> Unit = {},
        onVerificationCompleted: () -> Unit = {}
    ) {
        Log.d("OrderShieldSDK", "startVerification called, isInitialized: $isInitialized")
        
        // Check SDK initialization
        if (!isInitialized) {
            Log.e("OrderShieldSDK", "SDK is not initialized")
            onError("SDK is not initialized")
            return
        }

        // Store callbacks
        CallbackManager.setCallbacks(
            onError = onError,
            onStepCompleted = onStepCompleted,
            onVerificationCompleted = onVerificationCompleted
        )

        try {
            Log.d("OrderShieldSDK", "Starting VerificationActivity")
            val intent = Intent(activity, VerificationActivity::class.java)
            activity.startActivity(intent)
            Log.d("OrderShieldSDK", "VerificationActivity started successfully")
        } catch (e: Exception) {
            Log.e("OrderShieldSDK", "Failed to start VerificationActivity: ${e.message}", e)
            onError("Failed to start verification: ${e.message}")
        }
    }
}

