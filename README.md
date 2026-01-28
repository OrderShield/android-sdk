# OrderShield SDK

OrderShield SDK is an Android SDK that provides authentication and verification capabilities for mobile applications.

## Modules

### Auth Module

The `auth` module provides a complete verification flow including device registration, selfie verification, user information collection, email/phone verification, and terms & conditions acceptance.

**📖 For detailed documentation, see [auth/README.md](auth/README.md)**

## Quick Start

### 1. Add Module to Your Project

Copy the `auth` module folder into your Android project and add it to `settings.gradle.kts`:

```kotlin
include(":app")
include(":auth")
```

### 2. Add Dependency

In your `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":auth"))
}
```

### 3. Initialize SDK

```kotlin
import com.ordershieldsdk.auth.core.AuthSDK

AuthSDK.init(
    context = this,
    apiKey = "your-api-key-here",
    enableLogging = true
)
```

### 4. Start Verification

```kotlin
AuthSDK.startVerification(this) { success ->
    if (success) {
        // Verification completed
    }
}
```

## Documentation

For complete documentation, examples, and API reference, please see:

- **[Auth Module Documentation](auth/README.md)** - Complete guide for the authentication module

## Requirements

- Android API 29+ (Android 10+)
- Kotlin 1.9.0+
- Java 21

## License

[Add your license information here]
