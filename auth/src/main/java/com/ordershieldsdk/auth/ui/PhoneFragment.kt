package com.ordershieldsdk.auth.ui

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ordershieldsdk.auth.R
import com.ordershieldsdk.auth.core.VerificationSettingsManager
import com.ordershieldsdk.auth.internal.StepNavigator
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

    private val countryCodes = listOf(
        "+1", "+44", "+91", "+86", "+81", "+49", "+33", "+7", "+39", "+34",
        "+61", "+55", "+82", "+31", "+46", "+41", "+32", "+27", "+52", "+47"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        spinnerCountryCode.setSelection(0) // Default to +1
        
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
                btnGetOtp.isEnabled = isValid
                updateButtonAppearance(isValid)
                
                if (code.length == 6) {
                    verifyOtp(code)
                } else {
                    isOtpVerified = false
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
                        navigateToNextStep()
                    }
                }
            }
        }
    }
    
    private fun sendOtp() {
        // TODO: Call API to send OTP
        isOtpSent = true
        otpSection.visibility = View.VISIBLE
        btnGetOtp.text = "Verify"
        btnGetOtp.isEnabled = false
        updateButtonAppearance(false)
        etVerificationCode.requestFocus()
    }
    
    private fun verifyOtp(code: String) {
        // TODO: Call API to verify OTP
        if (code.length == 6) {
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(etVerificationCode.windowToken, 0)
            
            isOtpVerified = true
            btnGetOtp.isEnabled = true
            updateButtonAppearance(true)
        }
    }
    
    private fun navigateToNextStep() {
        // Navigate to next step after phone (and OTP if required)
        val nextStep = StepNavigator.getNextStep(StepNavigator.Step.PHONE)
        nextStep?.let {
            val fragment = StepNavigator.createFragmentForStep(it)
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

