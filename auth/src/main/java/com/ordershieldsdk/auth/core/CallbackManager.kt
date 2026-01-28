package com.ordershieldsdk.auth.core

/**
 * Manager to store and access verification callbacks
 * Used to notify app module about step completions and verification status
 */
internal object CallbackManager {
    
    @Volatile
    private var callback: VerificationCallback? = null
    
    /**
     * Set the verification callback
     */
    fun setCallback(callback: VerificationCallback?) {
        this.callback = callback
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
    }
    
    /**
     * Notify that verification failed
     */
    fun notifyVerificationFailed(error: String) {
        callback?.onVerificationFailed(error)
    }
    
    /**
     * Clear the callback
     */
    fun clear() {
        callback = null
    }
}
