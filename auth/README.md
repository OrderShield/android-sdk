# OrderShield Auth SDK

OrderShield Auth SDK is an Android library module that provides a complete verification flow for user authentication. It handles device registration, selfie verification, user information collection, email/phone verification, and terms & conditions acceptance.

## Table of Contents

- [Installation](#installation)
- [Setup](#setup)
- [Initialization](#initialization)
- [Starting Verification](#starting-verification)
- [Callbacks](#callbacks)
- [Verification Steps](#verification-steps)
- [Configuration](#configuration)
- [Requirements](#requirements)

## Installation

### Step 1: Add Module to Your Project

1. Copy the `auth` module folder into your Android project root directory (same level as your `app` module).

2. Add the module to your `settings.gradle.kts` (or `settings.gradle`):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "YourAppName"
include(":app")
include(":auth")  // Add this line
```

### Step 2: Add Dependency in App Module

Add the auth module as a dependency in your `app/build.gradle.kts` (or `app/build.gradle`):

```kotlin
dependencies {
    implementation(project(":auth"))
    // ... other dependencies
}
```

### Step 3: Sync Project

Sync your Gradle files to ensure the module is properly integrated.

## Setup

### Permissions

The SDK requires the following permissions. Add them to your `app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

**Note:** `READ_PHONE_STATE` is optional and only used for auto-detecting country code. The SDK will work without it by falling back to device locale.

## Initialization

Initialize the SDK in your `Application` class or `MainActivity` before using it:

### Basic Initialization

```kotlin
import com.ordershieldsdk.auth.core.AuthSDK

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK
        AuthSDK.init(
            context = this,
            apiKey = "your-api-key-here",
            enableLogging = true // Set to false in production
        )
    }
}
```

### Initialization with Callbacks (Recommended)

For step-by-step verification callbacks:

```kotlin
import com.ordershieldsdk.auth.core.AuthSDK
import com.ordershieldsdk.auth.core.VerificationCallback

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK with callbacks
        AuthSDK.init(
            context = this,
            apiKey = "your-api-key-here",
            enableLogging = true,
            callback = object : VerificationCallback {
                override fun onStepCompleted(step: String) {
                    // Called when each verification step completes
                    // Possible step values: "selfie", "userInfo", "email", "sms", "terms", "signature"
                    Log.d("AuthSDK", "Step completed: $step")
                }
                
                override fun onVerificationCompleted() {
                    // Called when all verification steps are completed successfully
                    Log.d("AuthSDK", "All verification completed successfully!")
                    // Update UI, navigate to next screen, etc.
                }
                
                override fun onVerificationFailed(error: String) {
                    // Note: This callback is defined but may not be called in all error scenarios
                    // Errors are typically handled and displayed to users via Toast messages
                    Log.e("AuthSDK", "Verification failed: $error")
                }
            }
        )
    }
}
```

### Advanced Initialization with SDKConfig

For more control over configuration:

```kotlin
import com.ordershieldsdk.auth.core.AuthSDK
import com.ordershieldsdk.auth.core.SDKConfig

val config = SDKConfig(
    apiKey = "your-api-key-here",
    enableLogging = true
)

AuthSDK.init(
    config = config,
    callback = yourVerificationCallback // Optional
)
```

## Starting Verification

### Basic Usage

```kotlin
// Start verification flow
AuthSDK.startVerification(this)
```

### With Result Callback

```kotlin
// Start verification with simple result callback
AuthSDK.startVerification(this) { success ->
    if (success) {
        Log.d("AuthSDK", "Verification completed successfully!")
        // Handle success
    } else {
        Log.e("AuthSDK", "Verification failed")
        // Handle failure
    }
}
```

### Complete Example

```kotlin
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ordershieldsdk.auth.core.AuthSDK
import com.ordershieldsdk.auth.core.VerificationCallback

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize SDK with callbacks
        AuthSDK.init(
            context = this,
            apiKey = "your-api-key-here",
            enableLogging = true,
            callback = object : VerificationCallback {
                override fun onStepCompleted(step: String) {
                    // Called for each completed step: "selfie", "userInfo", "email", "sms", "terms", "signature"
                    Toast.makeText(this@MainActivity, "Step completed: $step", Toast.LENGTH_SHORT).show()
                }
                
                override fun onVerificationCompleted() {
                    // Called when all verification steps are completed
                    Toast.makeText(this@MainActivity, "Verification completed!", Toast.LENGTH_LONG).show()
                    // Navigate to next screen or update UI
                }
                
                override fun onVerificationFailed(error: String) {
                    // Note: May not be called in all error scenarios
                    Toast.makeText(this@MainActivity, "Error: $error", Toast.LENGTH_LONG).show()
                }
            }
        )
        
        // Setup button to start verification
        findViewById<Button>(R.id.btnStartVerification).setOnClickListener {
            AuthSDK.startVerification(this) { success ->
                if (success) {
                    // Verification completed successfully
                    Toast.makeText(this@MainActivity, "Verification successful!", Toast.LENGTH_SHORT).show()
                } else {
                    // Verification failed
                    Toast.makeText(this@MainActivity, "Verification failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
```

## Callbacks

The SDK provides two types of callbacks:

### 1. VerificationCallback (Step-by-Step)

Provides detailed callbacks for each verification step:

- **`onStepCompleted(step: String)`** - Called when each step completes successfully
  - Possible step values: `"selfie"`, `"userInfo"`, `"email"`, `"sms"`, `"terms"`, `"signature"`
  - Called in sequence as each step is completed
- **`onVerificationCompleted()`** - Called when all verification steps are completed successfully
  - This is called when the user reaches the completion screen
- **`onVerificationFailed(error: String)`** - Defined but may not be called in all error scenarios
  - Most errors are handled internally and displayed to users via Toast messages

### 2. Simple Result Callback

Provides a simple boolean result:

- **`onResult: (Boolean) -> Unit`** - Called with `true` when verification completes successfully, `false` on failure
  - This callback is called when `onVerificationCompleted()` is triggered (success) or when verification fails

**Note:** You can use both callbacks together. The `VerificationCallback` provides detailed step-by-step notifications, while the simple callback provides a final result.

## Verification Steps

The SDK follows a fixed sequence of verification steps:

1. **Phone (SMS)** - Phone number input with optional OTP verification
   - Step name in callback: `"sms"`
2. **Selfie** - Selfie capture and upload
   - Step name in callback: `"selfie"`
3. **User Info** - First name, last name, and date of birth
   - Step name in callback: `"userInfo"`
4. **Email** - Email input with optional OTP verification
   - Step name in callback: `"email"`
5. **Terms & Conditions** - Terms acceptance with optional signature
   - Step names in callback: `"terms"` (when terms are accepted), `"signature"` (when signature is uploaded, if required)

**Important Notes:**
- The steps shown depend on the `steps_remaining` array returned from the verification status API
- Steps that are already completed or not in `steps_remaining` will be skipped automatically
- The sequence is fixed: Phone → Selfie → User Info → Email → Terms & Conditions
- Each step completion triggers `onStepCompleted()` with the corresponding step name

## Configuration

### SDKConfig

You can customize the SDK configuration:

```kotlin
val config = SDKConfig(
    apiKey = "your-api-key-here",
    enableLogging = true // Enable HTTP request/response logging
)
```

### Base URL

The base URL is configured in the SDK and points to: `https://ordershield-api.projectbeta.biz/`

## Requirements

- **Minimum SDK:** Android API 29 (Android 10)
- **Target SDK:** Android API 36
- **Kotlin:** 1.9.0 or higher
- **Java:** 21

## Features

- ✅ Device registration
- ✅ Selfie verification with face detection
- ✅ User information collection
- ✅ Email verification with OTP
- ✅ Phone verification with OTP
- ✅ Terms & conditions with dynamic checkboxes
- ✅ Signature capture and upload
- ✅ Step-by-step callbacks
- ✅ Auto country code detection
- ✅ Error handling with user-friendly messages
- ✅ Dynamic step flow based on API configuration

## Error Handling

The SDK automatically handles and displays errors to users. All API errors are parsed and shown as user-friendly Toast messages within the SDK. 

**Note:** The `onVerificationFailed()` callback in `VerificationCallback` is defined but may not be called in all error scenarios. Errors are typically handled internally and displayed to users automatically. The simple `onResult` callback in `startVerification()` will be called with `false` if verification fails.

## Support

For issues, questions, or feature requests, please contact the OrderShield team.

## License

[Add your license information here]
