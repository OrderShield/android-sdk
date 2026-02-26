package com.ordershieldsdk.auth.core

import com.ordershieldsdk.auth.data.model.CustomerInfoCustomer

/**
 * Manager to store and read session information during verification.
 * All data is persisted via [SdkPreferences] (SharedPreferences).
 */
internal object SessionManager {

    @Volatile
    private var customerFromApi: CustomerInfoCustomer? = null

    fun setCustomerFromApi(customer: CustomerInfoCustomer?) {
        customerFromApi = customer
    }

    fun getCustomerFromApi(): CustomerInfoCustomer? = customerFromApi

    fun setCustomerId(customerId: String) {
        SdkPreferences.setCustomerId(customerId)
    }

    fun getCustomerId(): String? = SdkPreferences.getCustomerId()

    fun setSession(sessionId: String, sessionToken: String) {
        SdkPreferences.setSession(sessionId, sessionToken)
    }

    fun getSessionId(): String? = SdkPreferences.getSessionId()

    fun getSessionToken(): String? = SdkPreferences.getSessionToken()

    fun setStepsRemaining(stepsRemaining: List<String>?) {
        SdkPreferences.setStepsRemaining(stepsRemaining)
    }

    fun getStepsRemaining(): List<String>? = SdkPreferences.getStepsRemaining()

    fun setStepsCompleted(steps: List<String>?) {
        SdkPreferences.setStepsCompleted(steps)
    }
    fun getStepsCompleted(): List<String>? = SdkPreferences.getStepsCompleted()

    fun getStepsOptional(): List<String>? = null // kept for API compatibility; unused

    fun isStepRemaining(step: String): Boolean {
        return getStepsRemaining()?.contains(step) == true
    }

    /**
     * Clear all session data (customer_id, session_id, session_token, steps_remaining, customer-from-API).
     * Call when verification completes or is cancelled.
     */
    fun clear() {
        customerFromApi = null
        if (SdkPreferences.isInitialized()) {
            SdkPreferences.clearSession()
        }
    }

    fun hasSession(): Boolean {
        return getCustomerId() != null && getSessionId() != null && getSessionToken() != null
    }

    fun getFirstName(): String? = SdkPreferences.getFirstName()
    fun getLastName(): String? = SdkPreferences.getLastName()
    fun getDob(): String? = SdkPreferences.getDob()
    fun getPhoneNumber(): String? = SdkPreferences.getPhoneNumber()
    fun getEmail(): String? = SdkPreferences.getEmail()
}
