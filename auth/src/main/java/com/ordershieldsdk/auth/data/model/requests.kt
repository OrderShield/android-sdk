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
