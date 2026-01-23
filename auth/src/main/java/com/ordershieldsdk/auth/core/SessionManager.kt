package com.ordershieldsdk.auth.core

/**
 * Manager to store session information during verification
 * Stores customer_id, session_id, and session_token until verification completes
 */
internal object SessionManager {
    
    @Volatile
    private var customerId: String? = null
    
    @Volatile
    private var sessionId: String? = null
    
    @Volatile
    private var sessionToken: String? = null
    
    @Volatile
    private var stepsRemaining: List<String>? = null
    
    @Volatile
    private var stepsOptional: List<String>? = null
    
    /**
     * Store customer ID
     */
    fun setCustomerId(customerId: String) {
        this.customerId = customerId
    }
    
    /**
     * Get customer ID
     */
    fun getCustomerId(): String? = customerId
    
    /**
     * Store session ID and token
     */
    fun setSession(sessionId: String, sessionToken: String) {
        this.sessionId = sessionId
        this.sessionToken = sessionToken
    }
    
    /**
     * Store steps remaining from verification status
     */
    fun setStepsRemaining(stepsRemaining: List<String>?) {
        this.stepsRemaining = stepsRemaining
    }
    
    /**
     * Get session ID
     */
    fun getSessionId(): String? = sessionId
    
    /**
     * Get session token
     */
    fun getSessionToken(): String? = sessionToken
    
    /**
     * Get remaining steps for this session
     */
    fun getStepsRemaining(): List<String>? = stepsRemaining
    
    /**
     * Get optional steps for this session
     */
    fun getStepsOptional(): List<String>? = stepsOptional
    
    /**
     * Check if a step is in remaining steps
     */
    fun isStepRemaining(step: String): Boolean {
        return stepsRemaining?.contains(step) == true
    }
    
    /**
     * Clear all session data
     * Call this when verification completes or is cancelled
     */
    fun clear() {
        customerId = null
        sessionId = null
        sessionToken = null
        stepsRemaining = null
        stepsOptional = null
    }
    
    /**
     * Check if session is initialized
     */
    fun hasSession(): Boolean {
        return customerId != null && sessionId != null && sessionToken != null
    }
}
