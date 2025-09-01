package com.ucb.pawapp.organization.ui.dashboard

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ucb.pawapp.R
import com.ucb.pawapp.organization.viewmodel.OrganizationPostListingViewModel
import com.ucb.pawapp.shared.model.AdoptionListing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrganizationPostListingFragment : Fragment() {

    private val vm: OrganizationPostListingViewModel by viewModels()

    // Views
    private lateinit var ivPetPhoto: ImageView
    private lateinit var btnSelectPhoto: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var btnPreview: Button
    private lateinit var btnPost: Button
    private lateinit var progress: ProgressBar

    private lateinit var spSpecies: Spinner
    private lateinit var spSize: Spinner
    private lateinit var spGender: Spinner
    private lateinit var spAge: Spinner

    private lateinit var etName: EditText
    private lateinit var etBreed: EditText
    private lateinit var etWeight: EditText
    private lateinit var etMedicalNotes: EditText
    private lateinit var etDescription: EditText
    private lateinit var etContactInfo: EditText
    private lateinit var etLocation: EditText

    private lateinit var cbSpayedNeutered: CheckBox
    private lateinit var cbVaccinated: CheckBox
    private lateinit var cbMicrochipped: CheckBox
    private lateinit var cbGoodWithKids: CheckBox
    private lateinit var cbGoodWithDogs: CheckBox
    private lateinit var cbGoodWithCats: CheckBox
    private lateinit var cbHouseTrained: CheckBox

    // Photo state
    private var photoUri: Uri? = null

    private val pickFromGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
            ivPetPhoto.setImageURI(uri)
        }
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) ivPetPhoto.setImageURI(photoUri) else photoUri = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(
        // Use your actual XML file name (yours is "activity_organization_post_listing")
        R.layout.activity_organization_post_listing,
        container,
        false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupSpinners()
        setupPhotoButtons()
        setupActionButtons()
    }

    private fun bindViews(root: View) {
        fun <T : View> View.req(@IdRes id: Int): T =
            findViewById<T>(id) ?: throw IllegalStateException(
                "Missing view ${resources.getResourceEntryName(id)} in activity_organization_post_listing.xml"
            )

        ivPetPhoto     = root.req(R.id.ivPetPhoto)
        btnSelectPhoto = root.req(R.id.btnSelectPhoto)
        btnTakePhoto   = root.req(R.id.btnTakePhoto)
        btnPreview     = root.req(R.id.btnPreview)
        btnPost        = root.req(R.id.btnPost)
        progress       = root.req(R.id.progress)

        spSpecies = root.req(R.id.spSpecies)
        spSize    = root.req(R.id.spSize)
        spGender  = root.req(R.id.spGender)
        spAge     = root.req(R.id.spAge)

        etName         = root.req(R.id.etName)
        etBreed        = root.req(R.id.etBreed)
        etWeight       = root.req(R.id.etWeight)
        etMedicalNotes = root.req(R.id.etMedicalNotes)
        etDescription  = root.req(R.id.etDescription)
        etContactInfo  = root.req(R.id.etContactInfo)
        etLocation     = root.req(R.id.etLocation)

        cbSpayedNeutered = root.req(R.id.cbSpayedNeutered)
        cbVaccinated     = root.req(R.id.cbVaccinated)
        cbMicrochipped   = root.req(R.id.cbMicrochipped)
        cbGoodWithKids   = root.req(R.id.cbGoodWithKids)
        cbGoodWithDogs   = root.req(R.id.cbGoodWithDogs)
        cbGoodWithCats   = root.req(R.id.cbGoodWithCats)
        cbHouseTrained   = root.req(R.id.cbHouseTrained)
    }

    private fun setupSpinners() {
        spSpecies.adapter = spinnerAdapterOf(listOf("dog", "cat"))
        spSize.adapter    = spinnerAdapterOf(listOf("small", "medium", "large"))
        spGender.adapter  = spinnerAdapterOf(listOf("male", "female"))
        spAge.adapter     = spinnerAdapterOf(listOf("0-1 year", "1-3 years", "3-7 years", "7+ years"))
    }

    private fun setupPhotoButtons() {
        btnSelectPhoto.setOnClickListener { pickFromGallery.launch("image/*") }
        btnTakePhoto.setOnClickListener {
            photoUri = createTempImageUri(requireContext())
            takePicture.launch(photoUri)
        }
    }

    private fun setupActionButtons() {
        btnPreview.setOnClickListener { showPreview() }
        btnPost.setOnClickListener { postListing() }
    }

    private fun showPreview() {
        val name = etName.text?.toString()?.trim().orEmpty()
        val species = spSpecies.selectedItem?.toString().orEmpty()
        val breed = etBreed.text?.toString()?.trim().orEmpty()
        val size = spSize.selectedItem?.toString().orEmpty()
        val age = spAge.selectedItem?.toString().orEmpty()
        val gender = spGender.selectedItem?.toString().orEmpty()
        val weight = etWeight.text?.toString()?.trim().orEmpty()
        val location = etLocation.text?.toString()?.trim().orEmpty()
        val contact = etContactInfo.text?.toString()?.trim().orEmpty()

        val healthStatus = buildString {
            if (cbVaccinated.isChecked) append("Vaccinated ")
            if (cbSpayedNeutered.isChecked) append("Spayed/Neutered ")
            if (cbMicrochipped.isChecked) append("Microchipped ")
        }.trim().ifEmpty { "Health status not specified" }

        val temperament = buildString {
            val traits = mutableListOf<String>()
            if (cbGoodWithKids.isChecked) traits.add("Good with kids")
            if (cbGoodWithDogs.isChecked) traits.add("Good with dogs")
            if (cbGoodWithCats.isChecked) traits.add("Good with cats")
            if (cbHouseTrained.isChecked) traits.add("House trained")
            append(if (traits.isEmpty()) "No temperament info specified" else traits.joinToString(", "))
        }

        val summary = """
            Pet: $name
            Species: $species ${if (breed.isNotEmpty()) "($breed)" else ""}
            Age: $age, Gender: $gender
            Size: $size ${if (weight.isNotEmpty()) ", Weight: $weight lbs" else ""}
            
            Health: $healthStatus
            Temperament: $temperament
            
            Location: $location
            Contact: $contact
        """.trimIndent()

        Toast.makeText(requireContext(), summary, Toast.LENGTH_LONG).show()
    }

    private fun postListing() {
        if (!validateInput()) return
        setBusy(true)

        lifecycleScope.launchWhenStarted {
            // 1) Upload photo to Firebase Storage (get HTTPS url)
            val httpsUrl = try {
                photoUri?.let { uploadPhotoToStorage(it) }
            } catch (e: Exception) {
                setBusy(false)
                Toast.makeText(requireContext(), "Photo upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                return@launchWhenStarted
            }

            // 2) Build the listing with HTTPS photoUrl
            val listing = AdoptionListing(
                species  = spSpecies.selectedItem.toString().lowercase(),
                size     = spSize.selectedItem.toString().lowercase(),
                name     = etName.text.toString().trim(),
                breed    = etBreed.text?.toString()?.trim()?.ifEmpty { null },
                photoUrl = httpsUrl, // <- IMPORTANT: permanent URL

                gender    = spGender.selectedItem?.toString()?.lowercase(),
                ageMonths = toMonths(spAge.selectedItem?.toString()),
                weightLbs = etWeight.text?.toString()?.trim()?.toDoubleOrNull(),

                spayedNeutered = cbSpayedNeutered.isChecked,
                vaccinated     = cbVaccinated.isChecked,
                microchipped   = cbMicrochipped.isChecked,

                goodWithKids = cbGoodWithKids.isChecked,
                goodWithDogs = cbGoodWithDogs.isChecked,
                goodWithCats = cbGoodWithCats.isChecked,
                houseTrained = cbHouseTrained.isChecked,

                medicalNotes = etMedicalNotes.text?.toString()?.trim()?.ifEmpty { null },
                description  = etDescription.text?.toString()?.trim()?.ifEmpty { null },
                contactInfo  = etContactInfo.text?.toString()?.trim()?.ifEmpty { null },
                location     = etLocation.text?.toString()?.trim()?.ifEmpty { null }
            )

            // 3) Save to RTDB via ViewModel
            vm.postListing(listing) { ok, err ->
                setBusy(false)
                if (ok) {
                    Toast.makeText(requireContext(), "Pet posted successfully!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to post: $err", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun uploadPhotoToStorage(localUri: Uri): String = withContext(Dispatchers.IO) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Not signed in")
        val filename = "adoption_photos/$uid/${System.currentTimeMillis()}.jpg"
        val ref = Firebase.storage.reference.child(filename)
        ref.putFile(localUri).await()
        ref.downloadUrl.await().toString() // HTTPS URL
    }

    private fun validateInput(): Boolean {
        var ok = true
        if (etName.text.isNullOrBlank())        { etName.error = "Pet name is required"; ok = false }
        if (etDescription.text.isNullOrBlank()) { etDescription.error = "Description is required"; ok = false }
        if (etContactInfo.text.isNullOrBlank()) { etContactInfo.error = "Contact is required"; ok = false }
        if (etLocation.text.isNullOrBlank())    { etLocation.error = "Location is required"; ok = false }
        if (photoUri == null) {
            Toast.makeText(requireContext(), "Please add a photo of the pet", Toast.LENGTH_SHORT).show()
            ok = false
        }
        return ok
    }

    private fun setBusy(b: Boolean) {
        progress.visibility = if (b) View.VISIBLE else View.GONE
        btnPost.isEnabled = !b
        btnPreview.isEnabled = !b
        btnSelectPhoto.isEnabled = !b
        btnTakePhoto.isEnabled = !b
    }

    private fun spinnerAdapterOf(items: List<String>) =
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

    private fun toMonths(label: String?): Int? = when (label) {
        "0-1 year"  -> 6
        "1-3 years" -> 24
        "3-7 years" -> 60
        "7+ years"  -> 108
        else -> null
    }

    private fun createTempImageUri(context: Context): Uri {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File.createTempFile("PAW_$ts", ".jpg", context.cacheDir)
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }
}
