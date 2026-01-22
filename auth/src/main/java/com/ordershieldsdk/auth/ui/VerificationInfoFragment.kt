package com.ordershieldsdk.auth.ui

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.ordershieldsdk.auth.R

class VerificationInfoFragment : Fragment(R.layout.fragment_verification_info) {

    private lateinit var btnStartVerification: MaterialButton
    private var backPressedCallback: OnBackPressedCallback? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        handleEdgeToEdge(view)
        lockScreen()
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
        
        // Enforce black theme on button to prevent app module overrides
        enforceBlackThemeOnButton(btnStartVerification)
        
        // Underline the OrderShield text in footer
        val tvOrderShieldLink = view.findViewById<TextView>(R.id.tvOrderShieldLink)
        tvOrderShieldLink?.paintFlags = tvOrderShieldLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setupClickListeners() {
        btnStartVerification.setOnClickListener {
            // Navigate to CameraFragment
            navigateToCamera()
        }
    }
    
    private fun navigateToCamera() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, CameraFragment())
            .commit()
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

