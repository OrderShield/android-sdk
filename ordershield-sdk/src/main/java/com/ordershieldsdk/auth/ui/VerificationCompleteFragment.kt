package com.ordershieldsdk.auth.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.ordershieldsdk.auth.R
import com.ordershieldsdk.auth.core.CallbackManager
import com.ordershieldsdk.auth.core.EventTracker

class VerificationCompleteFragment : Fragment(R.layout.fragment_verification_complete) {

    private lateinit var btnClose: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Track session end event
        EventTracker.trackSessionEnd()

        initViews(view)
        setupClickListeners()
    }

    private fun initViews(view: View) {
        btnClose = view.findViewById(R.id.btnClose)
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener {
            // Call callback before finishing - allows app to navigate (e.g., to payment screen)
            CallbackManager.notifyVerificationCompleted()
            activity?.setResult(android.app.Activity.RESULT_OK)
            activity?.finish()
        }
    }
}
