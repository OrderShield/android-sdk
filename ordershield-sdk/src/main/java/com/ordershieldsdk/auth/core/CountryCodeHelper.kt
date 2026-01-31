package com.ordershieldsdk.auth.core

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale

/**
 * Helper utility to detect user's country and map it to phone country code
 */
internal object CountryCodeHelper {
    
    /**
     * Mapping from country ISO codes (e.g., "IN", "US") to phone country codes (e.g., "+91", "+1")
     */
    private val countryToPhoneCodeMap = mapOf(
        "US" to "+1",      // United States
        "CA" to "+1",      // Canada
        "GB" to "+44",     // United Kingdom
        "IN" to "+91",     // India
        "CN" to "+86",     // China
        "JP" to "+81",     // Japan
        "DE" to "+49",     // Germany
        "FR" to "+33",     // France
        "RU" to "+7",      // Russia
        "IT" to "+39",     // Italy
        "ES" to "+34",     // Spain
        "AU" to "+61",     // Australia
        "BR" to "+55",     // Brazil
        "KR" to "+82",     // South Korea
        "NL" to "+31",     // Netherlands
        "SE" to "+46",     // Sweden
        "CH" to "+41",     // Switzerland
        "BE" to "+32",     // Belgium
        "ZA" to "+27",     // South Africa
        "MX" to "+52",     // Mexico
        "NO" to "+47"      // Norway
    )
    
    /**
     * Detect user's country code using multiple methods
     * Priority: SIM card > Network > Locale
     */
    fun detectCountryCode(context: Context): String? {
        // Method 1: Try to get from SIM card (most reliable)
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val simCountry = telephonyManager?.simCountryIso?.uppercase()
            if (!simCountry.isNullOrEmpty()) {
                return simCountry
            }
        } catch (e: Exception) {
            // Permission might not be granted, continue to next method
        }
        
        // Method 2: Try to get from network
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val networkCountry = telephonyManager?.networkCountryIso?.uppercase()
            if (!networkCountry.isNullOrEmpty()) {
                return networkCountry
            }
        } catch (e: Exception) {
            // Permission might not be granted, continue to next method
        }
        
        // Method 3: Fallback to device locale
        val localeCountry = Locale.getDefault().country
        if (!localeCountry.isNullOrEmpty()) {
            return localeCountry.uppercase()
        }
        
        return null
    }
    
    /**
     * Get phone country code from country ISO code
     * Returns null if country is not in the supported list
     */
    fun getPhoneCodeForCountry(countryIso: String?): String? {
        if (countryIso == null) return null
        return countryToPhoneCodeMap[countryIso.uppercase()]
    }
    
    /**
     * Detect user's phone country code directly
     * Returns the phone code (e.g., "+91") if detected, null otherwise
     */
    fun detectPhoneCountryCode(context: Context): String? {
        val countryIso = detectCountryCode(context)
        return getPhoneCodeForCountry(countryIso)
    }
}
