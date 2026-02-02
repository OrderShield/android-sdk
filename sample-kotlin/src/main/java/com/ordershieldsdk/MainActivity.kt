package com.ordershieldsdk

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.ordershieldsdk.auth.core.AuthSDK

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Initialize SDK (for testing - in production, initialize in Application class)
        // TODO: Replace with your actual API key
        // Base URL is fixed in auth module
        AuthSDK.init(
            context = this,
            apiKey = "prod_SHy1Bf5dPNrIikD1FDayUwUhk3yGC0kQ9OOHAs4XnJQ",
            enableLogging = true // Set to false in production
        )
        
        // Setup button click listener
        val btnStartVerification = findViewById<MaterialButton>(R.id.btnStartVerification)
        btnStartVerification.setOnClickListener {
            // Start verification flow when button is clicked
            AuthSDK.startVerification(this) { success ->
                print("++++++++++++++++++++++++++++++++++++++++")
                print("Verification success: $success")
                print("++++++++++++++++++++++++++++++++++++++++")
            }
        }
    }
}