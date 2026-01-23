package com.ordershieldsdk.auth.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.ordershieldsdk.auth.R

class PhoneVerificationFragment : Fragment(R.layout.fragment_phone_verification) {

    private lateinit var etVerificationCode: TextInputEditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupTextWatcher()
        
        // Continue button removed
    }

    private fun initViews(view: View) {
        etVerificationCode = view.findViewById(R.id.etVerificationCode)
    }

    private fun setupTextWatcher() {
        etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val code = s?.toString()?.trim() ?: ""
                val isComplete = code.length == 6
                // Continue button removed
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun isCodeValid(): Boolean {
        val code = etVerificationCode.text?.toString()?.trim()

        if (TextUtils.isEmpty(code)) {
            return false
        }

        if (code!!.length != 6) {
            return false
        }

        // Check if all digits are numbers
        if (!code.matches(Regex("^[0-9]{6}$"))) {
            return false
        }

        return true
    }

    fun getVerificationCode(): String {
        return etVerificationCode.text?.toString()?.trim() ?: ""
    }
}

