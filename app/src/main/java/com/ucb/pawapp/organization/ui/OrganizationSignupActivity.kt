package com.ucb.pawapp.organization.ui

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ucb.pawapp.R
import com.ucb.pawapp.organization.model.OrganizationSignupData
import com.ucb.pawapp.organization.viewmodel.OrganizationSignupViewModel

class OrganizationSignupActivity : AppCompatActivity() {
    private val viewModel: OrganizationSignupViewModel by viewModels()
    private val organizationTypes = arrayOf("Shelter", "Rescue Group", "Advocacy Group", "Veterinary Clinic", "Other")
    private var logoImageUri: Uri? = null
    private var documentImageUri: Uri? = null

    // Image picker launchers
    private val pickLogo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            logoImageUri = it
            findViewById<ShapeableImageView>(R.id.logo_image).setImageURI(it)
        }
    }

    private val pickDocument = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            documentImageUri = it
            findViewById<ShapeableImageView>(R.id.document_image).setImageURI(it)
            findViewById<TextView>(R.id.tv_document_name).text = getFileName(it) ?: "Document selected"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_signup)

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Register Organization"

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Find views
        val tilOrgName = findViewById<TextInputLayout>(R.id.til_org_name)
        val etOrgName = findViewById<TextInputEditText>(R.id.et_org_name)
        val tilRepName = findViewById<TextInputLayout>(R.id.til_rep_name)
        val etRepName = findViewById<TextInputEditText>(R.id.et_rep_name)
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
        val tilLicense = findViewById<TextInputLayout>(R.id.til_license)
        val etLicense = findViewById<TextInputEditText>(R.id.et_license)
        val orgTypeSpinner = findViewById<Spinner>(R.id.spinner_org_type)
        val cbTerms = findViewById<MaterialCheckBox>(R.id.cb_terms)
        val btnSignup = findViewById<MaterialButton>(R.id.btn_signup)
        val btnUploadLogo = findViewById<MaterialButton>(R.id.btn_upload_logo)
        val btnUploadDocument = findViewById<MaterialButton>(R.id.btn_upload_document)

        // Setup spinner
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_layout,
            organizationTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        orgTypeSpinner.adapter = adapter

        // Setup image upload buttons
        btnUploadLogo.setOnClickListener {
            pickLogo.launch("image/*")
        }

        btnUploadDocument.setOnClickListener {
            pickDocument.launch("*/*")
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
                showSnackbar("You must accept the terms")
                return@setOnClickListener
            }

            // Validate inputs
            var hasError = false

            if (etOrgName.text.toString().trim().isEmpty()) {
                tilOrgName.error = "Organization name is required"
                hasError = true
            }

            if (etRepName.text.toString().trim().isEmpty()) {
                tilRepName.error = "Representative name is required"
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

            if (etLicense.text.toString().trim().isEmpty()) {
                tilLicense.error = "License number is required"
                hasError = true
            }

            if (hasError) return@setOnClickListener

            val signupData = OrganizationSignupData(
                organizationName = etOrgName.text.toString().trim(),
                representativeName = etRepName.text.toString().trim(),
                email = etEmail.text.toString().trim(),
                phone = etPhone.text.toString().trim(),
                address = etAddress.text.toString().trim(),
                licenseNumber = etLicense.text.toString().trim(),
                organizationType = orgTypeSpinner.selectedItem.toString(),
                logoImageUri = logoImageUri?.toString(),
                documentImageUri = documentImageUri?.toString()
            )

            viewModel.registerOrganization(
                data = signupData,
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

    private fun getFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    return it.getString(displayNameIndex)
                }
            }
        }
        return null
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}