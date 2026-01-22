package com.ordershieldsdk.auth.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ordershieldsdk.auth.R
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private lateinit var viewFinder: PreviewView
    private lateinit var cameraPlaceholder: LinearLayout
    private lateinit var tvCountdownText: TextView
    private lateinit var tvCountdownNumber: TextView
    private lateinit var tvHeaderTitle: TextView
    private lateinit var greenCircleOverlay: View
    private lateinit var capturedImageView: ImageView

    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService? = null
    private var imageCapture: ImageCapture? = null
    private var countdownHandler: Handler? = null
    private var countdownRunnable: Runnable? = null
    private var currentCountdown = 3

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

        initViews(view)
        countdownHandler = Handler(Looper.getMainLooper())
        
        // Start countdown when screen appears
        startCountdown()
        
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
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    
    private fun startCountdown() {
        currentCountdown = 3
        tvCountdownNumber.text = currentCountdown.toString()
        tvCountdownText.text = "Taking photo in $currentCountdown..."

        countdownRunnable = object : Runnable {
            override fun run() {
                if (currentCountdown > 0) {
                    tvCountdownNumber.text = currentCountdown.toString()
                    tvCountdownText.text = "Taking photo in $currentCountdown..."
                    currentCountdown--
                    countdownHandler?.postDelayed(this, 1000)
                } else {
                    // When countdown reaches 0, capture photo
                    capturePhoto()
                }
            }
        }
        countdownHandler?.postDelayed(countdownRunnable!!, 1000)
    }
    
    private fun capturePhoto() {
        // Hide countdown circle and text
        greenCircleOverlay.visibility = View.GONE
        tvCountdownText.visibility = View.GONE
        
        // Change header text to "Photo taken"
        tvHeaderTitle.text = getString(R.string.photo_taken)
        
        // Capture photo
        val imageCapture = imageCapture ?: run {
            Log.e("CameraFragment", "ImageCapture use case is null")
            return
        }
        
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            File(requireContext().cacheDir, "captured_photo.jpg")
        ).build()
        
        imageCapture.takePicture(
            outputFileOptions,
            cameraExecutor!!,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Load and display the captured image
                    val savedUri = output.savedUri
                    activity?.runOnUiThread {
                        if (savedUri != null) {
                            capturedImageView.setImageURI(savedUri)
                            capturedImageView.visibility = View.VISIBLE
                            viewFinder.visibility = View.GONE
                            
                            // Show preview for 2 seconds, then navigate to next screen
                            countdownHandler?.postDelayed({
                                navigateToUserInformation()
                            }, 2000)
                        }
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraFragment", "Photo capture failed", exception)
                    activity?.runOnUiThread {
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
                        imageCapture
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

    override fun onPause() {
        super.onPause()
        // Cancel countdown
        countdownRunnable?.let { countdownHandler?.removeCallbacks(it) }
        countdownRunnable = null
    }

    private fun navigateToUserInformation() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, UserInformationFragment())
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel countdown
        countdownRunnable?.let { countdownHandler?.removeCallbacks(it) }
        countdownHandler = null
        
        // Shutdown camera executor
        cameraExecutor?.shutdown()
        cameraExecutor = null
    }
}
