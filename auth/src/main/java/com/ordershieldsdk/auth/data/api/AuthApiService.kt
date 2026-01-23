package com.ordershieldsdk.auth.data.api

import com.ordershieldsdk.auth.data.model.ApiResponse
import com.ordershieldsdk.auth.data.model.ImageUploadResponse
import com.ordershieldsdk.auth.data.model.RegisterDeviceRequest
import com.ordershieldsdk.auth.data.model.RegisterDeviceResponse
import com.ordershieldsdk.auth.data.model.SendEmailCodeRequest
import com.ordershieldsdk.auth.data.model.SendEmailCodeResponse
import com.ordershieldsdk.auth.data.model.SendOtpRequest
import com.ordershieldsdk.auth.data.model.SendOtpResponse
import com.ordershieldsdk.auth.data.model.SendPhoneCodeRequest
import com.ordershieldsdk.auth.data.model.SendPhoneCodeResponse
import com.ordershieldsdk.auth.data.model.SelfieUploadResponse
import com.ordershieldsdk.auth.data.model.StartVerificationRequest
import com.ordershieldsdk.auth.data.model.StartVerificationResponse
import com.ordershieldsdk.auth.data.model.SubmitTermsRequest
import com.ordershieldsdk.auth.data.model.SubmitTermsResponse
import com.ordershieldsdk.auth.data.model.TermsCheckboxesResponse
import com.ordershieldsdk.auth.data.model.UploadSignatureResponse
import com.ordershieldsdk.auth.data.model.UserInfoRequest
import com.ordershieldsdk.auth.data.model.UserInfoUploadResponse
import com.ordershieldsdk.auth.data.model.VerificationSettingsResponse
import com.ordershieldsdk.auth.data.model.VerifyEmailCodeRequest
import com.ordershieldsdk.auth.data.model.VerifyEmailCodeResponse
import com.ordershieldsdk.auth.data.model.VerifyOtpRequest
import com.ordershieldsdk.auth.data.model.VerifyOtpResponse
import com.ordershieldsdk.auth.data.model.VerifyPhoneCodeRequest
import com.ordershieldsdk.auth.data.model.VerifyPhoneCodeResponse
import com.ordershieldsdk.auth.data.model.VerificationSubmitRequest
import com.ordershieldsdk.auth.data.model.VerificationSubmitResponse
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

    /**
     * Get verification settings
     * This endpoint returns the verification configuration for the SDK
     */
    @GET("api/sdk/verification-settings")
    suspend fun getVerificationSettings(): Response<VerificationSettingsResponse>

    /**
     * Register device
     * This endpoint registers the device and returns customer_id
     */
    @POST("api/sdk/register-device")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest
    ): Response<RegisterDeviceResponse>

    /**
     * Start verification session
     * This endpoint starts a verification session and returns session_id and session_token
     */
    @POST("api/sdk/verification/start")
    suspend fun startVerification(
        @Body request: StartVerificationRequest
    ): Response<StartVerificationResponse>

    /**
     * Upload selfie image
     * Multipart form data with selfie_image, customer_id, session_token, and image_format
     */
    @Multipart
    @POST("api/sdk/verification/selfie")
    suspend fun uploadSelfie(
        @Part selfieImage: okhttp3.MultipartBody.Part,
        @Part("customer_id") customerId: okhttp3.RequestBody,
        @Part("session_token") sessionToken: okhttp3.RequestBody,
        @Part("image_format") imageFormat: okhttp3.RequestBody
    ): Response<SelfieUploadResponse>

    /**
     * Submit user information
     * POST with customer_id, session_token, first_name, last_name, date_of_birth
     */
    @POST("api/sdk/verification/user-info")
    suspend fun submitUserInfo(
        @Body request: UserInfoRequest
    ): Response<UserInfoUploadResponse>

    /**
     * Send email verification code
     * POST with customer_id, session_token, email
     */
    @POST("api/sdk/verification/email/send-code")
    suspend fun sendEmailCode(
        @Body request: SendEmailCodeRequest
    ): Response<SendEmailCodeResponse>

    /**
     * Verify email verification code
     * POST with customer_id, session_token, email, verification_code
     */
    @POST("api/sdk/verification/email/verify-code")
    suspend fun verifyEmailCode(
        @Body request: VerifyEmailCodeRequest
    ): Response<VerifyEmailCodeResponse>

    /**
     * Send phone verification code
     * POST with customer_id, session_token, phone_number
     */
    @POST("api/sdk/verification/phone/send-code")
    suspend fun sendPhoneCode(
        @Body request: SendPhoneCodeRequest
    ): Response<SendPhoneCodeResponse>

    /**
     * Verify phone verification code
     * POST with customer_id, session_token, phone_number, verification_code
     */
    @POST("api/sdk/verification/phone/verify-code")
    suspend fun verifyPhoneCode(
        @Body request: VerifyPhoneCodeRequest
    ): Response<VerifyPhoneCodeResponse>

    /**
     * Get terms and conditions checkboxes
     * Returns list of checkboxes with text, required status, and display order
     */
    @GET("api/sdk/terms-checkboxes")
    suspend fun getTermsCheckboxes(): Response<TermsCheckboxesResponse>

    /**
     * Submit terms acceptance
     * POST with customer_id, session_token, accepted_checkboxes
     */
    @POST("api/sdk/verification/terms")
    suspend fun submitTerms(
        @Body request: SubmitTermsRequest
    ): Response<SubmitTermsResponse>

    /**
     * Upload signature image
     * Multipart form data with signature_image, customer_id, session_token, image_format
     */
    @Multipart
    @POST("api/sdk/verification/signature")
    suspend fun uploadSignature(
        @Part signatureImage: okhttp3.MultipartBody.Part,
        @Part("customer_id") customerId: okhttp3.RequestBody,
        @Part("session_token") sessionToken: okhttp3.RequestBody,
        @Part("image_format") imageFormat: okhttp3.RequestBody
    ): Response<UploadSignatureResponse>
}
