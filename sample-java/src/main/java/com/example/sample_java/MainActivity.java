package com.example.sample_java;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.ordershieldsdk.auth.core.AuthSDK;
import com.ordershieldsdk.auth.core.VerificationCallback;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etApiKey;
    private MaterialButton btnInitialize;
    private MaterialButton btnStartVerification;
    private boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etApiKey = findViewById(R.id.etApiKey);
        btnInitialize = findViewById(R.id.btnInitialize);
        btnStartVerification = findViewById(R.id.btnStartVerification);
    }

    private void setupClickListeners() {
        // Initialize SDK button
        btnInitialize.setOnClickListener(v -> {
            String apiKey = etApiKey.getText() != null ? etApiKey.getText().toString().trim() : "";

            if (TextUtils.isEmpty(apiKey)) {
                Toast.makeText(this, "Please enter an API key", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Initialize SDK with callbacks
                AuthSDK.INSTANCE.init(
                        this,
                        apiKey,
                        true, // enableLogging
                        new VerificationCallback() {
                            @Override
                            public void onStepCompleted(String step) {
                                // Called when each verification step completes
                                runOnUiThread(() -> Toast.makeText(
                                        MainActivity.this,
                                        "Step completed: " + step,
                                        Toast.LENGTH_SHORT
                                ).show());
                            }

                            @Override
                            public void onVerificationCompleted() {
                                // Called when all verification steps are completed
                                runOnUiThread(() -> Toast.makeText(
                                        MainActivity.this,
                                        "Verification completed!",
                                        Toast.LENGTH_LONG
                                ).show());
                            }

                            @Override
                            public void onVerificationFailed(String error) {
                                // Called when verification fails
                                runOnUiThread(() -> Toast.makeText(
                                        MainActivity.this,
                                        "Error: " + error,
                                        Toast.LENGTH_LONG
                                ).show());
                            }
                        }
                );

                isInitialized = true;
                btnStartVerification.setEnabled(true);
                btnInitialize.setEnabled(false);
                etApiKey.setEnabled(false);
                Toast.makeText(this, "SDK initialized successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to initialize SDK: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Start verification button
        btnStartVerification.setOnClickListener(v -> {
            if (!isInitialized) {
                Toast.makeText(this, "Please initialize SDK first", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start verification flow
            AuthSDK.INSTANCE.startVerification(
                    this,
                    success -> {
                        runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(MainActivity.this, "Verification successful!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return null; // Required for Kotlin Function1<Boolean, Unit>
                    }
            );
        });
    }
}