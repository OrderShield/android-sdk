package com.ordershieldsdk.auth.ui

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ordershieldsdk.auth.R
import com.ordershieldsdk.auth.core.CallbackManager
import com.ordershieldsdk.auth.core.ErrorHandler
import com.ordershieldsdk.auth.core.VerificationSettingsManager
import com.ordershieldsdk.auth.data.repository.AuthRepository
import com.ordershieldsdk.auth.internal.StepNavigator
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class EmailFragment : Fragment(R.layout.fragment_email) {

    private lateinit var etEmail: TextInputEditText
    private lateinit var btnGetOtp: MaterialButton
    private lateinit var otpSection: LinearLayout
    private lateinit var etVerificationCode: TextInputEditText
    
    private var isOtpSent = false
    private var isOtpVerified = false
    private var isVerifyingOtp = false
    private val repository = AuthRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupTextWatchers()
        setupClickListeners()
        handleEdgeToEdge(view)
    }
    
    private fun handleEdgeToEdge(view: View) {
        // Handle system window insets for footer
        val footerLayout = view.findViewById<View>(R.id.footerLayout)
        footerLayout?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val bottomPadding = if (systemBars.bottom > 0) {
                    systemBars.bottom + 16
                } else {
                    16
                }
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding)
                insets
            }
        }
        
        // Underline the OrderShield text in footer
        val tvOrderShieldLink = view.findViewById<TextView>(R.id.tvOrderShieldLink)
        tvOrderShieldLink?.paintFlags = tvOrderShieldLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun initViews(view: View) {
        etEmail = view.findViewById(R.id.etEmail)
        btnGetOtp = view.findViewById(R.id.btnGetOtp)
        otpSection = view.findViewById(R.id.otpSection)
        etVerificationCode = view.findViewById(R.id.etVerificationCode)
        
        // Check if OTP is required
        val isOtpRequired = VerificationSettingsManager.isEmailVerificationRequired()
        if (!isOtpRequired) {
            // Hide OTP section and change button text
            otpSection.visibility = View.GONE
            btnGetOtp.text = getString(R.string.continue_text)
        }
        
        // Button starts disabled
        btnGetOtp.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(R.color.gray_lighter, null)
        )
        btnGetOtp.setTextColor(resources.getColor(R.color.gray_light, null))
    }
    
    private fun setupTextWatchers() {
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s?.toString()?.trim() ?: ""
                val isValid = isValidEmail(email)
                btnGetOtp.isEnabled = isValid
                
                // Update button appearance based on enabled state (only if OTP not sent yet)
                if (!isOtpSent) {
                    updateButtonAppearance(isValid)
                }
                
                // Reset OTP state if email changes
                if (isOtpSent) {
                    isOtpSent = false
                    isOtpVerified = false
                    otpSection.visibility = View.GONE
                    btnGetOtp.text = getString(R.string.get_otp)
                    btnGetOtp.isEnabled = isValid
                    updateButtonAppearance(isValid)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val code = s?.toString()?.trim() ?: ""
                val isValid = code.length == 6
                
                // Only enable button if code is 6 digits and not already verifying
                if (isValid && !isVerifyingOtp) {
                    btnGetOtp.isEnabled = true
                    updateButtonAppearance(true)
                } else if (!isValid) {
                    btnGetOtp.isEnabled = false
                    updateButtonAppearance(false)
                    isOtpVerified = false
                }
                
                // Auto-verify when 6 digits are entered
                if (code.length == 6 && !isVerifyingOtp) {
                    verifyOtp(code)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun updateButtonAppearance(isEnabled: Boolean) {
        if (isEnabled) {
            btnGetOtp.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.BLACK)
            btnGetOtp.setTextColor(Color.WHITE)
        } else {
            btnGetOtp.backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.gray_lighter, null)
            )
            btnGetOtp.setTextColor(resources.getColor(R.color.gray_light, null))
        }
    }
    
    private fun setupClickListeners() {
        btnGetOtp.setOnClickListener {
            val isOtpRequired = VerificationSettingsManager.isEmailVerificationRequired()
            
            if (!isOtpRequired) {
                // OTP not required, just continue to next step
                if (isEmailValid()) {
                    // Notify callback that email step is completed
                    CallbackManager.notifyStepCompleted("email")
                    navigateToNextStep()
                }
            } else {
                // OTP is required
                if (!isOtpSent) {
                    // First click: Send OTP
                    if (isEmailValid()) {
                        sendOtp()
                    }
                } else {
                    // Second click: Verify OTP and navigate to next step
                    if (isOtpVerified) {
                        // Notify callback that email step is completed
                        CallbackManager.notifyStepCompleted("email")
                        navigateToNextStep()
                    }
                }
            }
        }
    }
    
    private fun sendOtp() {
        val email = getEmail()
        if (email.isEmpty() || !isEmailValid()) {
            return
        }
        
        // Disable button while sending
        btnGetOtp.isEnabled = false
        updateButtonAppearance(false)
        
        lifecycleScope.launch {
            try {
                val result = repository.sendEmailCode(email)
                
                result.onSuccess { success ->
                    if (success) {
                        // OTP sent successfully
                        isOtpSent = true
                        otpSection.visibility = View.VISIBLE
                        btnGetOtp.text = "Verify"
                        btnGetOtp.isEnabled = false
                        updateButtonAppearance(false)
                        etVerificationCode.requestFocus()
                    } else {
                        // Failed to send OTP
                        btnGetOtp.isEnabled = true
                        updateButtonAppearance(true)
                        ErrorHandler.showError(requireContext(), "Failed to send verification code. Please try again.")
                    }
                }.onFailure { exception ->
                    // Handle error
                    btnGetOtp.isEnabled = true
                    updateButtonAppearance(true)
                    ErrorHandler.showError(requireContext(), exception)
                }
            } catch (e: Exception) {
                btnGetOtp.isEnabled = true
                updateButtonAppearance(true)
                ErrorHandler.showError(requireContext(), e)
            }
        }
    }
    
    private fun verifyOtp(code: String) {
        if (code.length != 6 || isVerifyingOtp) {
            return
        }
        
        isVerifyingOtp = true
        btnGetOtp.isEnabled = false
        updateButtonAppearance(false)
        
        val email = getEmail()
        if (email.isEmpty()) {
            isVerifyingOtp = false
            btnGetOtp.isEnabled = true
            updateButtonAppearance(true)
            return
        }
        
        // Hide keyboard
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(etVerificationCode.windowToken, 0)
        
        lifecycleScope.launch {
            try {
                val result = repository.verifyEmailCode(email, code)
                
                result.onSuccess { success ->
                    isVerifyingOtp = false
                    if (success) {
                        // OTP verified successfully
                        isOtpVerified = true
                        btnGetOtp.isEnabled = true
                        updateButtonAppearance(true)
                    } else {
                        // Verification failed
                        btnGetOtp.isEnabled = true
                        updateButtonAppearance(true)
                        ErrorHandler.showError(requireContext(), "Invalid verification code. Please try again.")
                        // Clear the OTP field
                        etVerificationCode.setText("")
                    }
                }.onFailure { exception ->
                    // Handle error
                    isVerifyingOtp = false
                    btnGetOtp.isEnabled = true
                    updateButtonAppearance(true)
                    ErrorHandler.showError(requireContext(), exception)
                    // Clear the OTP field on error
                    etVerificationCode.setText("")
                }
            } catch (e: Exception) {
                isVerifyingOtp = false
                btnGetOtp.isEnabled = true
                updateButtonAppearance(true)
                ErrorHandler.showError(requireContext(), e)
                etVerificationCode.setText("")
            }
        }
    }
    
    private fun navigateToNextStep() {
        // Navigate to next step after email (and OTP if required)
        val nextStep = StepNavigator.getNextStep(StepNavigator.Step.EMAIL)
        if (nextStep != null) {
            val fragment = StepNavigator.createFragmentForStep(nextStep)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        } else {
            // Unexpected null - fallback to COMPLETE screen to prevent user being stuck
            Log.w("EmailFragment", "getNextStep returned null, navigating to COMPLETE as fallback")
            val fragment = StepNavigator.createFragmentForStep(StepNavigator.Step.COMPLETE)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }

    fun isEmailValid(): Boolean {
        val email = etEmail.text?.toString()?.trim()

        if (TextUtils.isEmpty(email)) {
            return false
        }

        if (!isValidEmail(email!!)) {
            return false
        }

        return true
    }
    
    fun isOtpSent(): Boolean {
        return isOtpSent
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return emailPattern.matcher(email).matches()
    }
    
    fun getEmail(): String {
        return etEmail.text?.toString()?.trim() ?: ""
    }
    
    private fun enforceBlackThemeOnButton(button: MaterialButton) {
        val BLACK = android.graphics.Color.BLACK
        val WHITE = android.graphics.Color.WHITE
        button.backgroundTintList = android.content.res.ColorStateList.valueOf(BLACK)
        button.setTextColor(WHITE)
        button.iconTint = android.content.res.ColorStateList.valueOf(WHITE)
    }
}

