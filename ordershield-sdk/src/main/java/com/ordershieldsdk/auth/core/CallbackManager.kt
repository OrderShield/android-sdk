package com.ordershieldsdk.auth.core

/**
 * Manager to store and access verification callbacks
 * Used to notify app module about step completions and verification status
 */
internal object CallbackManager {
    
    @Volatile
    private var onError: ((String) -> Unit)? = null
    
    @Volatile
    private var onStepCompleted: ((String) -> Unit)? = null
    
    @Volatile
    private var onVerificationCompleted: (() -> Unit)? = null
    
    /**
     * Set all callbacks for verification flow
     */
    fun setCallbacks(
        onError: (String) -> Unit,
        onStepCompleted: (String) -> Unit = {},
        onVerificationCompleted: () -> Unit = {}
    ) {
        this.onError = onError
        this.onStepCompleted = onStepCompleted
        this.onVerificationCompleted = onVerificationCompleted
    }
    
    /**
     * Notify that a step is completed
     */
    fun notifyStepCompleted(step: String) {
        onStepCompleted?.invoke(step)
    }
    
    /**
     * Notify that verification is completed (user exits completion screen)
     */
    fun notifyVerificationCompleted() {
        onVerificationCompleted?.invoke()
    }
    
    /**
     * Notify that verification failed (for internal errors)
     */
    fun notifyVerificationFailed(error: String) {
        onError?.invoke(error)
    }
    
    /**
     * Clear all callbacks
     */
    fun clear() {
        onError = null
        onStepCompleted = null
        onVerificationCompleted = null
    }
}
