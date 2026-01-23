package com.ordershieldsdk

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.ordershieldsdk.core.AuthSDK

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
            apiKey = "prod_TfHaD4kYlIwdUGpBsflsMwpozo3NazRgRNsqspwnws4",
            enableLogging = true // Set to false in production
        )
        
        // Setup button click listener
        val btnStartVerification = findViewById<MaterialButton>(R.id.btnStartVerification)
        btnStartVerification.setOnClickListener {
            // Start verification flow when button is clicked
            AuthSDK.startVerification(this) { success ->
                // Handle result if needed
            }
        }
    }
}