package com.ordershieldsdk.auth.core

import android.content.Context
import android.widget.Toast
import com.google.gson.JsonParser

/**
 * Utility class to handle and display errors to users
 */
internal object ErrorHandler {
    
    /**
     * Parse error message from exception
     * Tries to extract meaningful error message from API responses
     */
    fun parseErrorMessage(exception: Throwable): String {
        val message = exception.message ?: "Unknown error occurred"
        
        // Try to parse JSON error response if available
        try {
            // Check if message contains JSON
            if (message.contains("{") && message.contains("}")) {
                val jsonObject = JsonParser.parseString(message).asJsonObject
                
                // Try to get error message from common fields
                jsonObject.get("message")?.asString?.let { return it }
                jsonObject.get("error")?.asString?.let { return it }
                jsonObject.get("data")?.asJsonObject?.get("error")?.asString?.let { return it }
                jsonObject.get("data")?.asJsonObject?.get("message")?.asString?.let { return it }
            }
        } catch (e: Exception) {
            // If parsing fails, return original message
        }
        
        return message
    }
    
    /**
     * Show error message to user using Toast
     */
    fun showError(context: Context, error: Throwable) {
        val errorMessage = parseErrorMessage(error)
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Show error message to user using Toast with custom message
     */
    fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Parse error from API response body
     * Handles different response formats
     */
    fun parseApiError(responseBody: String?): String {
        if (responseBody.isNullOrEmpty()) {
            return "Unknown error occurred"
        }
        
        try {
            val jsonObject = JsonParser.parseString(responseBody).asJsonObject
            
            // Try different error message fields
            jsonObject.get("message")?.asString?.let { return it }
            jsonObject.get("error")?.asString?.let { return it }
            jsonObject.get("data")?.asJsonObject?.get("error")?.asString?.let { return it }
            jsonObject.get("data")?.asJsonObject?.get("message")?.asString?.let { return it }
            jsonObject.get("data")?.asJsonObject?.get("ban_reason")?.asString?.let { 
                return "Account banned: $it"
            }
        } catch (e: Exception) {
            // If parsing fails, return original response body
        }
        
        return responseBody
    }
}
