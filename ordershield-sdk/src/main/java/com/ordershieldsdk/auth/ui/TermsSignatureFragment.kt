package com.ordershieldsdk.auth.ui

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.android.material.button.MaterialButton
import com.ordershieldsdk.auth.R
import com.ordershieldsdk.auth.core.CallbackManager
import com.ordershieldsdk.auth.core.ErrorHandler
import com.ordershieldsdk.auth.core.VerificationSettingsManager
import com.ordershieldsdk.auth.data.model.AcceptedCheckbox
import com.ordershieldsdk.auth.data.model.TermsCheckbox
import com.ordershieldsdk.auth.data.repository.AuthRepository
import com.ordershieldsdk.auth.internal.StepNavigator
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class TermsSignatureFragment : Fragment(R.layout.fragment_terms_signature) {

    private lateinit var checkboxesContainer: LinearLayout
    private lateinit var btnAcceptAndSign: MaterialButton
    private lateinit var loadingOverlay: FrameLayout
    
    private val repository = AuthRepository()
    private val checkboxes = mutableListOf<CheckBox>()
    private val checkboxData = mutableListOf<TermsCheckbox>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        fetchTermsCheckboxes()
    }

    private fun initViews(view: View) {
        checkboxesContainer = view.findViewById(R.id.checkboxesContainer)
        btnAcceptAndSign = view.findViewById(R.id.btnAcceptAndSign)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)

        // Set button text based on signature setting
        val buttonText = StepNavigator.getTermsSignatureButtonText()
        btnAcceptAndSign.text = buttonText

        // Button starts disabled
        btnAcceptAndSign.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(R.color.gray_lighter, null)
        )
        btnAcceptAndSign.setTextColor(resources.getColor(R.color.gray_light, null))
    }
    
    private fun fetchTermsCheckboxes() {
        // Show loader
        showLoader(true)
        
        lifecycleScope.launch {
            try {
                val result = repository.getTermsCheckboxes()
                
                result.onSuccess { checkboxesList ->
                    showLoader(false)
                    checkboxData.clear()
                    checkboxData.addAll(checkboxesList)
                    createCheckboxes()
                    setupClickListeners()
                }.onFailure { exception ->
                    showLoader(false)
                    ErrorHandler.showError(requireContext(), exception)
                    // On error, still allow user to proceed (fallback)
                    setupClickListeners()
                }
            } catch (e: Exception) {
                showLoader(false)
                ErrorHandler.showError(requireContext(), e)
                setupClickListeners()
            }
        }
    }
    
    private fun createCheckboxes() {
        checkboxesContainer.removeAllViews()
        checkboxes.clear()
        
        checkboxData.forEach { checkboxItem ->
            val checkboxLayout = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_terms_checkbox, checkboxesContainer, false) as LinearLayout
            
            val checkbox = checkboxLayout.findViewById<CheckBox>(R.id.checkbox)
            val textView = checkboxLayout.findViewById<TextView>(R.id.checkboxText)
            
            // Set checkbox text
            textView.text = checkboxItem.checkboxText
            
            // Store checkbox and data
            checkbox.tag = checkboxItem.id
            checkboxes.add(checkbox)
            
            // Add to container
            checkboxesContainer.addView(checkboxLayout)
        }
    }

    private fun setupClickListeners() {
        // Set up listeners for all checkboxes
        checkboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, _ -> checkValidation() }
        }

        btnAcceptAndSign.setOnClickListener {
            if (areAllRequiredCheckboxesChecked()) {
                // Check if signature is in required steps
                if (VerificationSettingsManager.isSignatureStepRequired()) {
                    showSignatureDialog()
                } else {
                    // No signature required, submit terms only
                    submitTerms()
                }
            }
        }
    }

    private fun checkValidation() {
        val allRequiredChecked = areAllRequiredCheckboxesChecked()
        btnAcceptAndSign.isEnabled = allRequiredChecked
        
        if (allRequiredChecked) {
            btnAcceptAndSign.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.BLACK)
            btnAcceptAndSign.setTextColor(Color.WHITE)
        } else {
            btnAcceptAndSign.backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.gray_lighter, null)
            )
            btnAcceptAndSign.setTextColor(resources.getColor(R.color.gray_light, null))
        }
    }

    private fun areAllRequiredCheckboxesChecked(): Boolean {
        if (checkboxData.isEmpty()) {
            // If no checkboxes loaded, allow proceeding (fallback)
            return true
        }
        
        // All checkboxes must be checked
        return checkboxes.all { it.isChecked }
    }
    
    private fun showLoader(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun submitTerms() {
        // Show loader
        showLoader(true)
        
        // Prepare accepted checkboxes
        val acceptedCheckboxes = checkboxData.map { checkboxItem ->
            val checkbox = checkboxes.find { it.tag == checkboxItem.id }
            AcceptedCheckbox(
                checkboxId = checkboxItem.id,
                accepted = checkbox?.isChecked ?: false
            )
        }
        
        lifecycleScope.launch {
            try {
                val result = repository.submitTerms(acceptedCheckboxes)
                
                result.onSuccess { success ->
                    if (success) {
                        // Notify callback that terms step is completed
                        CallbackManager.notifyStepCompleted("terms")
                        
                        // Terms submitted successfully, navigate to next step
                        showLoader(false)
                        navigateToNextStep()
                    } else {
                        showLoader(false)
                        ErrorHandler.showError(requireContext(), "Failed to submit terms. Please try again.")
                    }
                }.onFailure { exception ->
                    showLoader(false)
                    ErrorHandler.showError(requireContext(), exception)
                }
            } catch (e: Exception) {
                showLoader(false)
                ErrorHandler.showError(requireContext(), e)
            }
        }
    }
    
    private fun submitTermsAndSignature(signatureBitmap: Bitmap) {
        // Show loader
        showLoader(true)
        
        // Prepare accepted checkboxes
        val acceptedCheckboxes = checkboxData.map { checkboxItem ->
            val checkbox = checkboxes.find { it.tag == checkboxItem.id }
            AcceptedCheckbox(
                checkboxId = checkboxItem.id,
                accepted = checkbox?.isChecked ?: false
            )
        }
        
        lifecycleScope.launch {
            try {
                // Step 1: Submit terms
                val termsResult = repository.submitTerms(acceptedCheckboxes)
                
                termsResult.onSuccess { success ->
                    if (success) {
                        // Notify callback that terms step is completed
                        CallbackManager.notifyStepCompleted("terms")
                        
                        // Step 2: Upload signature
                        val signatureFile = saveSignatureToFile(signatureBitmap)
                        if (signatureFile != null) {
                            val signatureResult = repository.uploadSignature(signatureFile)
                            
                            signatureResult.onSuccess { sigSuccess ->
                                if (sigSuccess) {
                                    // Notify callback that signature step is completed
                                    CallbackManager.notifyStepCompleted("signature")
                                    
                                    // Both APIs successful, navigate to next step
                                    showLoader(false)
                                    navigateToNextStep()
                                } else {
                                    showLoader(false)
                                    ErrorHandler.showError(requireContext(), "Failed to upload signature. Please try again.")
                                }
                            }.onFailure { exception ->
                                showLoader(false)
                                ErrorHandler.showError(requireContext(), exception)
                            }
                        } else {
                            showLoader(false)
                            ErrorHandler.showError(requireContext(), "Failed to save signature image.")
                        }
                    } else {
                        showLoader(false)
                        ErrorHandler.showError(requireContext(), "Failed to submit terms. Please try again.")
                    }
                }.onFailure { exception ->
                    showLoader(false)
                    ErrorHandler.showError(requireContext(), exception)
                }
            } catch (e: Exception) {
                showLoader(false)
                ErrorHandler.showError(requireContext(), e)
            }
        }
    }
    
    private fun saveSignatureToFile(bitmap: Bitmap): File? {
        return try {
            val file = File(requireContext().cacheDir, "signature_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }
    
    private fun showSignatureDialog() {
        val dialog = SignatureDialogFragment()
        dialog.setSignatureListener(object : SignatureDialogFragment.SignatureListener {
            override fun onSignatureAccepted(signatureBitmap: Bitmap) {
                // Submit terms and signature
                submitTermsAndSignature(signatureBitmap)
            }
        })
        dialog.show(parentFragmentManager, "SignatureDialog")
    }

    private fun navigateToNextStep() {
        val nextStep = StepNavigator.getNextStep(StepNavigator.Step.TERMS_SIGNATURE)
        if (nextStep != null) {
            val fragment = StepNavigator.createFragmentForStep(nextStep)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        } else {
            // TERMS_SIGNATURE should always return COMPLETE, but handle null gracefully
            Log.w("TermsSignatureFragment", "getNextStep returned null, navigating to COMPLETE as fallback")
            val fragment = StepNavigator.createFragmentForStep(StepNavigator.Step.COMPLETE)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }
}

class SignatureDialogFragment : DialogFragment() {

    interface SignatureListener {
        fun onSignatureAccepted(signatureBitmap: Bitmap)
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
                val signatureBitmap = signaturePad.signatureBitmap
                signatureListener?.onSignatureAccepted(signatureBitmap)
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
