package com.ordershieldsdk.auth.core

/**
 * Callback interface for verification flow events
 * Implement this in your app module to receive step completion notifications
 */
interface VerificationCallback {
    /**
     * Called when a verification step is completed
     * @param step The step that was completed (e.g., "selfie", "email", "sms", "userInfo", "terms", "signature")
     */
    fun onStepCompleted(step: String)
    
    /**
     * Called when all verification steps are completed successfully
     */
    fun onVerificationCompleted()
    
    /**
     * Called when verification fails or is cancelled
     * @param error Error message describing what went wrong
     */
    fun onVerificationFailed(error: String)
}
