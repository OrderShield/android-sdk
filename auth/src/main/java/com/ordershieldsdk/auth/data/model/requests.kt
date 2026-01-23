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
