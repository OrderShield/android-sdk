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

    @Volatile
    private var appContext: Context? = null

    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository by lazy { AuthRepository() }

    /**
     * Initialize SDK with configuration (internal method)
     * @param config SDK configuration containing API key, base URL, and optional context.
     */
    private fun initInternal(config: SDKConfig) {
        try {
            config.context?.let { ctx ->
                appContext = ctx.applicationContext
                SdkPreferences.initialize(ctx)
            }
            NetworkModule.getInstance().initialize(config)
            isInitialized = true

            fetchVerificationSettings()

            // Register device and create session at init; persist in SharedPreferences
            config.context?.let { ensureSessionAsync(it) }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize SDK: ${e.message}", e)
        }
    }

    /**
     * Runs register device + start verification and persists session in SharedPreferences.
     * Called during init when context is available; also used as fallback by sendEvent.
     */
    private fun ensureSessionAsync(context: Context) {
        sdkScope.launch {
            try {
                val deviceInfo = DeviceInfoHelper.getDeviceInfo(context)
                repository.registerDevice(deviceInfo)
                    .onSuccess { customerId ->
                        SessionManager.setCustomerId(customerId)
                        repository.startVerification(customerId)
                            .onSuccess { (sessionId, sessionToken) ->
                                SessionManager.setSession(sessionId, sessionToken)
                                Log.d("OrderShieldSDK", "Session created and stored at init")
                            }
                            .onFailure { e ->
                                Log.e("OrderShieldSDK", "startVerification at init failed: ${e.message}")
                            }
                    }
                    .onFailure { e ->
                        Log.e("OrderShieldSDK", "registerDevice at init failed: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("OrderShieldSDK", "ensureSession failed: ${e.message}", e)
            }
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
            enableLogging = enableLogging,
            context = context.applicationContext
        )
        initInternal(config)
    }

    /**
     * Initialize SDK with configuration
     * @param config SDK configuration (include context for session persistence and register/session at init)
     */
    fun init(config: SDKConfig) {
        initInternal(config)
    }

    // --------------- User data keys (stored in SharedPreferences; usage TBD) ---------------

    fun setFirstName(value: String) {
        if (SdkPreferences.isInitialized()) SdkPreferences.setFirstName(value)
    }

    fun setLastName(value: String) {
        if (SdkPreferences.isInitialized()) SdkPreferences.setLastName(value)
    }

    fun setDOB(value: String) {
        if (SdkPreferences.isInitialized()) SdkPreferences.setDob(value)
    }

    fun setPhoneNumber(value: String) {
        if (SdkPreferences.isInitialized()) SdkPreferences.setPhoneNumber(value)
    }

    fun setEmail(value: String) {
        if (SdkPreferences.isInitialized()) SdkPreferences.setEmail(value)
    }

    /**
     * Send a custom event to the track-event API.
     * If customer_id and session_token are already in SharedPreferences, uses them.
     * Otherwise calls register device + start verification to obtain session, then sends the event.
     * Safe to call from outside the SDK; no-op if SDK was not initialized with context.
     */
    fun sendEvent(eventName: String, eventValue: String) {
        if (!SdkPreferences.isInitialized()) {
            Log.w("OrderShieldSDK", "sendEvent ignored: SDK not initialized with context")
            return
        }
        sdkScope.launch {
            if (SessionManager.hasSession()) {
                EventTracker.trackEvent(eventName, eventValue)
            } else {
                val ctx = appContext ?: return@launch
                try {
                    val deviceInfo = DeviceInfoHelper.getDeviceInfo(ctx)
                    repository.registerDevice(deviceInfo)
                        .onSuccess { customerId ->
                            SessionManager.setCustomerId(customerId)
                            repository.startVerification(customerId)
                                .onSuccess { (sessionId, sessionToken) ->
                                    SessionManager.setSession(sessionId, sessionToken)
                                    EventTracker.trackEvent(eventName, eventValue)
                                }
                                .onFailure { e ->
                                    Log.e("OrderShieldSDK", "sendEvent: startVerification failed: ${e.message}")
                                }
                        }
                        .onFailure { e ->
                            Log.e("OrderShieldSDK", "sendEvent: registerDevice failed: ${e.message}")
                        }
                } catch (e: Exception) {
                    Log.e("OrderShieldSDK", "sendEvent failed: ${e.message}", e)
                }
            }
        }
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

