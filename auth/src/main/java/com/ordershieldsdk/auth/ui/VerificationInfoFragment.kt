package com.ordershieldsdk.auth.ui

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.ordershieldsdk.auth.R
import com.ordershieldsdk.auth.core.DeviceInfoHelper
import com.ordershieldsdk.auth.core.ErrorHandler
import com.ordershieldsdk.auth.core.SessionManager
import com.ordershieldsdk.auth.data.repository.AuthRepository
import kotlinx.coroutines.launch

class VerificationInfoFragment : Fragment(R.layout.fragment_verification_info) {

    private lateinit var btnStartVerification: MaterialButton
    private lateinit var loadingOverlay: FrameLayout
    private var backPressedCallback: OnBackPressedCallback? = null
    private val repository = AuthRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        handleEdgeToEdge(view)
        lockScreen()
        
        // Call APIs when fragment is shown
        initializeVerification()
        
        setupClickListeners()
    }
    
    private fun initializeVerification() {
        // Show loader
        showLoader(true)
        
        // Disable button until APIs complete
        btnStartVerification.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // Step 1: Register device
                val deviceInfo = DeviceInfoHelper.getDeviceInfo(requireContext())
                val registerResult = repository.registerDevice(deviceInfo)
                
                registerResult.onSuccess { customerId ->
                    // Store customer ID
                    SessionManager.setCustomerId(customerId)
                    
                    // Step 2: Start verification
                    val startResult = repository.startVerification(customerId)
                    
                    startResult.onSuccess { (sessionId, sessionToken) ->
                        // Store session info
                        SessionManager.setSession(sessionId, sessionToken)
                        
                        // Hide loader and enable button
                        showLoader(false)
                        btnStartVerification.isEnabled = true
                    }.onFailure { exception ->
                        // Handle error
                        showLoader(false)
                        btnStartVerification.isEnabled = true
                        ErrorHandler.showError(requireContext(), exception)
                    }
                }.onFailure { exception ->
                    // Handle error
                    showLoader(false)
                    btnStartVerification.isEnabled = true
                    ErrorHandler.showError(requireContext(), exception)
                }
            } catch (e: Exception) {
                // Handle exception
                showLoader(false)
                btnStartVerification.isEnabled = true
                ErrorHandler.showError(requireContext(), e)
            }
        }
    }
    
    private fun showLoader(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun lockScreen() {
        // Disable back button navigation - lock the screen
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing - screen is locked
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback!!)
    }
    
    private fun handleEdgeToEdge(view: View) {
        // Handle system window insets for bottom content
        val bottomContent = view.findViewById<View>(R.id.bottomContent)
        bottomContent?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Add bottom padding only if there's a navigation bar (hardware buttons)
                // For gesture navigation, use minimal padding
                val bottomPadding = if (systemBars.bottom > 0) {
                    systemBars.bottom + 16 // Add extra padding for hardware buttons
                } else {
                    16 // Minimal padding for gesture navigation
                }
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding)
                insets
            }
        }
    }

    private fun initViews(view: View) {
        btnStartVerification = view.findViewById(R.id.btnStartVerification)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
        
        // Enforce black theme on button to prevent app module overrides
        enforceBlackThemeOnButton(btnStartVerification)
        
        // Underline the OrderShield text in footer
        val tvOrderShieldLink = view.findViewById<TextView>(R.id.tvOrderShieldLink)
        tvOrderShieldLink?.paintFlags = tvOrderShieldLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setupClickListeners() {
        btnStartVerification.setOnClickListener {
            // Navigate to next step based on settings
            navigateToNextStep()
        }
    }
    
    private fun navigateToNextStep() {
        val nextStep = com.ordershieldsdk.auth.internal.StepNavigator.getNextStep(
            com.ordershieldsdk.auth.internal.StepNavigator.Step.INFO
        )
        nextStep?.let {
            val fragment = com.ordershieldsdk.auth.internal.StepNavigator.createFragmentForStep(it)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }
    
    private fun enforceBlackThemeOnButton(button: MaterialButton) {
        val BLACK = Color.BLACK
        val WHITE = Color.WHITE
        button.backgroundTintList = android.content.res.ColorStateList.valueOf(BLACK)
        button.setTextColor(WHITE)
        button.iconTint = android.content.res.ColorStateList.valueOf(WHITE)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback?.remove()
        backPressedCallback = null
    }
}

