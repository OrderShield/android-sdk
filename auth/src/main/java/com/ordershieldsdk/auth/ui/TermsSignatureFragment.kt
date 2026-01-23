package com.ordershieldsdk.auth.ui

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.android.material.button.MaterialButton
import com.ordershieldsdk.auth.R
import com.ordershieldsdk.auth.core.VerificationSettingsManager
import com.ordershieldsdk.auth.internal.StepNavigator

class TermsSignatureFragment : Fragment(R.layout.fragment_terms_signature) {

    private lateinit var checkbox1: CheckBox
    private lateinit var checkbox2: CheckBox
    private lateinit var checkbox3: CheckBox
    private lateinit var btnAcceptAndSign: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
    }

    private fun initViews(view: View) {
        checkbox1 = view.findViewById(R.id.checkbox1)
        checkbox2 = view.findViewById(R.id.checkbox2)
        checkbox3 = view.findViewById(R.id.checkbox3)
        btnAcceptAndSign = view.findViewById(R.id.btnAcceptAndSign)

        // Set button text based on signature setting
        val buttonText = StepNavigator.getTermsSignatureButtonText()
        btnAcceptAndSign.text = buttonText

        // Button starts disabled
        btnAcceptAndSign.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(R.color.gray_lighter, null)
        )
        btnAcceptAndSign.setTextColor(resources.getColor(R.color.gray_light, null))
    }

    private fun setupClickListeners() {
        val checkboxListener = { _: View -> checkValidation() }
        
        checkbox1.setOnCheckedChangeListener { _, _ -> checkValidation() }
        checkbox2.setOnCheckedChangeListener { _, _ -> checkValidation() }
        checkbox3.setOnCheckedChangeListener { _, _ -> checkValidation() }

        btnAcceptAndSign.setOnClickListener {
            if (areAllCheckboxesChecked()) {
                // Check if signature is enabled
                if (VerificationSettingsManager.isSignatureConfirmationEnabled()) {
                    showSignatureDialog()
                } else {
                    // No signature required, navigate directly to next step
                    navigateToNextStep()
                }
            }
        }
    }

    private fun checkValidation() {
        val allChecked = areAllCheckboxesChecked()
        btnAcceptAndSign.isEnabled = allChecked
        
        if (allChecked) {
            btnAcceptAndSign.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.BLACK)
            btnAcceptAndSign.setTextColor(Color.WHITE)
        } else {
            btnAcceptAndSign.backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.gray_lighter, null)
            )
            btnAcceptAndSign.setTextColor(resources.getColor(R.color.gray_light, null))
        }
    }

    private fun areAllCheckboxesChecked(): Boolean {
        return checkbox1.isChecked && checkbox2.isChecked && checkbox3.isChecked
    }

    private fun showSignatureDialog() {
        val dialog = SignatureDialogFragment()
        dialog.setSignatureListener(object : SignatureDialogFragment.SignatureListener {
            override fun onSignatureAccepted() {
                navigateToNextStep()
            }
        })
        dialog.show(parentFragmentManager, "SignatureDialog")
    }

    private fun navigateToNextStep() {
        val nextStep = StepNavigator.getNextStep(StepNavigator.Step.TERMS_SIGNATURE)
        nextStep?.let {
            val fragment = StepNavigator.createFragmentForStep(it)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }
}

class SignatureDialogFragment : DialogFragment() {

    interface SignatureListener {
        fun onSignatureAccepted()
    }

    private var signatureListener: SignatureListener? = null
    private lateinit var signaturePad: SignaturePad
    private lateinit var btnClear: MaterialButton
    private lateinit var btnAcceptSignature: MaterialButton
    private lateinit var tvCancel: TextView
    private lateinit var tvPlaceholder: TextView

    fun setSignatureListener(listener: SignatureListener) {
        signatureListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_signature_pad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signaturePad = view.findViewById(R.id.signaturePad)
        btnClear = view.findViewById(R.id.btnClear)
        btnAcceptSignature = view.findViewById(R.id.btnAcceptSignature)
        tvCancel = view.findViewById(R.id.tvCancel)
        tvPlaceholder = view.findViewById(R.id.tvPlaceholder)

        // Configure SignaturePad
        signaturePad.setPenColor(requireContext().getColor(R.color.black))
        signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                tvPlaceholder.visibility = View.GONE
            }

            override fun onSigned() {
                btnAcceptSignature.isEnabled = true
                btnAcceptSignature.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.BLACK)
                btnAcceptSignature.setTextColor(Color.WHITE)
            }

            override fun onClear() {
                tvPlaceholder.visibility = View.VISIBLE
                btnAcceptSignature.isEnabled = false
                btnAcceptSignature.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    resources.getColor(R.color.gray_lighter, null)
                )
                btnAcceptSignature.setTextColor(resources.getColor(R.color.gray_light, null))
            }
        })

        btnClear.setOnClickListener {
            signaturePad.clear()
        }

        btnAcceptSignature.setOnClickListener {
            if (!signaturePad.isEmpty) {
                signatureListener?.onSignatureAccepted()
                dismiss()
            }
        }

        tvCancel.setOnClickListener {
            dismiss()
        }

        // Button starts disabled
        btnAcceptSignature.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(R.color.gray_lighter, null)
        )
        btnAcceptSignature.setTextColor(resources.getColor(R.color.gray_light, null))
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
