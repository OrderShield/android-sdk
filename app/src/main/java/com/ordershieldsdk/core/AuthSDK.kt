package com.ordershieldsdk.core

import android.app.Activity
import android.content.Context
import com.ordershieldsdk.auth.core.AuthSDK as AuthModuleSDK

/**
 * Wrapper for OrderShield Auth SDK
 * Delegates to the auth module's AuthSDK
 */
object AuthSDK {
    
    /**
     * Initialize SDK with API key
     * Base URL is fixed in auth module
     */
    fun init(
        context: Context,
        apiKey: String,
        enableLogging: Boolean = false
    ) {
        AuthModuleSDK.init(context, apiKey, enableLogging)
    }

    /**
     * Start the verification flow
     */
    fun startVerification(activity: Activity, onResult: (Boolean) -> Unit = {}) {
        AuthModuleSDK.startVerification(activity, onResult)
    }
}