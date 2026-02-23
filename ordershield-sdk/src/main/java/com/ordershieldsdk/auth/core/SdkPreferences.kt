package com.ordershieldsdk.auth.core

import android.content.Context
import android.content.SharedPreferences

/**
 * Internal persistence for SDK state using SharedPreferences.
 * Single source of truth for session and user data keys.
 * Must be initialized via [initialize] before any read/write.
 */
internal object SdkPreferences {

    private const val PREFS_NAME = "ordershield_sdk_prefs"
    private const val KEY_CUSTOMER_ID = "customer_id"
    private const val KEY_SESSION_ID = "session_id"
    private const val KEY_SESSION_TOKEN = "session_token"
    private const val KEY_STEPS_REMAINING = "steps_remaining" // comma-separated
    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_LAST_NAME = "last_name"
    private const val KEY_DOB = "dob"
    private const val KEY_PHONE_NUMBER = "phone_number"
    private const val KEY_EMAIL = "email"

    @Volatile
    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun requirePrefs(): SharedPreferences {
        return prefs ?: throw IllegalStateException("SdkPreferences not initialized. Call AuthSDK.init(context, ...) first.")
    }

    fun isInitialized(): Boolean = prefs != null

    // Session
    fun setCustomerId(value: String) = requirePrefs().edit().putString(KEY_CUSTOMER_ID, value).apply()
    fun getCustomerId(): String? = prefs?.getString(KEY_CUSTOMER_ID, null)

    fun setSession(sessionId: String, sessionToken: String) {
        requirePrefs().edit()
            .putString(KEY_SESSION_ID, sessionId)
            .putString(KEY_SESSION_TOKEN, sessionToken)
            .apply()
    }
    fun getSessionId(): String? = prefs?.getString(KEY_SESSION_ID, null)
    fun getSessionToken(): String? = prefs?.getString(KEY_SESSION_TOKEN, null)

    fun setStepsRemaining(steps: List<String>?) {
        val value = steps?.joinToString(COMMA) ?: ""
        requirePrefs().edit().putString(KEY_STEPS_REMAINING, value).apply()
    }
    fun getStepsRemaining(): List<String>? {
        val raw = prefs?.getString(KEY_STEPS_REMAINING, null) ?: return null
        if (raw.isEmpty()) return emptyList()
        return raw.split(COMMA).filter { it.isNotBlank() }
    }

    fun clearSession() {
        requirePrefs().edit()
            .remove(KEY_CUSTOMER_ID)
            .remove(KEY_SESSION_ID)
            .remove(KEY_SESSION_TOKEN)
            .remove(KEY_STEPS_REMAINING)
            .apply()
    }

    // User data keys (for prefill / external use)
    fun setFirstName(value: String) { if (prefs != null) requirePrefs().edit().putString(KEY_FIRST_NAME, value).apply() }
    fun getFirstName(): String? = prefs?.getString(KEY_FIRST_NAME, null)

    fun setLastName(value: String) { if (prefs != null) requirePrefs().edit().putString(KEY_LAST_NAME, value).apply() }
    fun getLastName(): String? = prefs?.getString(KEY_LAST_NAME, null)

    fun setDob(value: String) { if (prefs != null) requirePrefs().edit().putString(KEY_DOB, value).apply() }
    fun getDob(): String? = prefs?.getString(KEY_DOB, null)

    fun setPhoneNumber(value: String) { if (prefs != null) requirePrefs().edit().putString(KEY_PHONE_NUMBER, value).apply() }
    fun getPhoneNumber(): String? = prefs?.getString(KEY_PHONE_NUMBER, null)

    fun setEmail(value: String) { if (prefs != null) requirePrefs().edit().putString(KEY_EMAIL, value).apply() }
    fun getEmail(): String? = prefs?.getString(KEY_EMAIL, null)

    private const val COMMA = ","
}
