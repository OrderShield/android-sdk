package com.ordershieldsdk.auth.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.ordershieldsdk.auth.R
import java.util.Calendar

class DateOfBirthFragment : Fragment(R.layout.fragment_date_of_birth) {

    private lateinit var etDateOfBirth: TextInputEditText
    private var dateOfBirth: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupDatePicker()
        
        // Continue button removed
    }

    private fun initViews(view: View) {
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth)
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

    private fun validateForm() {
        val isValid = dateOfBirth.isNotEmpty()
        // Continue button removed
    }

    fun isValid(): Boolean {
        return dateOfBirth.isNotEmpty()
    }

    fun getDateOfBirth(): String = dateOfBirth
}
