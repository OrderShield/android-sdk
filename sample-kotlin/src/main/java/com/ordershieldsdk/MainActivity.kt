package com.ordershieldsdk

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ordershieldsdk.auth.core.AuthSDK
import com.ordershieldsdk.auth.core.VerificationCallback

class MainActivity : AppCompatActivity() {
    
    private lateinit var etApiKey: TextInputEditText
    private lateinit var btnInitialize: MaterialButton
    private lateinit var btnStartVerification: MaterialButton
    private var isInitialized = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        etApiKey = findViewById(R.id.etApiKey)
        btnInitialize = findViewById(R.id.btnInitialize)
        btnStartVerification = findViewById(R.id.btnStartVerification)
    }
    
    private fun setupClickListeners() {
        // Initialize SDK button
        btnInitialize.setOnClickListener {
            val apiKey = etApiKey.text?.toString()?.trim()
            
            if (apiKey.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter an API key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            try {
                // Initialize SDK with callbacks
                AuthSDK.init(
                    context = this,
                    apiKey = apiKey,
                    enableLogging = true,
                    callback = object : VerificationCallback {
                        override fun onStepCompleted(step: String) {
                            // Called when each verification step completes
                            Toast.makeText(this@MainActivity, "Step completed: $step", Toast.LENGTH_SHORT).show()
                        }
                        
                        override fun onVerificationCompleted() {
                            // Called when all verification steps are completed
                            Toast.makeText(this@MainActivity, "Verification completed!", Toast.LENGTH_LONG).show()
                        }
                        
                        override fun onVerificationFailed(error: String) {
                            // Called when verification fails
                            Toast.makeText(this@MainActivity, "Error: $error", Toast.LENGTH_LONG).show()
                        }
                    }
                )
                
                isInitialized = true
                btnStartVerification.isEnabled = true
                btnInitialize.isEnabled = false
                etApiKey.isEnabled = false
                Toast.makeText(this, "SDK initialized successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to initialize SDK: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        // Start verification button
        btnStartVerification.setOnClickListener {
            if (!isInitialized) {
                Toast.makeText(this, "Please initialize SDK first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Start verification flow
            AuthSDK.startVerification(this) { success ->
                if (success) {
                    Toast.makeText(this@MainActivity, "Verification successful1!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Verification failed1", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}