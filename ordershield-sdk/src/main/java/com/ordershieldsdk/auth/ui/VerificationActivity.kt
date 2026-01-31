package com.ordershieldsdk.auth.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ordershieldsdk.auth.R

class VerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_verification)
        
        // ENFORCE BLACK THEME - Set status bar to black
        enforceBlackTheme()
        
        // Show verification info fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, VerificationInfoFragment())
            .commit()
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
}
