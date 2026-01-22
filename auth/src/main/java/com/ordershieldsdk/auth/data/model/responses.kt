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
