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
     * Uses steps_remaining from verification/status API to determine which steps to show
     * New sequence: Phone -> Selfie -> User Info -> Email -> T&C
     * 
     * Uses iterative approach instead of recursion to prevent stack overflow and infinite loops
     */
    fun getNextStep(currentStep: Step): Step? {
        // Define the fixed step order (excluding INFO and COMPLETE which are special)
        val stepOrder = listOf(Step.PHONE, Step.SELFIE, Step.USER_INFO, Step.EMAIL, Step.TERMS_SIGNATURE)
        
        return when (currentStep) {
            Step.INFO -> {
                // After info, find first required step in order
                findNextRequiredStep(stepOrder, startIndex = -1)
            }
            
            Step.PHONE -> {
                // After phone, find next required step starting from SELFIE
                findNextRequiredStep(stepOrder, startIndex = 0) // 0 = SELFIE index
            }
            
            Step.SELFIE -> {
                // After selfie, find next required step starting from USER_INFO
                findNextRequiredStep(stepOrder, startIndex = 1) // 1 = USER_INFO index
            }
            
            Step.USER_INFO -> {
                // After user info, find next required step starting from EMAIL
                findNextRequiredStep(stepOrder, startIndex = 2) // 2 = EMAIL index
            }
            
            Step.EMAIL -> {
                // After email, check if terms/signature is required, otherwise COMPLETE
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
     * Helper method to find the next required step in the order
     * Iterates through steps starting from startIndex + 1
     * Returns first required step found, or COMPLETE if none found
     */
    private fun findNextRequiredStep(stepOrder: List<Step>, startIndex: Int): Step {
        // Iterate through steps starting after startIndex
        for (i in (startIndex + 1) until stepOrder.size) {
            val step = stepOrder[i]
            when (step) {
                Step.PHONE -> {
                    if (VerificationSettingsManager.isSmsStepRequired()) {
                        return Step.PHONE
                    }
                }
                Step.SELFIE -> {
                    if (VerificationSettingsManager.isSelfieStepRequired()) {
                        return Step.SELFIE
                    }
                }
                Step.USER_INFO -> {
                    if (VerificationSettingsManager.isUserInfoStepRequired()) {
                        return Step.USER_INFO
                    }
                }
                Step.EMAIL -> {
                    if (VerificationSettingsManager.isEmailStepRequired()) {
                        return Step.EMAIL
                    }
                }
                Step.TERMS_SIGNATURE -> {
                    if (VerificationSettingsManager.isTermsStepRequired() || 
                        VerificationSettingsManager.isSignatureStepRequired()) {
                        return Step.TERMS_SIGNATURE
                    }
                }
                else -> {
                    // Should not happen, but handle gracefully
                }
            }
        }
        // No required steps found, go to COMPLETE
        return Step.COMPLETE
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
