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

/**
 * Register device response data
 */
data class RegisterDeviceData(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("customer_id")
    val customerId: String?,
    
    @SerializedName("is_new_customer")
    val isNewCustomer: Boolean?,
    
    @SerializedName("is_banned")
    val isBanned: Boolean?,
    
    @SerializedName("error")
    val error: String?,
    
    @SerializedName("ban_reason")
    val banReason: String?,
    
    @SerializedName("banned_at")
    val bannedAt: String?,
    
    @SerializedName("contact")
    val contact: String?
)

/**
 * Register device API response wrapper
 */
data class RegisterDeviceResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: RegisterDeviceData,
    
    @SerializedName("dto")
    val dto: Map<String, Any>?,
    
    @SerializedName("statusCode")
    val statusCode: Int
)

/**
 * Start verification response data
 */
data class StartVerificationData(
    @SerializedName("session_id")
    val sessionId: String,
    
    @SerializedName("session_token")
    val sessionToken: String,
    
    @SerializedName("steps_required")
    val stepsRequired: List<String>?,
    
    @SerializedName("steps_optional")
    val stepsOptional: List<String>?,
    
    @SerializedName("expires_at")
    val expiresAt: String?,
    
    @SerializedName("created_at")
    val createdAt: String?
)

/**
 * Start verification API response wrapper
 */
data class StartVerificationResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: StartVerificationData,
    
    @SerializedName("dto")
    val dto: Map<String, Any>?,
    
    @SerializedName("statusCode")
    val statusCode: Int
)

/**
 * Verification session info
 */
data class VerificationSession(
    @SerializedName("session_id")
    val sessionId: String,
    
    @SerializedName("steps_completed")
    val stepsCompleted: List<String>?,
    
    @SerializedName("steps_remaining")
    val stepsRemaining: List<String>?,
    
    @SerializedName("steps_optional")
    val stepsOptional: List<String>?,
    
    @SerializedName("is_complete")
    val isComplete: Boolean?,
    
    @SerializedName("completed_at")
    val completedAt: String?
)

/**
 * Selfie upload response data
 */
data class SelfieUploadData(
    @SerializedName("step_completed")
    val stepCompleted: String,
    
    @SerializedName("verification_session")
    val verificationSession: VerificationSession?
)

/**
 * Selfie upload API response wrapper
 */
data class SelfieUploadResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: SelfieUploadData,
    
    @SerializedName("dto")
    val dto: Map<String, Any>?,
    
    @SerializedName("statusCode")
    val statusCode: Int
)

/**
 * User info upload response data
 */
data class UserInfoUploadData(
    @SerializedName("step_completed")
    val stepCompleted: String,
    
    @SerializedName("verification_session")
    val verificationSession: VerificationSession?
)

/**
 * User info upload API response wrapper
 */
data class UserInfoUploadResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: UserInfoUploadData,
    
    @SerializedName("dto")
    val dto: Map<String, Any>?,
    
    @SerializedName("statusCode")
    val statusCode: Int
)

/**
 * Send email code response data
 */
data class SendEmailCodeData(
    @SerializedName("code_sent")
    val codeSent: Boolean?,
    
    @SerializedName("message")
    val message: String?
)

/**
 * Send email code API response wrapper
 */
data class SendEmailCodeResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: SendEmailCodeData?,
    
    @SerializedName("dto")
    val dto: Map<String, Any>?,
    
    @SerializedName("statusCode")
    val statusCode: Int
)

/**
 * Verify email code response data
 */
data class VerifyEmailCodeData(
    @SerializedName("verified")
    val verified: Boolean?,
    
    @SerializedName("message")
    val message: String?
)

/**
 * Verify email code API response wrapper
 */
data class VerifyEmailCodeResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: VerifyEmailCodeData?,
    
    @SerializedName("dto")
    val dto: Map<String, Any>?,
    
    @SerializedName("statusCode")
    val statusCode: Int
)

/**
 * Send phone code response data
 */
data class SendPhoneCodeData(
    @SerializedName("code_sent")
    val codeSent: Boolean?,
    
    @SerializedName("message")
    val message: String?
)

/**
 * Send phone code API response wrapper
 */
data class SendPhoneCodeResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: SendPhoneCodeData?,
    
    @SerializedName("dto")
    val dto: Map<String, Any>?,
    
    @SerializedName("statusCode")
    val statusCode: Int
)

/**
 * Verify phone code response data
 */
data class VerifyPhoneCodeData(
    @SerializedName("verified")
    val verified: Boolean?,
    
    @SerializedName("message")
    val message: String?
)

/**
 * Verify phone code API response wrapper
 */
data class VerifyPhoneCodeResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: VerifyPhoneCodeData?,
    
    @SerializedName("dto")
    val dto: Map<String, Any>?,
    
    @SerializedName("statusCode")
    val statusCode: Int
)

/**
 * Terms checkbox item
 */
data class TermsCheckbox(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("checkboxText")
    val checkboxText: String,
    
    @SerializedName("isRequired")
    val isRequired: Boolean,
    
    @SerializedName("displayOrder")
    val displayOrder: Int
)

/**
 * Terms checkboxes API response wrapper
 */
data class TermsCheckboxesResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<TermsCheckbox>,
    
    @SerializedName("statusCode")
    val statusCode: Int,
    
    @SerializedName("status")
    val status: String
)

/**
 * Submit terms response data
 */
data class SubmitTermsData(
    @SerializedName("step_completed")
    val stepCompleted: String?,
    
    @SerializedName("verification_session")
    val verificationSession: VerificationSession?
)

/**
 * Submit terms API response wrapper
 */
data class SubmitTermsResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: SubmitTermsData?,
    
    @SerializedName("dto")
    val dto: Map<String, Any>?,
    
    @SerializedName("statusCode")
    val statusCode: Int
)

/**
 * Upload signature response data
 */
data class UploadSignatureData(
    @SerializedName("step_completed")
    val stepCompleted: String?,
    
    @SerializedName("verification_session")
    val verificationSession: VerificationSession?
)

/**
 * Upload signature API response wrapper
 */
data class UploadSignatureResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: UploadSignatureData?,
    
    @SerializedName("dto")
    val dto: Map<String, Any>?,
    
    @SerializedName("statusCode")
    val statusCode: Int
)
