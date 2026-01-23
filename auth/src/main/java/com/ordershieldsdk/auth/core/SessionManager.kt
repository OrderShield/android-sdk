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
     * Get session ID
     */
    fun getSessionId(): String? = sessionId
    
    /**
     * Get session token
     */
    fun getSessionToken(): String? = sessionToken
    
    /**
     * Clear all session data
     * Call this when verification completes or is cancelled
     */
    fun clear() {
        customerId = null
        sessionId = null
        sessionToken = null
    }
    
    /**
     * Check if session is initialized
     */
    fun hasSession(): Boolean {
        return customerId != null && sessionId != null && sessionToken != null
    }
}
