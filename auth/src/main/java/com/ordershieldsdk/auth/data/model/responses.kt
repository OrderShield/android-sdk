package com.ordershieldsdk.auth.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for sending OTP
 */
data class SendOtpResponse(
    @SerializedName("otpSent")
    val otpSent: Boolean,
    
    @SerializedName("message")
    val message: String?
)

/**
 * Response model for verifying OTP
 */
data class VerifyOtpResponse(
    @SerializedName("verified")
    val verified: Boolean,
    
    @SerializedName("message")
    val message: String?
)

/**
 * Response model for image upload
 */
data class ImageUploadResponse(
    @SerializedName("imageId")
    val imageId: String,
    
    @SerializedName("imageUrl")
    val imageUrl: String?
)

/**
 * Response model for verification submission
 */
data class VerificationSubmitResponse(
    @SerializedName("verificationId")
    val verificationId: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String?
)

/**
 * Verification settings model
 */
data class VerificationSettings(
    @SerializedName("selfie_verification_enabled")
    val selfieVerificationEnabled: Boolean,
    
    @SerializedName("email_verification_enabled")
    val emailVerificationEnabled: Boolean,
    
    @SerializedName("email_verification_required")
    val emailVerificationRequired: Boolean,
    
    @SerializedName("sms_verification_enabled")
    val smsVerificationEnabled: Boolean,
    
    @SerializedName("sms_verification_required")
    val smsVerificationRequired: Boolean,
    
    @SerializedName("terms_agreement_enabled")
    val termsAgreementEnabled: Boolean,
    
    @SerializedName("signature_confirmation_enabled")
    val signatureConfirmationEnabled: Boolean,
    
    @SerializedName("user_info_verification_enabled")
    val userInfoVerificationEnabled: Boolean
)

/**
 * Verification settings data model
 */
data class VerificationSettingsData(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("verification_enabled")
    val verificationEnabled: Boolean,
    
    @SerializedName("settings")
    val settings: VerificationSettings,
    
    @SerializedName("required_steps")
    val requiredSteps: List<String>,
    
    @SerializedName("optional_steps")
    val optionalSteps: List<String>
)

/**
 * Verification settings API response wrapper
 */
data class VerificationSettingsResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: VerificationSettingsData,
    
    @SerializedName("statusCode")
    val statusCode: Int,
    
    @SerializedName("status")
    val status: String
)
