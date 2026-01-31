package com.ordershieldsdk.auth.core

/**
 * Manager to store and access verification callbacks
 * Used to notify app module about step completions and verification status
 */
internal object CallbackManager {
    
    @Volatile
    private var callback: VerificationCallback? = null
    
    @Volatile
    private var onResultCallback: ((Boolean) -> Unit)? = null
    
    /**
     * Set the verification callback (for step-by-step notifications)
     */
    fun setCallback(callback: VerificationCallback?) {
        this.callback = callback
    }
    
    /**
     * Set the simple result callback (for final result only)
     */
    fun setOnResultCallback(onResult: ((Boolean) -> Unit)?) {
        this.onResultCallback = onResult
    }
    
    /**
     * Get the current callback
     */
    fun getCallback(): VerificationCallback? = callback
    
    /**
     * Notify that a step is completed
     */
    fun notifyStepCompleted(step: String) {
        callback?.onStepCompleted(step)
    }
    
    /**
     * Notify that verification is completed
     */
    fun notifyVerificationCompleted() {
        callback?.onVerificationCompleted()
        // Also call the simple onResult callback with success
        onResultCallback?.invoke(true)
    }
    
    /**
     * Notify that verification failed
     */
    fun notifyVerificationFailed(error: String) {
        callback?.onVerificationFailed(error)
        // Also call the simple onResult callback with failure
        onResultCallback?.invoke(false)
    }
    
    /**
     * Clear all callbacks
     */
    fun clear() {
        callback = null
        onResultCallback = null
    }
}
