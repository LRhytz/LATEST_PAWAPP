package com.ucb.pawapp.citizen.ui.report

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ucb.pawapp.R

class ReportIncidentActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etLocation: EditText
    private lateinit var imagePreview: ImageView
    private lateinit var btnUploadImage: Button
    private lateinit var btnSubmit: Button

    private var selectedImageUri: Uri? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imagePreview.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_incident)

        etTitle = findViewById(R.id.et_title)
        etDescription = findViewById(R.id.et_description)
        etLocation = findViewById(R.id.et_location)
        imagePreview = findViewById(R.id.image_preview)
        btnUploadImage = findViewById(R.id.btn_upload_image)
        btnSubmit = findViewById(R.id.btn_submit)

        btnUploadImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            submitReport()
        }
    }

    private fun submitReport() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val location = etLocation.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Upload to Firebase (Firestore + Storage)
        Toast.makeText(this, "Report submitted successfully!", Toast.LENGTH_LONG).show()

        finish()
    }
}
