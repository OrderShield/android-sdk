package com.ordershieldsdk.auth.ui

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.ordershieldsdk.auth.R
import com.ordershieldsdk.auth.core.DeviceInfoHelper
import com.ordershieldsdk.auth.core.ErrorHandler
import com.ordershieldsdk.auth.core.EventTracker
import com.ordershieldsdk.auth.core.SessionManager
import com.ordershieldsdk.auth.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class VerificationInfoFragment : Fragment(R.layout.fragment_verification_info) {

    private lateinit var btnStartVerification: MaterialButton
    private lateinit var loadingOverlay: FrameLayout
    private var backPressedCallback: OnBackPressedCallback? = null
    private val repository = AuthRepository()

    // Retry configuration
    private var registerDeviceRetryCount = 0
    private var startVerificationRetryCount = 0
    private var getStatusRetryCount = 0
    private val MAX_RETRIES = 3
    private val INITIAL_RETRY_DELAY_MS = 1000L // 1 second

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        handleEdgeToEdge(view)
        lockScreen()

        // Call APIs when fragment is shown
        initializeVerification()

        setupClickListeners()
    }

    private fun initializeVerification() {
        // Reset retry counts
        registerDeviceRetryCount = 0
        startVerificationRetryCount = 0
        getStatusRetryCount = 0

        // Show loader
        showLoader(true)

        // Disable button until APIs complete
        btnStartVerification.isEnabled = false

        lifecycleScope.launch {
            try {
                // Session is created at SDK init and stored in SharedPreferences.
                // If already present, only fetch verification status; otherwise register + start then status.
                if (SessionManager.hasSession()) {
                    getVerificationStatusWithRetry()
                } else {
                    registerDeviceWithRetry()
                }
            } catch (e: Exception) {
                showLoader(false)
                btnStartVerification.isEnabled = true
                ErrorHandler.showError(requireContext(), e)
            }
        }
    }

    /**
     * Register device with retry logic
     */
    private suspend fun registerDeviceWithRetry() {
        val deviceInfo = DeviceInfoHelper.getDeviceInfo(requireContext())
        val registerResult = repository.registerDevice(deviceInfo)

        registerResult.onSuccess { customerId ->
            // Reset retry count on success
            registerDeviceRetryCount = 0
            // Store customer ID
            SessionManager.setCustomerId(customerId)
            // Proceed to start verification
            startVerificationWithRetry(customerId)
        }.onFailure { exception ->
            // Check if error is retryable
            if (isRetryableError(exception) && registerDeviceRetryCount < MAX_RETRIES) {
                registerDeviceRetryCount++
                // Track retry event
                EventTracker.trackStepRetry("register_device")
                val delayMs = INITIAL_RETRY_DELAY_MS * (1 shl (registerDeviceRetryCount - 1)) // Exponential backoff
                android.util.Log.d(
                    "VerificationInfoFragment",
                    "Retrying registerDevice (attempt $registerDeviceRetryCount/$MAX_RETRIES) after ${delayMs}ms"
                )
                delay(delayMs)
                registerDeviceWithRetry()
            } else {
                // Max retries reached or non-retryable error
                showLoader(false)
                btnStartVerification.isEnabled = true
                ErrorHandler.showError(requireContext(), exception)
            }
        }
    }

    /**
     * Start verification with retry logic
     */
    private suspend fun startVerificationWithRetry(customerId: String) {
        val startResult = repository.startVerification(customerId)

        startResult.onSuccess { (sessionId, sessionToken) ->
            // Reset retry count on success
            startVerificationRetryCount = 0
            // Store session info
            SessionManager.setSession(sessionId, sessionToken)
            // Proceed to get verification status
            getVerificationStatusWithRetry()
        }.onFailure { exception ->
            // Check if error is retryable
            if (isRetryableError(exception) && startVerificationRetryCount < MAX_RETRIES) {
                startVerificationRetryCount++
                // Track retry event
                EventTracker.trackStepRetry("start_verification")
                val delayMs = INITIAL_RETRY_DELAY_MS * (1 shl (startVerificationRetryCount - 1)) // Exponential backoff
                android.util.Log.d(
                    "VerificationInfoFragment",
                    "Retrying startVerification (attempt $startVerificationRetryCount/$MAX_RETRIES) after ${delayMs}ms"
                )
                delay(delayMs)
                startVerificationWithRetry(customerId)
            } else {
                // Max retries reached or non-retryable error
                showLoader(false)
                btnStartVerification.isEnabled = true
                ErrorHandler.showError(requireContext(), exception)
            }
        }
    }

    /**
     * Get verification status with retry logic
     */
    private suspend fun getVerificationStatusWithRetry() {
        val statusResult = repository.getVerificationStatus()

        statusResult.onSuccess { statusResult ->
            // Reset retry count on success
            getStatusRetryCount = 0

            // Handle empty steps_remaining based on isComplete flag
            when {
                // If verification is complete, navigate directly to COMPLETE
                statusResult.isComplete == true -> {
                    SessionManager.setStepsRemaining(emptyList())
                    showLoader(false)
                    btnStartVerification.isEnabled = true
                    // Navigate directly to COMPLETE screen
                    navigateToComplete()
                }
                // If steps_remaining is null or empty but not complete, treat as error
                statusResult.stepsRemaining == null || statusResult.stepsRemaining.isEmpty() -> {
                    showLoader(false)
                    btnStartVerification.isEnabled = true
                    ErrorHandler.showError(
                        requireContext(),
                        "No verification steps available. Please try again."
                    )
                    android.util.Log.e(
                        "VerificationInfoFragment",
                        "Empty steps_remaining with isComplete=${statusResult.isComplete}"
                    )
                }
                // Normal case: store steps_remaining, fetch customer-info for steps_completed, then proceed
                else -> {
                    SessionManager.setStepsRemaining(statusResult.stepsRemaining)
                    fetchCustomerInfoAndProceed()
                }
            }
        }.onFailure { exception ->
            // Check if error is retryable
            if (isRetryableError(exception) && getStatusRetryCount < MAX_RETRIES) {
                getStatusRetryCount++
                EventTracker.trackStepRetry("get_verification_status")
                val delayMs = INITIAL_RETRY_DELAY_MS * (1 shl (getStatusRetryCount - 1)) // Exponential backoff
                android.util.Log.d(
                    "VerificationInfoFragment",
                    "Retrying getVerificationStatus (attempt $getStatusRetryCount/$MAX_RETRIES) after ${delayMs}ms"
                )
                delay(delayMs)
                getVerificationStatusWithRetry()
            } else {
                // Max retries reached or non-retryable error
                showLoader(false)
                btnStartVerification.isEnabled = true
                ErrorHandler.showError(requireContext(), exception)
            }
        }
    }

    /**
     * Fetch customer-info (steps_completed) then enable Start button and track session start.
     */
    private suspend fun fetchCustomerInfoAndProceed() {
        val customerId = SessionManager.getCustomerId() ?: run {
            showLoader(false)
            btnStartVerification.isEnabled = true
            EventTracker.trackSessionStart()
            return
        }
        repository.getCustomerInfo(customerId)
            .onSuccess { data ->
                SessionManager.setStepsCompleted(data.stepsCompleted)
            }
            .onFailure { _ ->
                // Non-blocking: proceed without steps_completed (no skip-by-completed)
            }
        showLoader(false)
        btnStartVerification.isEnabled = true
        EventTracker.trackSessionStart()
    }

    /**
     * Check if an error is retryable (network errors, timeouts, etc.)
     * Non-retryable: banned accounts, validation errors, authentication errors
     */
    private fun isRetryableError(exception: Throwable): Boolean {
        // Network-related errors are retryable
        if (exception is ConnectException ||
            exception is SocketTimeoutException ||
            exception is UnknownHostException ||
            exception.cause is ConnectException ||
            exception.cause is SocketTimeoutException ||
            exception.cause is UnknownHostException
        ) {
            return true
        }

        // Check error message for banned account (non-retryable)
        val errorMessage = exception.message?.lowercase() ?: ""
        if (errorMessage.contains("banned") ||
            errorMessage.contains("account banned") ||
            errorMessage.contains("invalid") ||
            errorMessage.contains("unauthorized") ||
            errorMessage.contains("forbidden")
        ) {
            return false
        }

        // Default: retry for other errors (could be temporary server issues)
        return true
    }

    private fun showLoader(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun lockScreen() {
        // Disable back button navigation - lock the screen
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing - screen is locked
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback!!)
    }

    private fun handleEdgeToEdge(view: View) {
        // Handle system window insets for bottom content
        val bottomContent = view.findViewById<View>(R.id.bottomContent)
        bottomContent?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Add bottom padding only if there's a navigation bar (hardware buttons)
                // For gesture navigation, use minimal padding
                val bottomPadding = if (systemBars.bottom > 0) {
                    systemBars.bottom + 16 // Add extra padding for hardware buttons
                } else {
                    16 // Minimal padding for gesture navigation
                }
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding)
                insets
            }
        }
    }

    private fun initViews(view: View) {
        btnStartVerification = view.findViewById(R.id.btnStartVerification)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)

        // Enforce black theme on button to prevent app module overrides
        enforceBlackThemeOnButton(btnStartVerification)

        // Underline the OrderShield text in footer
        val tvOrderShieldLink = view.findViewById<TextView>(R.id.tvOrderShieldLink)
        tvOrderShieldLink?.paintFlags = tvOrderShieldLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setupClickListeners() {
        btnStartVerification.setOnClickListener {
            VerificationFlowHelper.resolveAndNavigate(
                parentFragmentManager,
                com.ordershieldsdk.auth.internal.StepNavigator.Step.INFO,
                lifecycleScope,
                requireContext(),
                repository
            )
        }
    }

    private fun navigateToComplete() {
        VerificationFlowHelper.navigateToComplete(parentFragmentManager)
    }

    private fun enforceBlackThemeOnButton(button: MaterialButton) {
        val BLACK = Color.BLACK
        val WHITE = Color.WHITE
        button.backgroundTintList = android.content.res.ColorStateList.valueOf(BLACK)
        button.setTextColor(WHITE)
        button.iconTint = android.content.res.ColorStateList.valueOf(WHITE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback?.remove()
        backPressedCallback = null
    }
}

