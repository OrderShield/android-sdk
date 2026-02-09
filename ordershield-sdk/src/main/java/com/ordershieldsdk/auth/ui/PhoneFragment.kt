package com.ordershieldsdk.auth.ui

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ordershieldsdk.auth.R
import com.ordershieldsdk.auth.core.CountryCodeHelper
import com.ordershieldsdk.auth.core.CallbackManager
import com.ordershieldsdk.auth.core.ErrorHandler
import com.ordershieldsdk.auth.core.EventTracker
import com.ordershieldsdk.auth.core.VerificationSettingsManager
import com.ordershieldsdk.auth.data.repository.AuthRepository
import com.ordershieldsdk.auth.internal.StepNavigator
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class PhoneFragment : Fragment(R.layout.fragment_phone) {

    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var spinnerCountryCode: Spinner
    private lateinit var btnGetOtp: MaterialButton
    private lateinit var otpSection: LinearLayout
    private lateinit var etVerificationCode: TextInputEditText
    private var selectedCountryCode = "+1"
    private var isOtpSent = false
    private var isOtpVerified = false
    private var isVerifyingOtp = false
    private val repository = AuthRepository()

    private val countryCodes = listOf(
        "+1", "+44", "+91", "+86", "+81", "+49", "+33", "+7", "+39", "+34",
        "+61", "+55", "+82", "+31", "+46", "+41", "+32", "+27", "+52", "+47"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Track step start
        EventTracker.trackStepStart(StepNavigator.Step.PHONE)

        initViews(view)
        setupCountryCodeSpinner()
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
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber)
        spinnerCountryCode = view.findViewById(R.id.spinnerCountryCode)
        btnGetOtp = view.findViewById(R.id.btnGetOtp)
        otpSection = view.findViewById(R.id.otpSection)
        etVerificationCode = view.findViewById(R.id.etVerificationCode)
        
        // Check if OTP is required
        val isOtpRequired = VerificationSettingsManager.isSmsVerificationRequired()
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

    private fun setupCountryCodeSpinner() {
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            countryCodes
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = layoutInflater.inflate(R.layout.spinner_country_code, parent, false)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = countryCodes[position]
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.text = countryCodes[position]
                view.textSize = 16f
                view.setTextColor(resources.getColor(R.color.input_text, null))
                view.setPadding(48, view.paddingTop, view.paddingEnd, view.paddingBottom)
                return view
            }
        }
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCountryCode.adapter = adapter
        
        // Auto-select country code based on user's location
        val detectedPhoneCode = CountryCodeHelper.detectPhoneCountryCode(requireContext())
        val defaultSelectionIndex = if (detectedPhoneCode != null) {
            // Find the index of detected country code in the list
            countryCodes.indexOf(detectedPhoneCode).takeIf { it >= 0 } ?: 0
        } else {
            // Default to +1 if detection fails
            0
        }
        
        spinnerCountryCode.setSelection(defaultSelectionIndex)
        selectedCountryCode = countryCodes[defaultSelectionIndex]
        
        spinnerCountryCode.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCountryCode = countryCodes[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupTextWatchers() {
        etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val phone = s?.toString()?.trim() ?: ""
                val digitsOnly = phone.replace(Regex("[^0-9]"), "")
                val isValid = digitsOnly.length >= 10
                
                if (!isOtpSent) {
                    btnGetOtp.isEnabled = isValid
                    updateButtonAppearance(isValid)
                }
                
                // Reset OTP state if phone changes
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
            val isOtpRequired = VerificationSettingsManager.isSmsVerificationRequired()
            
            if (!isOtpRequired) {
                // OTP not required, just continue to next step
                if (isPhoneValid()) {
                    // Track step end
                    EventTracker.trackStepEnd(StepNavigator.Step.PHONE)
                    // Notify callback that SMS step is completed
                    CallbackManager.notifyStepCompleted("sms")
                    navigateToNextStep()
                }
            } else {
                // OTP is required
                if (!isOtpSent) {
                    // First click: Send OTP
                    if (isPhoneValid()) {
                        sendOtp()
                    }
                } else {
                    // Second click: Verify OTP and navigate to next step
                    if (isOtpVerified) {
                        // Track step end
                        EventTracker.trackStepEnd(StepNavigator.Step.PHONE)
                        // Notify callback that SMS step is completed
                        CallbackManager.notifyStepCompleted("sms")
                        navigateToNextStep()
                    }
                }
            }
        }
    }
    
    private fun sendOtp() {
        val phoneNumber = getPhoneNumber()
        if (phoneNumber.isEmpty() || !isPhoneValid()) {
            return
        }
        
        // Disable button while sending
        btnGetOtp.isEnabled = false
        updateButtonAppearance(false)
        
        lifecycleScope.launch {
            try {
                val result = repository.sendPhoneCode(phoneNumber)
                
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
        
        val phoneNumber = getPhoneNumber()
        if (phoneNumber.isEmpty()) {
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
                val result = repository.verifyPhoneCode(phoneNumber, code)
                
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
        // Navigate to next step after phone (and OTP if required)
        val nextStep = StepNavigator.getNextStep(StepNavigator.Step.PHONE)
        if (nextStep != null) {
            val fragment = StepNavigator.createFragmentForStep(nextStep)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        } else {
            // Unexpected null - fallback to COMPLETE screen to prevent user being stuck
            Log.w("PhoneFragment", "getNextStep returned null, navigating to COMPLETE as fallback")
            val fragment = StepNavigator.createFragmentForStep(StepNavigator.Step.COMPLETE)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }

    fun isPhoneValid(): Boolean {
        val phone = etPhoneNumber.text?.toString()?.trim()

        if (TextUtils.isEmpty(phone)) {
            return false
        }

        // Remove any non-digit characters for validation
        val digitsOnly = phone!!.replace(Regex("[^0-9]"), "")

        if (digitsOnly.length < 10) {
            return false
        }

        return true
    }
    
    fun isOtpVerified(): Boolean {
        return isOtpVerified
    }

    fun getPhoneNumber(): String {
        val phone = etPhoneNumber.text?.toString()?.trim() ?: ""
        // Return with selected country code
        return "$selectedCountryCode$phone"
    }
    
    private fun enforceBlackThemeOnButton(button: MaterialButton) {
        val BLACK = android.graphics.Color.BLACK
        val WHITE = android.graphics.Color.WHITE
        button.backgroundTintList = android.content.res.ColorStateList.valueOf(BLACK)
        button.setTextColor(WHITE)
        button.iconTint = android.content.res.ColorStateList.valueOf(WHITE)
    }
}

