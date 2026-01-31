package com.example.sample_java;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.ordershieldsdk.auth.core.AuthSDK;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        AuthSDK authSDK = AuthSDK.INSTANCE;

        authSDK.init(this, "****************", true, null);

// Setup button click listener
        MaterialButton btnStartVerification = findViewById(R.id.btnStartVerification);
        btnStartVerification.setOnClickListener(v -> {
            // Start verification flow when button is clicked
            authSDK.startVerification(
                    this, success -> {
                        System.out.println("++++++++++++++++++++++++++++++++++++++++");
                        System.out.println("Verification success: " + success);
                        System.out.println("++++++++++++++++++++++++++++++++++++++++");
                        return null; // Required if the callback is a Kotlin Function1<Boolean, Unit>
                    }
            );
        });
    }
}