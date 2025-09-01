package com.ucb.pawapp.organization.ui

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import com.ucb.pawapp.R
import com.ucb.pawapp.organization.model.OrganizationSignupData
import com.ucb.pawapp.organization.viewmodel.OrganizationSignupViewModel
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged

class OrganizationSignupActivity : AppCompatActivity() {
    private val viewModel: OrganizationSignupViewModel by viewModels()
    private val organizationTypes = arrayOf(
        "Shelter", "Rescue Group", "Advocacy Group", "Veterinary Clinic", "Other"
    )
    private var logoImageUri: Uri? = null
    private var documentImageUri: Uri? = null

    // pickers
    private val pickLogo = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.also {
        logoImageUri = it
        findViewById<ShapeableImageView>(R.id.logo_image).setImageURI(it)
    }
    }
    private val pickDoc = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.also {
        documentImageUri = it
        findViewById<ShapeableImageView>(R.id.document_image).setImageURI(it)
        findViewById<TextView>(R.id.tv_document_name).text = it.lastPathSegment
    }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_signup)

        // toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Register Organization"

        setupForm()
        observeViewModel()
    }

    private fun setupForm() {
        val tilOrgName     = findViewById<TextInputLayout>(R.id.til_org_name)
        val etOrgName      = findViewById<TextInputEditText>(R.id.et_org_name)
        val tilRepName     = findViewById<TextInputLayout>(R.id.til_rep_name)
        val etRepName      = findViewById<TextInputEditText>(R.id.et_rep_name)
        val tilEmail       = findViewById<TextInputLayout>(R.id.til_email)
        val etEmail        = findViewById<TextInputEditText>(R.id.et_email)
        val tilPassword    = findViewById<TextInputLayout>(R.id.til_password)
        val etPassword     = findViewById<TextInputEditText>(R.id.et_password)
        val tilConfirm     = findViewById<TextInputLayout>(R.id.til_confirm_password)
        val etConfirm      = findViewById<TextInputEditText>(R.id.et_confirm_password)
        val tilPhone       = findViewById<TextInputLayout>(R.id.til_phone)
        val etPhone        = findViewById<TextInputEditText>(R.id.et_phone)
        val tilAddress     = findViewById<TextInputLayout>(R.id.til_address)
        val etAddress      = findViewById<TextInputEditText>(R.id.et_address)
        val tilLicense     = findViewById<TextInputLayout>(R.id.til_license)
        val etLicense      = findViewById<TextInputEditText>(R.id.et_license)
        val spinnerType    = findViewById<Spinner>(R.id.spinner_org_type)
        val cbTerms        = findViewById<MaterialCheckBox>(R.id.cb_terms)
        val btnUploadLogo  = findViewById<MaterialButton>(R.id.btn_upload_logo)
        val btnUploadDoc   = findViewById<MaterialButton>(R.id.btn_upload_document)
        val btnSignup      = findViewById<MaterialButton>(R.id.btn_signup)

        // spinner
        Spinner(this).also {
            spinnerType.adapter = ArrayAdapter(
                this, R.layout.spinner_item_layout, organizationTypes
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        // pickers
        btnUploadLogo.setOnClickListener { pickLogo.launch("image/*") }
        btnUploadDoc.setOnClickListener  { pickDoc.launch("*/*")     }

        // basic email/password inline validation
        etEmail.doOnTextChanged { txt, _, _, _ ->
            tilEmail.error =
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(txt.toString()).matches())
                    null else "Invalid email"
        }
        etPassword.doOnTextChanged { txt, _, _, _ ->
            tilPassword.error =
                if (txt!!.length >= 6) null else "Min 6 chars"
        }
        etConfirm.doOnTextChanged { txt, _, _, _ ->
            tilConfirm.error =
                if (txt.toString() == etPassword.text.toString())
                    null else "Passwords differ"
        }

        btnSignup.setOnClickListener {
            if (!cbTerms.isChecked) {
                Snackbar.make(
                    findViewById(R.id.root_layout),
                    "You must accept terms",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // collect & send
            val data = OrganizationSignupData(
                organizationName   = etOrgName.text.toString().trim(),
                representativeName = etRepName.text.toString().trim(),
                email              = etEmail.text.toString().trim(),
                phone              = etPhone.text.toString().trim(),
                address            = etAddress.text.toString().trim(),
                licenseNumber      = etLicense.text.toString().trim(),
                organizationType   = spinnerType.selectedItem as String,
                logoImageUri       = logoImageUri?.toString(),
                documentImageUri   = documentImageUri?.toString()
            )

            viewModel.registerOrganization(
                data,
                password       = etPassword.text.toString().trim(),
                confirmPassword= etConfirm.text.toString().trim()
            )
        }
    }

    private fun observeViewModel() {
        viewModel.signupSuccess.observe(this) { success ->
            if (success) {
                Snackbar.make(
                    findViewById(R.id.root_layout),
                    "Organization registered!",
                    Snackbar.LENGTH_LONG
                ).show()
                finish()
            }
        }
        viewModel.signupError .observe(this) { err ->
            err?.let {
                Snackbar.make(
                    findViewById(R.id.root_layout),
                    it,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed(); return true
    }
}
