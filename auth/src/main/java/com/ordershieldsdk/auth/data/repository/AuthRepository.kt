package com.ordershieldsdk.auth.data.repository

import com.ordershieldsdk.auth.core.NetworkModule
import com.ordershieldsdk.auth.data.api.AuthApiService
import com.ordershieldsdk.auth.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Repository for handling authentication API calls
 */
class AuthRepository {
    
    private val apiService: AuthApiService by lazy {
        NetworkModule.getInstance().createService()
    }

    /**
     * Send OTP to email
     */
    suspend fun sendEmailOtp(email: String): Result<SendOtpResponse> {
        return try {
            val request = SendOtpRequest(email = email)
            val response = apiService.sendEmailOtp(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            } else {
                Result.failure(Exception(response.body()?.error ?: response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify email OTP
     */
    suspend fun verifyEmailOtp(email: String, otp: String): Result<VerifyOtpResponse> {
        return try {
            val request = VerifyOtpRequest(email = email, otp = otp)
            val response = apiService.verifyEmailOtp(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            } else {
                Result.failure(Exception(response.body()?.error ?: response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send OTP to phone
     */
    suspend fun sendPhoneOtp(phone: String): Result<SendOtpResponse> {
        return try {
            val request = SendOtpRequest(phone = phone)
            val response = apiService.sendPhoneOtp(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            } else {
                Result.failure(Exception(response.body()?.error ?: response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify phone OTP
     */
    suspend fun verifyPhoneOtp(phone: String, otp: String): Result<VerifyOtpResponse> {
        return try {
            val request = VerifyOtpRequest(phone = phone, otp = otp)
            val response = apiService.verifyPhoneOtp(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            } else {
                Result.failure(Exception(response.body()?.error ?: response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload image (selfie or signature)
     */
    suspend fun uploadImage(imageFile: File, type: String): Result<ImageUploadResponse> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = apiService.uploadImage(typePart, imagePart)
            
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            } else {
                Result.failure(Exception(response.body()?.error ?: response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Submit complete verification data
     */
    suspend fun submitVerification(
        firstName: String,
        lastName: String,
        dateOfBirth: String,
        email: String,
        phone: String,
        selfieImageId: String?,
        signatureImageId: String?,
        termsAccepted: Boolean
    ): Result<VerificationSubmitResponse> {
        return try {
            val request = VerificationSubmitRequest(
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = dateOfBirth,
                email = email,
                phone = phone,
                selfieImageId = selfieImageId,
                signatureImageId = signatureImageId,
                termsAccepted = termsAccepted
            )
            
            val response = apiService.submitVerification(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            } else {
                Result.failure(Exception(response.body()?.error ?: response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get verification settings from the API
     * This should be called after SDK initialization to fetch verification configuration
     */
    suspend fun getVerificationSettings(): Result<VerificationSettingsData> {
        return try {
            val response = apiService.getVerificationSettings()
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success" && body.data.success) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message ?: "Failed to fetch verification settings"))
                }
            } else {
                Result.failure(
                    Exception(
                        response.body()?.message 
                            ?: response.message() 
                            ?: "Unknown error occurred"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register device
     * Returns customer_id on success
     */
    suspend fun registerDevice(request: com.ordershieldsdk.auth.data.model.RegisterDeviceRequest): Result<String> {
        return try {
            val response = apiService.registerDevice(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success" && body.data.success) {
                    body.data.customerId?.let { customerId ->
                        Result.success(customerId)
                    } ?: Result.failure(Exception(body.data.error ?: "Customer ID not found in response"))
                } else {
                    Result.failure(Exception(body.data.error ?: body.message ?: "Failed to register device"))
                }
            } else {
                Result.failure(
                    Exception(
                        response.body()?.message 
                            ?: response.message() 
                            ?: "Unknown error occurred"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Start verification session
     * Returns Pair of session_id and session_token on success
     */
    suspend fun startVerification(customerId: String): Result<Pair<String, String>> {
        return try {
            val request = com.ordershieldsdk.auth.data.model.StartVerificationRequest(customerId = customerId)
            val response = apiService.startVerification(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success") {
                    val sessionId = body.data.sessionId
                    val sessionToken = body.data.sessionToken
                    Result.success(Pair(sessionId, sessionToken))
                } else {
                    Result.failure(Exception(body.message ?: "Failed to start verification"))
                }
            } else {
                Result.failure(
                    Exception(
                        response.body()?.message 
                            ?: response.message() 
                            ?: "Unknown error occurred"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
