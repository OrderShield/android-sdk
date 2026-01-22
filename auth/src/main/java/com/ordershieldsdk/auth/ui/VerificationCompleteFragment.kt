package com.ordershieldsdk.auth.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.ordershieldsdk.auth.R

class VerificationCompleteFragment : Fragment(R.layout.fragment_verification_complete) {

    private lateinit var btnClose: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
    }

    private fun initViews(view: View) {
        btnClose = view.findViewById(R.id.btnClose)
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener {
            activity?.setResult(android.app.Activity.RESULT_OK)
            activity?.finish()
        }
    }
}
