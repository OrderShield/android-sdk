package com.ordershieldsdk.auth.internal

import androidx.fragment.app.Fragment
import com.ordershieldsdk.auth.core.VerificationSettingsManager
import com.ordershieldsdk.auth.ui.*

/**
 * Navigator to handle verification flow based on settings
 * Determines which screens to show and in what order
 */
object StepNavigator {
    
    /**
     * Enum representing all possible steps in the verification flow
     * Order is fixed as per requirements
     */
    enum class Step {
        INFO,              // VerificationInfoFragment - Always shown first
        SELFIE,            // CameraFragment
        USER_INFO,         // UserInformationFragment
        EMAIL,             // EmailFragment (with inline OTP if required)
        PHONE,             // PhoneFragment (with inline OTP if required)
        TERMS_SIGNATURE,   // TermsSignatureFragment
        COMPLETE           // VerificationCompleteFragment - Always shown last
    }
    
    /**
     * Get the first step in the flow
     */
    fun getFirstStep(): Step {
        return Step.INFO
    }
    
    /**
     * Get the next step based on current step and settings
     * Uses steps_required array to determine which steps to show
     */
    fun getNextStep(currentStep: Step): Step? {
        return when (currentStep) {
            Step.INFO -> {
                // After info, check if selfie is in required steps
                if (VerificationSettingsManager.isSelfieStepRequired()) {
                    Step.SELFIE
                } else {
                    getNextStep(Step.SELFIE) // Skip selfie, get next
                }
            }
            
            Step.SELFIE -> {
                // After selfie, check if userInfo is in required steps
                if (VerificationSettingsManager.isUserInfoStepRequired()) {
                    Step.USER_INFO
                } else {
                    getNextStep(Step.USER_INFO) // Skip user info, get next
                }
            }
            
            Step.USER_INFO -> {
                // After user info, check if email is in required steps
                if (VerificationSettingsManager.isEmailStepRequired()) {
                    Step.EMAIL
                } else {
                    getNextStep(Step.EMAIL) // Skip email, get next
                }
            }
            
            Step.EMAIL -> {
                // After email (and OTP if required, handled inline), check if SMS is in required steps
                if (VerificationSettingsManager.isSmsStepRequired()) {
                    Step.PHONE
                } else {
                    getNextStep(Step.PHONE) // Skip phone, get next
                }
            }
            
            Step.PHONE -> {
                // After phone (and OTP if required, handled inline), check if terms or signature is in required steps
                if (VerificationSettingsManager.isTermsStepRequired() || 
                    VerificationSettingsManager.isSignatureStepRequired()) {
                    Step.TERMS_SIGNATURE
                } else {
                    Step.COMPLETE
                }
            }
            
            Step.TERMS_SIGNATURE -> {
                // After terms/signature, always go to complete
                Step.COMPLETE
            }
            
            Step.COMPLETE -> {
                // No next step after complete
                null
            }
        }
    }
    
    /**
     * Create fragment instance for a given step
     */
    fun createFragmentForStep(step: Step): Fragment {
        return when (step) {
            Step.INFO -> VerificationInfoFragment()
            Step.SELFIE -> CameraFragment()
            Step.USER_INFO -> UserInformationFragment()
            Step.EMAIL -> EmailFragment()
            Step.PHONE -> PhoneFragment()
            Step.TERMS_SIGNATURE -> TermsSignatureFragment()
            Step.COMPLETE -> VerificationCompleteFragment()
        }
    }
    
    /**
     * Check if a step should be shown
     * Uses steps_required array to determine visibility
     */
    fun shouldShowStep(step: Step): Boolean {
        return when (step) {
            Step.INFO -> true // Always show
            Step.SELFIE -> VerificationSettingsManager.isSelfieStepRequired()
            Step.USER_INFO -> VerificationSettingsManager.isUserInfoStepRequired()
            Step.EMAIL -> VerificationSettingsManager.isEmailStepRequired()
            Step.PHONE -> VerificationSettingsManager.isSmsStepRequired()
            Step.TERMS_SIGNATURE -> VerificationSettingsManager.isTermsStepRequired() || 
                                   VerificationSettingsManager.isSignatureStepRequired()
            Step.COMPLETE -> true // Always show at the end
        }
    }
    
    /**
     * Get button text for Terms/Signature screen
     * Depends on whether signature is in required steps
     */
    fun getTermsSignatureButtonText(): String {
        return if (VerificationSettingsManager.isSignatureStepRequired()) {
            "Accept and Sign"
        } else {
            "Accept and Continue"
        }
    }
}
