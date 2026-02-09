package com.ordershieldsdk

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ordershieldsdk.auth.core.AuthSDK

class MainActivity : AppCompatActivity() {
    
    private lateinit var etApiKey: TextInputEditText
    private lateinit var btnInitialize: MaterialButton
    private lateinit var btnStartVerification: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        etApiKey = findViewById(R.id.etApiKey)
        btnInitialize = findViewById(R.id.btnInitialize)
        btnStartVerification = findViewById(R.id.btnStartVerification)
        
        btnInitialize.setOnClickListener {
            // Initialize SDK
            AuthSDK.init(
                context = this,
                apiKey = etApiKey.text?.toString() ?: "",
                enableLogging = true
            )
        }
        
        btnStartVerification.setOnClickListener {
            // Start verification flow
            AuthSDK.startVerification(
                activity = this,
                onError = { error ->
                    // Handle error
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                },
                onStepCompleted = { step ->
                    // Handle step completion
                    Toast.makeText(this@MainActivity, step, Toast.LENGTH_LONG).show()
                },
                onVerificationCompleted = {
                    // Navigate to next screen (e.g., payment screen)
                    Toast.makeText(this@MainActivity, "Completed", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}