package com.ordershieldsdk.auth.data.repository

import com.ordershieldsdk.auth.core.NetworkModule
import com.ordershieldsdk.auth.core.SessionManager
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
    suspend fun registerDevice(request: RegisterDeviceRequest): Result<String> {
        return try {
            val response = apiService.registerDevice(request)
            print("registerDevice: $response")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success" && body.data.success) {
                    body.data.customerId?.let { customerId ->
                        Result.success(customerId)
                    } ?: Result.failure(Exception(body.data.error ?: "Customer ID not found in response"))
                } else {
                    // Check if banned
                    val errorMessage = if (body.data.isBanned == true && body.data.banReason != null) {
                        "Account banned: ${body.data.banReason}"
                    } else {
                        body.data.error ?: body.message ?: "Failed to register device"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
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
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get verification status
     * Returns steps_remaining list on success
     */
    suspend fun getVerificationStatus(): Result<List<String>?> {
        return try {
            val customerId = SessionManager.getCustomerId()
            val sessionToken = SessionManager.getSessionToken()
            
            if (customerId == null || sessionToken == null) {
                return Result.failure(Exception("Session not initialized. Please start verification first."))
            }
            
            val response = apiService.getVerificationStatus(customerId, sessionToken)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success") {
                    Result.success(body.data.stepsRemaining)
                } else {
                    Result.failure(Exception(body.message ?: "Failed to get verification status"))
                }
            } else {
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload selfie image
     * Returns success status
     */
    suspend fun uploadSelfie(imageFile: File): Result<Boolean> {
        return try {
            val customerId = SessionManager.getCustomerId()
            val sessionToken = SessionManager.getSessionToken()
            
            if (customerId == null || sessionToken == null) {
                return Result.failure(Exception("Session not initialized. Please start verification first."))
            }
            
            // Determine image format from file extension
            val imageFormat = when {
                imageFile.name.endsWith(".jpg", ignoreCase = true) || 
                imageFile.name.endsWith(".jpeg", ignoreCase = true) -> "jpg"
                imageFile.name.endsWith(".png", ignoreCase = true) -> "png"
                else -> "jpg" // Default to jpg
            }
            
            // Create multipart form data
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val selfieImagePart = MultipartBody.Part.createFormData("selfie_image", imageFile.name, requestFile)
            val customerIdPart = customerId.toRequestBody("text/plain".toMediaTypeOrNull())
            val sessionTokenPart = sessionToken.toRequestBody("text/plain".toMediaTypeOrNull())
            val imageFormatPart = imageFormat.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = apiService.uploadSelfie(
                selfieImage = selfieImagePart,
                customerId = customerIdPart,
                sessionToken = sessionTokenPart,
                imageFormat = imageFormatPart
            )
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success") {
                    Result.success(true)
                } else {
                    val errorMessage = body.message 
                        ?: body.data.verificationSession?.let { "Verification failed" }
                        ?: "Failed to upload selfie"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Submit user information
     * Returns success status
     */
    suspend fun submitUserInfo(firstName: String, lastName: String, dateOfBirth: String): Result<Boolean> {
        return try {
            val customerId = SessionManager.getCustomerId()
            val sessionToken = SessionManager.getSessionToken()
            
            if (customerId == null || sessionToken == null) {
                return Result.failure(Exception("Session not initialized. Please start verification first."))
            }
            
            val request = com.ordershieldsdk.auth.data.model.UserInfoRequest(
                customerId = customerId,
                sessionToken = sessionToken,
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = dateOfBirth
            )
            
            val response = apiService.submitUserInfo(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success") {
                    Result.success(true)
                } else {
                    val errorMessage = body.message 
                        ?: body.data.verificationSession?.let { "Verification failed" }
                        ?: "Failed to submit user information"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send email verification code
     * Returns success status
     */
    suspend fun sendEmailCode(email: String): Result<Boolean> {
        return try {
            val customerId = SessionManager.getCustomerId()
            val sessionToken = SessionManager.getSessionToken()
            
            if (customerId == null || sessionToken == null) {
                return Result.failure(Exception("Session not initialized. Please start verification first."))
            }
            
            val request = com.ordershieldsdk.auth.data.model.SendEmailCodeRequest(
                customerId = customerId,
                sessionToken = sessionToken,
                email = email
            )
            
            val response = apiService.sendEmailCode(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success") {
                    Result.success(true)
                } else {
                    val errorMessage = body.message 
                        ?: body.data?.message
                        ?: "Failed to send email verification code"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify email verification code
     * Returns success status
     */
    suspend fun verifyEmailCode(email: String, verificationCode: String): Result<Boolean> {
        return try {
            val customerId = SessionManager.getCustomerId()
            val sessionToken = SessionManager.getSessionToken()
            
            if (customerId == null || sessionToken == null) {
                return Result.failure(Exception("Session not initialized. Please start verification first."))
            }
            
            val request = com.ordershieldsdk.auth.data.model.VerifyEmailCodeRequest(
                customerId = customerId,
                sessionToken = sessionToken,
                email = email,
                verificationCode = verificationCode
            )
            
            val response = apiService.verifyEmailCode(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success") {
                    Result.success(true)
                } else {
                    val errorMessage = body.message 
                        ?: body.data?.message
                        ?: "Failed to verify email code"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send phone verification code
     * Returns success status
     */
    suspend fun sendPhoneCode(phoneNumber: String): Result<Boolean> {
        return try {
            val customerId = SessionManager.getCustomerId()
            val sessionToken = SessionManager.getSessionToken()
            
            if (customerId == null || sessionToken == null) {
                return Result.failure(Exception("Session not initialized. Please start verification first."))
            }
            
            val request = com.ordershieldsdk.auth.data.model.SendPhoneCodeRequest(
                customerId = customerId,
                sessionToken = sessionToken,
                phoneNumber = phoneNumber
            )
            
            val response = apiService.sendPhoneCode(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success") {
                    Result.success(true)
                } else {
                    val errorMessage = body.message 
                        ?: body.data?.message
                        ?: "Failed to send phone verification code"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify phone verification code
     * Returns success status
     */
    suspend fun verifyPhoneCode(phoneNumber: String, verificationCode: String): Result<Boolean> {
        return try {
            val customerId = SessionManager.getCustomerId()
            val sessionToken = SessionManager.getSessionToken()
            
            if (customerId == null || sessionToken == null) {
                return Result.failure(Exception("Session not initialized. Please start verification first."))
            }
            
            val request = com.ordershieldsdk.auth.data.model.VerifyPhoneCodeRequest(
                customerId = customerId,
                sessionToken = sessionToken,
                phoneNumber = phoneNumber,
                verificationCode = verificationCode
            )
            
            val response = apiService.verifyPhoneCode(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success") {
                    Result.success(true)
                } else {
                    val errorMessage = body.message 
                        ?: body.data?.message
                        ?: "Failed to verify phone code"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get terms and conditions checkboxes
     * Returns list of checkboxes sorted by displayOrder
     */
    suspend fun getTermsCheckboxes(): Result<List<com.ordershieldsdk.auth.data.model.TermsCheckbox>> {
        return try {
            val response = apiService.getTermsCheckboxes()
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success") {
                    // Sort by displayOrder
                    val sortedCheckboxes = body.data.sortedBy { it.displayOrder }
                    Result.success(sortedCheckboxes)
                } else {
                    Result.failure(Exception(body.message ?: "Failed to fetch terms checkboxes"))
                }
            } else {
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Submit terms acceptance
     * Returns success status
     */
    suspend fun submitTerms(acceptedCheckboxes: List<com.ordershieldsdk.auth.data.model.AcceptedCheckbox>): Result<Boolean> {
        return try {
            val customerId = SessionManager.getCustomerId()
            val sessionToken = SessionManager.getSessionToken()
            
            if (customerId == null || sessionToken == null) {
                return Result.failure(Exception("Session not initialized. Please start verification first."))
            }
            
            val request = com.ordershieldsdk.auth.data.model.SubmitTermsRequest(
                customerId = customerId,
                sessionToken = sessionToken,
                acceptedCheckboxes = acceptedCheckboxes
            )
            
            val response = apiService.submitTerms(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success") {
                    Result.success(true)
                } else {
                    val errorMessage = body.message 
                        ?: body.data?.verificationSession?.let { "Verification failed" }
                        ?: "Failed to submit terms"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload signature image
     * Returns success status
     */
    suspend fun uploadSignature(imageFile: File): Result<Boolean> {
        return try {
            val customerId = SessionManager.getCustomerId()
            val sessionToken = SessionManager.getSessionToken()
            
            if (customerId == null || sessionToken == null) {
                return Result.failure(Exception("Session not initialized. Please start verification first."))
            }
            
            // Determine image format from file extension
            val imageFormat = when {
                imageFile.name.endsWith(".jpg", ignoreCase = true) || 
                imageFile.name.endsWith(".jpeg", ignoreCase = true) -> "jpg"
                imageFile.name.endsWith(".png", ignoreCase = true) -> "png"
                else -> "png" // Default to png for signature
            }
            
            // Create multipart form data
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val signatureImagePart = MultipartBody.Part.createFormData("signature_image", imageFile.name, requestFile)
            val customerIdPart = customerId.toRequestBody("text/plain".toMediaTypeOrNull())
            val sessionTokenPart = sessionToken.toRequestBody("text/plain".toMediaTypeOrNull())
            val imageFormatPart = imageFormat.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = apiService.uploadSignature(
                signatureImage = signatureImagePart,
                customerId = customerIdPart,
                sessionToken = sessionTokenPart,
                imageFormat = imageFormatPart
            )
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.status == "success") {
                    Result.success(true)
                } else {
                    val errorMessage = body.message 
                        ?: body.data?.verificationSession?.let { "Verification failed" }
                        ?: "Failed to upload signature"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Try to parse error from error body
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    com.ordershieldsdk.auth.core.ErrorHandler.parseApiError(errorBody)
                } else {
                    response.body()?.message 
                        ?: response.message() 
                        ?: "Unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
