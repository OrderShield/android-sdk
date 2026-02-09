package com.ordershieldsdk.auth.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.ordershieldsdk.auth.R
import com.ordershieldsdk.auth.core.CallbackManager
import com.ordershieldsdk.auth.core.ErrorHandler
import com.ordershieldsdk.auth.core.EventTracker
import com.ordershieldsdk.auth.data.repository.AuthRepository
import com.ordershieldsdk.auth.internal.StepNavigator
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private lateinit var viewFinder: PreviewView
    private lateinit var cameraPlaceholder: LinearLayout
    private lateinit var tvCountdownText: TextView
    private lateinit var tvCountdownNumber: TextView
    private lateinit var tvHeaderTitle: TextView
    private lateinit var greenCircleOverlay: FrameLayout
    private lateinit var capturedImageView: ImageView
    private lateinit var faceOverlayView: FaceOverlayView
    private lateinit var loadingOverlay: FrameLayout

    private var cameraProvider: ProcessCameraProvider? = null
    private val repository = AuthRepository()
    private var capturedImageFile: File? = null
    private var cameraExecutor: ExecutorService? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var countdownHandler: Handler? = null
    private var countdownRunnable: Runnable? = null
    private var currentCountdown = 3

    // Face detection
    private lateinit var faceDetector: FaceDetector
    private var isFaceDetected = false
    private var faceDetectionStartTime: Long = 0
    private val FACE_DETECTION_DURATION_MS = 3000L // 3 seconds
    private var countdownStarted = false
    private var faceDetectionTimer: Runnable? = null
    private var facePresentDuringCountdown = true
    private var timerRetryCount = 0
    private val MAX_TIMER_RETRIES = 300 // 30 seconds max (300 * 100ms = 30s)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("CameraFragment", "Permission granted")
            startCamera()
        } else {
            activity?.onBackPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Track step start
        EventTracker.trackStepStart(StepNavigator.Step.SELFIE)

        initViews(view)
        initFaceDetector()
        countdownHandler = Handler(Looper.getMainLooper())
        
        // Initialize loading overlay
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
        
        // Initially hide countdown ring and show instruction
        greenCircleOverlay.visibility = View.GONE
        tvCountdownText.visibility = View.VISIBLE
        tvCountdownNumber.visibility = View.GONE
        
        // Update header text
        tvHeaderTitle.text = "Selfie Verification"
        tvCountdownText.text = "Position your face in the frame"
        
        // Check permission and start camera
        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun initViews(view: View) {
        viewFinder = view.findViewById(R.id.viewFinder)
        cameraPlaceholder = view.findViewById(R.id.cameraPlaceholder)
        tvCountdownText = view.findViewById(R.id.tvCountdownText)
        tvCountdownNumber = view.findViewById(R.id.tvCountdownNumber)
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle)
        greenCircleOverlay = view.findViewById(R.id.greenCircleOverlay)
        capturedImageView = view.findViewById(R.id.capturedImageView)
        faceOverlayView = view.findViewById(R.id.faceOverlayView)
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Wait for PreviewView to be measured before using its dimensions
        viewFinder.post {
            // PreviewView is now measured
        }
    }

    private fun initFaceDetector() {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .enableTracking()
            .build()

        faceDetector = FaceDetection.getClient(options)
    }
    
    private fun startCountdown() {
        if (countdownStarted) return
        countdownStarted = true
        facePresentDuringCountdown = true
        
        currentCountdown = 3
        greenCircleOverlay.visibility = View.GONE // Hide the ring
        tvCountdownText.visibility = View.VISIBLE
        tvCountdownNumber.visibility = View.VISIBLE
        tvCountdownNumber.text = currentCountdown.toString()
        tvCountdownText.text = "Taking photo in $currentCountdown..."

        countdownRunnable = object : Runnable {
            override fun run() {
                // Check if face is still present before continuing countdown
                if (!isFaceDetected) {
                    // Face disappeared during countdown - abort
                    abortCountdown()
                    return
                }
                
                if (currentCountdown > 0) {
                    tvCountdownNumber.text = currentCountdown.toString()
                    tvCountdownText.text = "Taking photo in $currentCountdown..."
                    currentCountdown--
                    countdownHandler?.postDelayed(this, 1000)
                } else {
                    // When countdown reaches 0, verify face is still present before capturing
                    if (isFaceDetected && facePresentDuringCountdown) {
                        capturePhoto()
                    } else {
                        // Face disappeared - abort
                        abortCountdown()
                    }
                }
            }
        }
        countdownHandler?.postDelayed(countdownRunnable!!, 1000)
    }
    
    private fun abortCountdown() {
        // Cancel countdown
        countdownRunnable?.let { countdownHandler?.removeCallbacks(it) }
        countdownRunnable = null
        
        // Reset state
        countdownStarted = false
        currentCountdown = 3
        facePresentDuringCountdown = false
        faceDetectionStartTime = 0
        
        // Hide countdown UI
        greenCircleOverlay.visibility = View.GONE
        tvCountdownNumber.visibility = View.GONE
        tvCountdownText.text = "Position your face in the frame"
        tvCountdownText.visibility = View.VISIBLE
        
        // Hide face overlay
        faceOverlayView.visibility = View.GONE
    }
    
    private fun capturePhoto() {
        // Final check - ensure face is still present
        if (!isFaceDetected) {
            abortCountdown()
            return
        }
        
        // Hide countdown UI and face overlay
        greenCircleOverlay.visibility = View.GONE
        tvCountdownText.visibility = View.GONE
        tvCountdownNumber.visibility = View.GONE
        faceOverlayView.visibility = View.GONE
        
        // Change header text to "Photo taken"
        tvHeaderTitle.text = getString(R.string.photo_taken)
        
        // Capture photo
        val imageCapture = imageCapture ?: run {
            Log.e("CameraFragment", "ImageCapture use case is null")
            return
        }
        
        val imageFile = File(requireContext().cacheDir, "captured_photo_${System.currentTimeMillis()}.jpg")
        capturedImageFile = imageFile
        
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()
        
        imageCapture.takePicture(
            outputFileOptions,
            cameraExecutor!!,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Load and display the captured image
                    val savedUri = output.savedUri
                    activity?.runOnUiThread {
                        if (savedUri != null && imageFile.exists()) {
                            capturedImageView.setImageURI(savedUri)
                            capturedImageView.visibility = View.VISIBLE
                            viewFinder.visibility = View.GONE
                            
                            // Upload image immediately
                            uploadSelfieImage(imageFile)
                        }
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraFragment", "Photo capture failed", exception)
                    activity?.runOnUiThread {
                        ErrorHandler.showError(requireContext(), "Failed to capture photo. Please try again.")
                    }
                }
            }
        )
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        
        cameraProviderFuture.addListener({
            try {
                // Camera provider is now guaranteed to be available
                cameraProvider = cameraProviderFuture.get()
                
                // Set up the preview use case
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
                
                // Set up image capture use case
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Set up image analysis for face detection
                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor!!, FaceDetectionAnalyzer())
                    }
                
                // Select front camera
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                
                try {
                    // Unbind use cases before rebinding
                    cameraProvider?.unbindAll()
                    
                    // Bind use cases to camera
                    cameraProvider?.bindToLifecycle(
                        viewLifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalyzer
                    )
                    
                    // Hide placeholder and show camera
                    viewFinder.visibility = View.VISIBLE
                    cameraPlaceholder.visibility = View.GONE
                    
                } catch (e: Exception) {
                    Log.e("CameraFragment", "Use case binding failed", e)
                }
                
            } catch (e: Exception) {
                Log.e("CameraFragment", "Camera provider initialization failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private inner class FaceDetectionAnalyzer : ImageAnalysis.Analyzer {
        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: return
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    // IMPORTANT: The 'faces' list belongs to this specific detection frame.
                    // We handle the UI logic safely on the main thread.
                    activity?.runOnUiThread {
                        val primaryFace = faces.firstOrNull() // Use safe access

                        if (primaryFace != null) {
                            isFaceDetected = true

                            // Handle rotation for coordinate mapping
                            val isRotated = rotationDegrees == 90 || rotationDegrees == 270
                            val mappedWidth = if (isRotated) imageProxy.height else imageProxy.width
                            val mappedHeight = if (isRotated) imageProxy.width else imageProxy.height

                            // Update the boundary circle
                            faceOverlayView.setFaces(listOf(primaryFace), mappedWidth, mappedHeight)
                            faceOverlayView.visibility = View.VISIBLE

                            if (!countdownStarted) {
                                if (faceDetectionStartTime == 0L) faceDetectionStartTime = System.currentTimeMillis()
                                startFaceDetectionTimer()
                            } else {
                                facePresentDuringCountdown = true
                            }
                        } else {
                            // NO FACE DETECTED
                            isFaceDetected = false
                            handleFaceLost()
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    // Helper to keep logic clean
    private fun handleFaceLost() {
        // Reset timer counter when face is lost
        timerRetryCount = 0
        
        if (countdownStarted) {
            facePresentDuringCountdown = false
            abortCountdown()
        } else {
            faceDetectionStartTime = 0
            faceOverlayView.visibility = View.GONE
            tvCountdownText.text = "Position your face in the frame"
        }
    }
    
    private fun startFaceDetectionTimer() {
        // Cancel any existing timer
        faceDetectionTimer?.let { countdownHandler?.removeCallbacks(it) }
        
        // Reset retry count when starting new timer
        timerRetryCount = 0
        
        faceDetectionTimer = object : Runnable {
            override fun run() {
                // Safety check: prevent infinite loops with timeout
                if (timerRetryCount >= MAX_TIMER_RETRIES) {
                    Log.w("CameraFragment", "Face detection timer timeout reached, resetting")
                    resetFaceDetection()
                    return
                }
                
                timerRetryCount++
                
                if (isFaceDetected && !countdownStarted) {
                    val elapsedTime = System.currentTimeMillis() - faceDetectionStartTime
                    if (elapsedTime >= FACE_DETECTION_DURATION_MS) {
                        // 3 seconds passed - start countdown
                        timerRetryCount = 0 // Reset counter when countdown starts
                        startCountdown()
                    } else {
                        // Continue checking
                        countdownHandler?.postDelayed(this, 100)
                    }
                } else if (!isFaceDetected) {
                    // Face lost, reset counter
                    timerRetryCount = 0
                }
            }
        }
        countdownHandler?.postDelayed(faceDetectionTimer!!, 100)
    }
    
    /**
     * Reset face detection state when timeout occurs
     */
    private fun resetFaceDetection() {
        timerRetryCount = 0
        faceDetectionStartTime = 0
        isFaceDetected = false
        faceOverlayView.visibility = View.GONE
        tvCountdownText.text = "Position your face in the frame"
        if (countdownStarted) {
            abortCountdown()
        }
    }

    override fun onPause() {
        super.onPause()
        // Cancel countdown
        countdownRunnable?.let { countdownHandler?.removeCallbacks(it) }
        countdownRunnable = null
        
        // Cancel face detection timer
        faceDetectionTimer?.let { countdownHandler?.removeCallbacks(it) }
        faceDetectionTimer = null
    }

    private fun uploadSelfieImage(imageFile: File) {
        // Show loader
        showLoader(true)
        
        lifecycleScope.launch {
            try {
                val result = repository.uploadSelfie(imageFile)
                
                result.onSuccess { success ->
                    if (success) {
                        // Track step end
                        EventTracker.trackStepEnd(StepNavigator.Step.SELFIE)
                        // Notify callback that selfie step is completed
                        CallbackManager.notifyStepCompleted("selfie")
                        
                        // Upload successful, navigate to next step
                        showLoader(false)
                        navigateToNextStep()
                    } else {
                        // Upload failed
                        showLoader(false)
                        ErrorHandler.showError(requireContext(), "Failed to upload selfie. Please try again.")
                        Log.e("CameraFragment", "Selfie upload failed")
                    }
                }.onFailure { exception ->
                    // Handle error
                    showLoader(false)
                    ErrorHandler.showError(requireContext(), exception)
                    Log.e("CameraFragment", "Selfie upload error: ${exception.message}")
                }
            } catch (e: Exception) {
                showLoader(false)
                ErrorHandler.showError(requireContext(), e)
                Log.e("CameraFragment", "Selfie upload exception: ${e.message}")
            }
        }
    }
    
    private fun showLoader(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun navigateToNextStep() {
        val nextStep = com.ordershieldsdk.auth.internal.StepNavigator.getNextStep(
            com.ordershieldsdk.auth.internal.StepNavigator.Step.SELFIE
        )
        if (nextStep != null) {
            val fragment = com.ordershieldsdk.auth.internal.StepNavigator.createFragmentForStep(nextStep)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        } else {
            // Unexpected null - fallback to COMPLETE screen to prevent user being stuck
            Log.w("CameraFragment", "getNextStep returned null, navigating to COMPLETE as fallback")
            val fragment = com.ordershieldsdk.auth.internal.StepNavigator.createFragmentForStep(
                com.ordershieldsdk.auth.internal.StepNavigator.Step.COMPLETE
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel countdown
        countdownRunnable?.let { countdownHandler?.removeCallbacks(it) }
        countdownRunnable = null
        
        // Cancel face detection timer
        faceDetectionTimer?.let { countdownHandler?.removeCallbacks(it) }
        faceDetectionTimer = null
        
        countdownHandler = null
        
        // Shutdown camera executor
        cameraExecutor?.shutdown()
        cameraExecutor = null
        
        // Close face detector
        faceDetector.close()
    }
}
