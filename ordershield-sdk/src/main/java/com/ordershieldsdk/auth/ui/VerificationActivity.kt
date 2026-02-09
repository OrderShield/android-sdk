package com.ordershieldsdk.auth.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ordershieldsdk.auth.R
import com.ordershieldsdk.auth.core.CallbackManager
import com.ordershieldsdk.auth.internal.StepNavigator

class VerificationActivity : AppCompatActivity() {

    private var backPressedCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_verification)
        
        // ENFORCE BLACK THEME - Set status bar to black
        enforceBlackTheme()
        
        // Setup back button handler
        setupBackButtonHandler()
        
        // Show verification info fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, VerificationInfoFragment())
            .commit()
    }

    /**
     * Setup back button handler to call callback when on completion screen
     */
    private fun setupBackButtonHandler() {
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if we're on completion screen
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (currentFragment is VerificationCompleteFragment) {
                    // Call callback before finishing - allows app to navigate (e.g., to payment screen)
                    CallbackManager.notifyVerificationCompleted()
                    finish()
                } else {
                    // For other screens, allow normal back navigation
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback!!)
    }

    /**
     * Enforce black theme programmatically to prevent app module resource overrides
     */
    private fun enforceBlackTheme() {
        // Set status bar color to black and make it visible
        window.statusBarColor = Color.BLACK
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        var flags = window.decorView.systemUiVisibility
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        window.decorView.systemUiVisibility = flags

        window.navigationBarColor = Color.WHITE
    }

    override fun onDestroy() {
        super.onDestroy()
        backPressedCallback?.remove()
        backPressedCallback = null
        // Clear callbacks when activity is destroyed
        CallbackManager.clear()
    }
}
