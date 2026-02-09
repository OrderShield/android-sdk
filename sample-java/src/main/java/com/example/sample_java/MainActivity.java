package com.example.sample_java;

import static android.app.ProgressDialog.show;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.ordershieldsdk.auth.core.AuthSDK;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etApiKey;
    private MaterialButton btnInitialize;
    private MaterialButton btnStartVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etApiKey = findViewById(R.id.etApiKey);
        btnInitialize = findViewById(R.id.btnInitialize);
        btnStartVerify = findViewById(R.id.btnStartVerify);

        btnInitialize.setOnClickListener(v -> {
            // Initialize SDK
            String apiKey = etApiKey.getText() != null ? etApiKey.getText().toString() : "";
            AuthSDK.INSTANCE.init(
                    this,
                    apiKey.trim(),
                    true // enableLogging
            );
        });

        btnStartVerify.setOnClickListener(v -> {
            // Start verification flow
            AuthSDK.INSTANCE.startVerification(
                    this,
                    error -> {
                        // Handle error
                        return null;
                    },
                    step -> {
                        // Handle step completion
                        return null;
                    },
                    () -> {
                        // Navigate to next screen (e.g., payment screen)
                        return null;
                    }
            );
        });
    }
}