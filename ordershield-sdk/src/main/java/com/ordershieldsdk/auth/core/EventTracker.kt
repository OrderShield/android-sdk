package com.ordershieldsdk.auth.core

import android.util.Log
import com.ordershieldsdk.auth.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for tracking events
 * Handles API calls to track user events during verification flow
 */
object EventTracker {
    
    private val eventScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository = AuthRepository()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    
    /**
     * Track an event
     * @param eventType Event type (e.g., "session_start", "step_start", "step_end")
     * @param description Description/value of the event (timestamp or step name)
     */
    fun trackEvent(eventType: String, description: String) {
        // Check if session is available
        val customerId = SessionManager.getCustomerId()
        val sessionToken = SessionManager.getSessionToken()
        
        if (customerId == null || sessionToken == null) {
            Log.w("EventTracker", "Cannot track event '$eventType': Session not initialized")
            return
        }
        
        eventScope.launch {
            try {
                val result = repository.trackEvent(eventType, description)
                result.onSuccess {
                    Log.d("EventTracker", "Event tracked successfully: $eventType - $description")
                }.onFailure { exception ->
                    Log.e("EventTracker", "Failed to track event '$eventType': ${exception.message}")
                }
            } catch (e: Exception) {
                Log.e("EventTracker", "Exception while tracking event '$eventType': ${e.message}")
            }
        }
    }
    
    /**
     * Track session start event
     */
    fun trackSessionStart() {
        val timestamp = dateFormat.format(Date())
        trackEvent("session_start", timestamp)
    }
    
    /**
     * Track session end event
     */
    fun trackSessionEnd() {
        val timestamp = dateFormat.format(Date())
        trackEvent("session_end", timestamp)
    }
    
    /**
     * Track step start event
     * @param step Step enum value
     */
    fun trackStepStart(step: com.ordershieldsdk.auth.internal.StepNavigator.Step) {
        val stepName = getStepName(step)
        trackEvent("step_start", stepName)
    }
    
    /**
     * Track step end event
     * @param step Step enum value
     */
    fun trackStepEnd(step: com.ordershieldsdk.auth.internal.StepNavigator.Step) {
        val stepName = getStepName(step)
        trackEvent("step_end", stepName)
    }
    
    /**
     * Track step retry event
     * @param stepName Step name or operation name (e.g., "sms", "selfie", "register_device", "start_verification")
     */
    fun trackStepRetry(stepName: String) {
        trackEvent("step_retry", stepName)
    }
    
    /**
     * Convert StepNavigator.Step enum to step name string
     */
    private fun getStepName(step: com.ordershieldsdk.auth.internal.StepNavigator.Step): String {
        return when (step) {
            com.ordershieldsdk.auth.internal.StepNavigator.Step.PHONE -> "sms"
            com.ordershieldsdk.auth.internal.StepNavigator.Step.SELFIE -> "selfie"
            com.ordershieldsdk.auth.internal.StepNavigator.Step.USER_INFO -> "userInfo"
            com.ordershieldsdk.auth.internal.StepNavigator.Step.EMAIL -> "email"
            com.ordershieldsdk.auth.internal.StepNavigator.Step.TERMS_SIGNATURE -> "terms"
            com.ordershieldsdk.auth.internal.StepNavigator.Step.INFO -> "info"
            com.ordershieldsdk.auth.internal.StepNavigator.Step.COMPLETE -> "complete"
        }
    }
}
