package com.ordershieldsdk.auth.core

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.TimeZone

/**
 * Helper class to get device information for device registration
 */
internal object DeviceInfoHelper {
    
    /**
     * Get device ID (Android ID)
     */
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-${System.currentTimeMillis()}"
    }
    
    /**
     * Get device type
     */
    fun getDeviceType(): String {
        return "android"
    }
    
    /**
     * Get device model
     */
    fun getDeviceModel(): String {
        return Build.MODEL ?: "Unknown"
    }
    
    /**
     * Get OS version
     */
    fun getOsVersion(): String {
        return Build.VERSION.RELEASE ?: "Unknown"
    }
    
    /**
     * Get app version
     * Note: This requires PackageManager, so we'll use a placeholder or get from context
     */
    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    /**
     * Get IP address
     * Note: This is a simplified version. In production, you might want to get actual IP
     */
    fun getIpAddress(): String {
        // For Android, getting actual IP requires network operations
        // For now, return a placeholder. You can enhance this to get actual IP if needed
        return "0.0.0.0"
    }
    
    /**
     * Get user agent
     */
    fun getUserAgent(context: Context): String {
        val appVersion = getAppVersion(context)
        val osVersion = getOsVersion()
        return "OrderShieldSDK/$appVersion Android/$osVersion"
    }
    
    /**
     * Get timezone
     */
    fun getTimezone(): String {
        return TimeZone.getDefault().id
    }
    
    /**
     * Get all device info as a map
     */
    fun getDeviceInfo(context: Context): com.ordershieldsdk.auth.data.model.RegisterDeviceRequest {
        return com.ordershieldsdk.auth.data.model.RegisterDeviceRequest(
            deviceId = getDeviceId(context),
            deviceType = getDeviceType(),
            deviceModel = getDeviceModel(),
            osVersion = getOsVersion(),
            appVersion = getAppVersion(context),
            ipAddress = getIpAddress(),
            userAgent = getUserAgent(context),
            timezone = getTimezone()
        )
    }
}
