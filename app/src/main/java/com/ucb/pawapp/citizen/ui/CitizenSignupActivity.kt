package com.ucb.pawapp.citizen.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.ucb.pawapp.R
import com.ucb.pawapp.citizen.model.CitizenSignupData
import com.ucb.pawapp.citizen.viewmodel.CitizenSignupViewModel
import java.util.*

class CitizenSignupActivity : AppCompatActivity() {
    private val viewModel: CitizenSignupViewModel by viewModels()
    private var selectedDob: String = ""
    private var profileImageUri: Uri? = null

    // Image picker launcher
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            profileImageUri = it
            findViewById<ShapeableImageView>(R.id.profile_image).setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_citizen_signup)

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Account"

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Find views
        val tilFullName = findViewById<TextInputLayout>(R.id.til_full_name)
        val etFullName = findViewById<TextInputEditText>(R.id.et_full_name)
        val tilEmail = findViewById<TextInputLayout>(R.id.til_email)
        val etEmail = findViewById<TextInputEditText>(R.id.et_email)
        val tilPassword = findViewById<TextInputLayout>(R.id.til_password)
        val etPassword = findViewById<TextInputEditText>(R.id.et_password)
        val tilConfirmPassword = findViewById<TextInputLayout>(R.id.til_confirm_password)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.et_confirm_password)
        val tilPhone = findViewById<TextInputLayout>(R.id.til_phone)
        val etPhone = findViewById<TextInputEditText>(R.id.et_phone)
        val tilAddress = findViewById<TextInputLayout>(R.id.til_address)
        val etAddress = findViewById<TextInputEditText>(R.id.et_address)
        val tvDob = findViewById<MaterialTextView>(R.id.tv_dob)
        val cbTerms = findViewById<MaterialCheckBox>(R.id.cb_terms)
        val btnSignup = findViewById<MaterialButton>(R.id.btn_signup)
        val profileImage = findViewById<ShapeableImageView>(R.id.profile_image)
        val btnUploadImage = findViewById<MaterialButton>(R.id.btn_upload_image)

        // Setup image upload
        btnUploadImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Setup date picker
        tvDob.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDob = "$year-${month + 1}-$day"
                    tvDob.text = selectedDob
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Input validations
        etEmail.doOnTextChanged { text, _, _, _ ->
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches()) {
                tilEmail.error = null
            } else {
                tilEmail.error = "Please enter a valid email"
            }
        }

        etPassword.doOnTextChanged { text, _, _, _ ->
            if (text.toString().length >= 6) {
                tilPassword.error = null
            } else {
                tilPassword.error = "Password must be at least 6 characters"
            }
        }

        etConfirmPassword.doOnTextChanged { text, _, _, _ ->
            if (text.toString() == etPassword.text.toString()) {
                tilConfirmPassword.error = null
            } else {
                tilConfirmPassword.error = "Passwords don't match"
            }
        }

        // Submit button
        btnSignup.setOnClickListener {
            if (!cbTerms.isChecked) {
                showSnackbar("You must agree to the terms")
                return@setOnClickListener
            }

            // Validate inputs
            var hasError = false

            if (etFullName.text.toString().trim().isEmpty()) {
                tilFullName.error = "Name is required"
                hasError = true
            }

            if (etEmail.text.toString().trim().isEmpty() || tilEmail.error != null) {
                tilEmail.error = "Valid email is required"
                hasError = true
            }

            if (etPassword.text.toString().trim().isEmpty() || tilPassword.error != null) {
                tilPassword.error = "Valid password is required"
                hasError = true
            }

            if (etConfirmPassword.text.toString() != etPassword.text.toString()) {
                tilConfirmPassword.error = "Passwords don't match"
                hasError = true
            }

            if (selectedDob.isEmpty()) {
                showSnackbar("Please select your date of birth")
                hasError = true
            }

            if (hasError) return@setOnClickListener

            val signupData = CitizenSignupData(
                fullName = etFullName.text.toString().trim(),
                email = etEmail.text.toString().trim(),
                phone = etPhone.text.toString().trim(),
                address = etAddress.text.toString().trim(),
                dateOfBirth = selectedDob,
                profileImageUri = profileImageUri?.toString()
            )

            viewModel.registerCitizen(
                signupData,
                password = etPassword.text.toString().trim(),
                confirmPassword = etConfirmPassword.text.toString().trim()
            )
        }
    }

    private fun observeViewModel() {
        viewModel.signupSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                showSnackbar("Signup successful!")
                finish()
            }
        }

        viewModel.signupError.observe(this) { errorMessage ->
            errorMessage?.let { showSnackbar(it) }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.root_layout), message, Snackbar.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}