package com.ordershieldsdk.auth.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for sending OTP
 */
data class SendOtpRequest(
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null
)

/**
 * Request model for verifying OTP
 */
data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("otp")
    val otp: String
)

/**
 * Request model for submitting verification data
 */
data class VerificationSubmitRequest(
    @SerializedName("firstName")
    val firstName: String,
    
    @SerializedName("lastName")
    val lastName: String,
    
    @SerializedName("dateOfBirth")
    val dateOfBirth: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("selfieImageId")
    val selfieImageId: String? = null,
    
    @SerializedName("signatureImageId")
    val signatureImageId: String? = null,
    
    @SerializedName("termsAccepted")
    val termsAccepted: Boolean
)

/**
 * Request model for device registration
 */
data class RegisterDeviceRequest(
    @SerializedName("device_id")
    val deviceId: String,
    
    @SerializedName("device_type")
    val deviceType: String,
    
    @SerializedName("device_model")
    val deviceModel: String,
    
    @SerializedName("os_version")
    val osVersion: String,
    
    @SerializedName("app_version")
    val appVersion: String,
    
    @SerializedName("ip_address")
    val ipAddress: String,
    
    @SerializedName("user_agent")
    val userAgent: String,
    
    @SerializedName("timezone")
    val timezone: String
)

/**
 * Request model for starting verification
 */
data class StartVerificationRequest(
    @SerializedName("customer_id")
    val customerId: String
)

/**
 * Request model for submitting user information
 */
data class UserInfoRequest(
    @SerializedName("customer_id")
    val customerId: String,
    
    @SerializedName("session_token")
    val sessionToken: String,
    
    @SerializedName("first_name")
    val firstName: String,
    
    @SerializedName("last_name")
    val lastName: String,
    
    @SerializedName("date_of_birth")
    val dateOfBirth: String
)

/**
 * Request model for sending email verification code
 */
data class SendEmailCodeRequest(
    @SerializedName("customer_id")
    val customerId: String,
    
    @SerializedName("session_token")
    val sessionToken: String,
    
    @SerializedName("email")
    val email: String
)

/**
 * Request model for verifying email verification code
 */
data class VerifyEmailCodeRequest(
    @SerializedName("customer_id")
    val customerId: String,
    
    @SerializedName("session_token")
    val sessionToken: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("verification_code")
    val verificationCode: String
)

/**
 * Request model for sending phone verification code
 */
data class SendPhoneCodeRequest(
    @SerializedName("customer_id")
    val customerId: String,
    
    @SerializedName("session_token")
    val sessionToken: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String
)

/**
 * Request model for verifying phone verification code
 */
data class VerifyPhoneCodeRequest(
    @SerializedName("customer_id")
    val customerId: String,
    
    @SerializedName("session_token")
    val sessionToken: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    @SerializedName("verification_code")
    val verificationCode: String
)

/**
 * Accepted checkbox item
 */
data class AcceptedCheckbox(
    @SerializedName("checkbox_id")
    val checkboxId: String,
    
    @SerializedName("accepted")
    val accepted: Boolean
)

/**
 * Request model for submitting terms acceptance
 */
data class SubmitTermsRequest(
    @SerializedName("customer_id")
    val customerId: String,
    
    @SerializedName("session_token")
    val sessionToken: String,
    
    @SerializedName("accepted_checkboxes")
    val acceptedCheckboxes: List<AcceptedCheckbox>
)

/**
 * Request model for tracking events
 */
data class TrackEventRequest(
    @SerializedName("customer_id")
    val customerId: String,
    
    @SerializedName("session_token")
    val sessionToken: String,
    
    @SerializedName("event_type")
    val eventType: String,
    
    @SerializedName("description")
    val description: String
)
