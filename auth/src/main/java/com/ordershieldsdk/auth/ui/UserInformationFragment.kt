package com.ordershieldsdk.auth.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ordershieldsdk.auth.R
import java.util.Calendar

class UserInformationFragment : Fragment(R.layout.fragment_user_information) {

    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etDateOfBirth: TextInputEditText
    private lateinit var btnContinue: MaterialButton
    private lateinit var scrollView: NestedScrollView
    private var scrollPosition = 0

    private var firstName: String = ""
    private var lastName: String = ""
    private var dateOfBirth: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupTextWatchers()
        setupDatePicker()
        setupKeyboardListener()
        setupContinueButton()
        handleEdgeToEdge(view)
    }
    
    private fun handleEdgeToEdge(view: View) {
        // Handle system window insets for footer
        val footerLayout = view.findViewById<View>(R.id.footerLayout)
        footerLayout?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val bottomPadding = if (systemBars.bottom > 0) {
                    systemBars.bottom + 16
                } else {
                    16
                }
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding)
                insets
            }
        }
        
        // Underline the OrderShield text in footer
        val tvOrderShieldLink = view.findViewById<TextView>(R.id.tvOrderShieldLink)
        tvOrderShieldLink?.paintFlags = tvOrderShieldLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun initViews(view: View) {
        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth)
        btnContinue = view.findViewById(R.id.btnContinue)
        scrollView = view.findViewById(R.id.scrollView)
        
        // Enforce black theme on button when enabled
        enforceButtonTheme()
    }
    
    private fun setupDatePicker() {
        etDateOfBirth.setOnClickListener {
            showDatePicker()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format date as mm/dd/yyyy
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                val formattedDay = String.format("%02d", selectedDay)
                dateOfBirth = "$formattedMonth/$formattedDay/$selectedYear"
                etDateOfBirth.setText(dateOfBirth)
                validateForm()
            },
            year,
            month,
            day
        )

        // Set max date to today (user can't be born in the future)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun enforceButtonTheme() {
        // Button starts disabled with gray background
        btnContinue.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (btnContinue.isEnabled) Color.BLACK else resources.getColor(R.color.gray_lighter, null)
        )
        btnContinue.setTextColor(
            if (btnContinue.isEnabled) Color.WHITE else resources.getColor(R.color.gray_light, null)
        )
    }
    
    private fun setupKeyboardListener() {
        val rootView = view?.rootView
        var wasKeyboardOpen = false
        
        rootView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val heightDiff = rootView.rootView.height - rootView.height
                val isKeyboardOpen = heightDiff > 200
                
                if (isKeyboardOpen) {
                    // Keyboard is open - save current scroll position
                    wasKeyboardOpen = true
                    scrollPosition = scrollView.scrollY
                } else if (wasKeyboardOpen) {
                    // Keyboard just closed - restore scroll position
                    wasKeyboardOpen = false
                    scrollView.post {
                        if (scrollPosition > 0) {
                            scrollView.scrollTo(0, scrollPosition)
                        }
                    }
                }
            }
        })
        
        // Handle IME action (Done button) - hide keyboard and restore scroll
        etFirstName.setOnEditorActionListener { _, _, _ ->
            etFirstName.clearFocus()
            hideKeyboard(etFirstName)
            false
        }
        
        etLastName.setOnEditorActionListener { _, _, _ ->
            etLastName.clearFocus()
            hideKeyboard(etLastName)
            false
        }
        
        // Handle focus loss - restore scroll position
        etFirstName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                scrollView.post {
                    if (scrollPosition > 0) {
                        scrollView.scrollTo(0, scrollPosition)
                    }
                }
            }
        }
        
        etLastName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                scrollView.post {
                    if (scrollPosition > 0) {
                        scrollView.scrollTo(0, scrollPosition)
                    }
                }
            }
        }
    }
    
    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupTextWatchers() {
        etFirstName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                firstName = s?.toString()?.trim() ?: ""
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etLastName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lastName = s?.toString()?.trim() ?: ""
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateForm() {
        val isValid = firstName.isNotEmpty() && lastName.isNotEmpty() && dateOfBirth.isNotEmpty()
        btnContinue.isEnabled = isValid
        
        // Update button appearance based on enabled state
        if (isValid) {
            // Active state: black background, white text
            btnContinue.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.BLACK)
            btnContinue.setTextColor(Color.WHITE)
        } else {
            // Disabled state: light gray background, gray text
            btnContinue.backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.gray_lighter, null)
            )
            btnContinue.setTextColor(resources.getColor(R.color.gray_light, null))
        }
    }
    
    private fun setupContinueButton() {
        btnContinue.setOnClickListener {
            // Navigate to EmailFragment
            navigateToEmail()
        }
    }
    
    private fun navigateToEmail() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, EmailFragment())
            .commit()
    }

    fun isValid(): Boolean {
        return firstName.isNotEmpty() && lastName.isNotEmpty() && dateOfBirth.isNotEmpty()
    }

    fun getFirstName(): String = firstName
    fun getLastName(): String = lastName
    fun getDateOfBirth(): String = dateOfBirth
}
