// File: app/src/main/java/com/ucb/pawapp/citizen/ui/report/ReportIncidentActivity.kt
package com.ucb.pawapp.citizen.ui.report

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ucb.pawapp.R
import com.ucb.pawapp.citizen.model.Incident

class ReportIncidentActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etLocation: EditText
    private lateinit var imagePreview: ImageView
    private lateinit var btnUploadImage: Button
    private lateinit var btnSubmit: Button

    private var selectedImageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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

        btnUploadImage.setOnClickListener { imagePicker.launch("image/*") }
        btnSubmit.setOnClickListener { submitReport() }
    }

    private fun submitReport() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val location = etLocation.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseDatabase.getInstance()
        val ref = db.getReference("incidents").child(uid)
        val id = ref.push().key ?: System.currentTimeMillis().toString()
        val now = System.currentTimeMillis()

        fun saveIncident(imageUrl: String?) {
            val incident = Incident(
                id = id,
                title = title,
                description = description,
                type = "Stray Animal",       // You can replace with a selector in the UI
                status = "NEW",
                location = location,
                latitude = 0.0,              // plug in real coords if you capture them
                longitude = 0.0,
                timestamp = now,
                reportedBy = uid,
                contactPhone = null,
                contactEmail = null,
                imageUrl = imageUrl,
                animalType = null,
                breed = null
            )
            ref.child(id).setValue(incident)
                .addOnSuccessListener {
                    Toast.makeText(this, "Report submitted successfully!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }

        val img = selectedImageUri
        if (img != null) {
            val storage = FirebaseStorage.getInstance()
            val sref = storage.getReference("incidents/$uid/$id.jpg")
            sref.putFile(img)
                .continueWithTask { sref.downloadUrl }
                .addOnSuccessListener { saveIncident(it.toString()) }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                    saveIncident(null) // still save the incident even if image fails
                }
        } else {
            saveIncident(null)
        }
    }
}
