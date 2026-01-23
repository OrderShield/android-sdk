package com.ordershieldsdk.auth.core

import com.ordershieldsdk.auth.data.model.VerificationSettings
import com.ordershieldsdk.auth.data.model.VerificationSettingsData

/**
 * Manager to store and access verification settings
 * Settings are fetched from API during SDK initialization
 */
internal object VerificationSettingsManager {
    
    @Volatile
    private var settings: VerificationSettingsData? = null
    
    /**
     * Store verification settings
     */
    fun setSettings(settings: VerificationSettingsData) {
        this.settings = settings
    }
    
    /**
     * Get verification settings
     */
    fun getSettings(): VerificationSettingsData? = settings
    
    /**
     * Check if selfie verification is enabled
     */
    fun isSelfieVerificationEnabled(): Boolean {
        return settings?.settings?.selfieVerificationEnabled ?: false
    }
    
    /**
     * Check if user info verification is enabled
     */
    fun isUserInfoVerificationEnabled(): Boolean {
        return settings?.settings?.userInfoVerificationEnabled ?: false
    }
    
    /**
     * Check if email verification is enabled
     */
    fun isEmailVerificationEnabled(): Boolean {
        return settings?.settings?.emailVerificationEnabled ?: false
    }
    
    /**
     * Check if email verification is required (OTP needed)
     */
    fun isEmailVerificationRequired(): Boolean {
        return settings?.settings?.emailVerificationRequired ?: false
    }
    
    /**
     * Check if SMS verification is enabled
     */
    fun isSmsVerificationEnabled(): Boolean {
        return settings?.settings?.smsVerificationEnabled ?: false
    }
    
    /**
     * Check if SMS verification is required (OTP needed)
     */
    fun isSmsVerificationRequired(): Boolean {
        return settings?.settings?.smsVerificationRequired ?: false
    }
    
    /**
     * Check if terms agreement is enabled
     */
    fun isTermsAgreementEnabled(): Boolean {
        return settings?.settings?.termsAgreementEnabled ?: false
    }
    
    /**
     * Check if signature confirmation is enabled
     */
    fun isSignatureConfirmationEnabled(): Boolean {
        return settings?.settings?.signatureConfirmationEnabled ?: false
    }
    
    /**
     * Get all settings
     */
    fun getAllSettings(): VerificationSettings? {
        return settings?.settings
    }
    
    /**
     * Check if settings are loaded
     */
    fun hasSettings(): Boolean = settings != null
}
