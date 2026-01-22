package com.ordershieldsdk.auth.data.api

import com.ordershieldsdk.auth.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface for OrderShield authentication endpoints
 */
interface AuthApiService {

    /**
     * Send OTP to email
     */
    @POST("auth/send-email-otp")
    suspend fun sendEmailOtp(
        @Body request: SendOtpRequest
    ): Response<ApiResponse<SendOtpResponse>>

    /**
     * Verify email OTP
     */
    @POST("auth/verify-email-otp")
    suspend fun verifyEmailOtp(
        @Body request: VerifyOtpRequest
    ): Response<ApiResponse<VerifyOtpResponse>>

    /**
     * Send OTP to phone
     */
    @POST("auth/send-phone-otp")
    suspend fun sendPhoneOtp(
        @Body request: SendOtpRequest
    ): Response<ApiResponse<SendOtpResponse>>

    /**
     * Verify phone OTP
     */
    @POST("auth/verify-phone-otp")
    suspend fun verifyPhoneOtp(
        @Body request: VerifyOtpRequest
    ): Response<ApiResponse<VerifyOtpResponse>>

    /**
     * Submit verification data
     */
    @POST("auth/submit-verification")
    suspend fun submitVerification(
        @Body request: VerificationSubmitRequest
    ): Response<ApiResponse<VerificationSubmitResponse>>

    /**
     * Upload image (selfie or signature)
     */
    @Multipart
    @POST("auth/upload-image")
    suspend fun uploadImage(
        @Part("type") type: okhttp3.RequestBody,
        @Part image: okhttp3.MultipartBody.Part
    ): Response<ApiResponse<ImageUploadResponse>>
}
