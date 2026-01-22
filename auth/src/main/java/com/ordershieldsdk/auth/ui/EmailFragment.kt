package com.ordershieldsdk.auth.ui

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ordershieldsdk.auth.R
import java.util.regex.Pattern

class EmailFragment : Fragment(R.layout.fragment_email) {

    private lateinit var etEmail: TextInputEditText
    private lateinit var btnGetOtp: MaterialButton
    private lateinit var otpSection: LinearLayout
    private lateinit var etVerificationCode: TextInputEditText
    
    private var isOtpSent = false
    private var isOtpVerified = false

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
            if (!isOtpSent) {
                // First click: Send OTP
                if (isEmailValid()) {
                    sendOtp()
                }
            } else {
                // Second click: Verify OTP
                if (isOtpVerified) {
                    navigateToPhone()
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
    
    private fun navigateToPhone() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PhoneFragment())
            .commit()
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

