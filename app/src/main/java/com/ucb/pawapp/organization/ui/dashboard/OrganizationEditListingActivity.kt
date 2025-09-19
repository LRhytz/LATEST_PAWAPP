package com.ucb.pawapp.organization.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ucb.pawapp.R
import com.ucb.pawapp.organization.repository.OrganizationAdoptionRepository
import com.ucb.pawapp.shared.model.AdoptionListing
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrganizationEditListingActivity : AppCompatActivity() {

    private lateinit var ivPetPhoto: ImageView
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

    private val repo = OrganizationAdoptionRepository()
    private var current: AdoptionListing? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Reuse the create form layout
        setContentView(R.layout.activity_organization_post_listing)

        bindViews()
        setupSpinners()

        // Avoid relying on a string resource that may not exist
        btnPost.text = "Save"

        val id = intent.getStringExtra(EXTRA_LISTING_ID)
        if (id.isNullOrBlank()) {
            Toast.makeText(this, "Missing listing id", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        load(id)
        btnPost.setOnClickListener { save() }
    }

    private fun bindViews() {
        ivPetPhoto       = findViewById(R.id.ivPetPhoto)
        btnPost          = findViewById(R.id.btnPost)
        progress         = findViewById(R.id.progress)

        spSpecies        = findViewById(R.id.spSpecies)
        spSize           = findViewById(R.id.spSize)
        spGender         = findViewById(R.id.spGender)
        spAge            = findViewById(R.id.spAge)

        etName           = findViewById(R.id.etName)
        etBreed          = findViewById(R.id.etBreed)
        etWeight         = findViewById(R.id.etWeight)
        etMedicalNotes   = findViewById(R.id.etMedicalNotes)
        etDescription    = findViewById(R.id.etDescription)
        etContactInfo    = findViewById(R.id.etContactInfo)
        etLocation       = findViewById(R.id.etLocation)

        cbSpayedNeutered = findViewById(R.id.cbSpayedNeutered)
        cbVaccinated     = findViewById(R.id.cbVaccinated)
        cbMicrochipped   = findViewById(R.id.cbMicrochipped)
        cbGoodWithKids   = findViewById(R.id.cbGoodWithKids)
        cbGoodWithDogs   = findViewById(R.id.cbGoodWithDogs)
        cbGoodWithCats   = findViewById(R.id.cbGoodWithCats)
        cbHouseTrained   = findViewById(R.id.cbHouseTrained)
    }

    private fun setupSpinners() {
        fun adapter(items: List<String>) =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, items).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        spSpecies.adapter = adapter(listOf("dog", "cat"))
        spSize.adapter    = adapter(listOf("small", "medium", "large"))
        spGender.adapter  = adapter(listOf("male", "female"))
        spAge.adapter     = adapter(listOf("0-1 year", "1-3 years", "3-7 years", "7+ years"))
    }

    private fun setBusy(b: Boolean) {
        progress.visibility = if (b) View.VISIBLE else View.GONE
        btnPost.isEnabled = !b
    }

    private fun load(id: String) {
        setBusy(true)
        lifecycleScope.launch {
            try {
                val snap = Firebase.database.reference.child("adoptions").child(id).get().await()
                val l = snap.getValue(AdoptionListing::class.java)
                if (l == null) {
                    Toast.makeText(this@OrganizationEditListingActivity, "Not found", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    current = l.copy(id = id)
                    fillForm(current!!)
                }
            } finally {
                setBusy(false)
            }
        }
    }

    private fun fillForm(l: AdoptionListing) {
        fun select(sp: Spinner, value: String?, options: List<String>) {
            val i = options.indexOf(value ?: "")
            if (i >= 0) sp.setSelection(i)
        }

        etName.setText(l.name)
        etBreed.setText(l.breed ?: "")
        etWeight.setText(l.weightLbs?.toString() ?: "")
        etMedicalNotes.setText(l.medicalNotes ?: "")
        etDescription.setText(l.description ?: "")
        etContactInfo.setText(l.contactInfo ?: "")
        etLocation.setText(l.location ?: "")

        select(spSpecies, l.species, listOf("dog","cat"))
        select(spSize,    l.size,    listOf("small","medium","large"))
        select(spGender,  l.gender,  listOf("male","female"))
        select(
            spAge,
            when (l.ageMonths) {
                null      -> null
                in 0..12  -> "0-1 year"
                in 13..36 -> "1-3 years"
                in 37..84 -> "3-7 years"
                else      -> "7+ years"
            },
            listOf("0-1 year","1-3 years","3-7 years","7+ years")
        )

        cbSpayedNeutered.isChecked = l.spayedNeutered == true
        cbVaccinated.isChecked     = l.vaccinated == true
        cbMicrochipped.isChecked   = l.microchipped == true
        cbGoodWithKids.isChecked   = l.goodWithKids == true
        cbGoodWithDogs.isChecked   = l.goodWithDogs == true
        cbGoodWithCats.isChecked   = l.goodWithCats == true
        cbHouseTrained.isChecked   = l.houseTrained == true
    }

    private fun toMonths(label: String?): Int? = when (label) {
        "0-1 year"  -> 6
        "1-3 years" -> 24
        "3-7 years" -> 60
        "7+ years"  -> 108
        else -> null
    }

    private fun save() {
        val base = current ?: return
        val updated = base.copy(
            species        = spSpecies.selectedItem.toString().lowercase(),
            size           = spSize.selectedItem.toString().lowercase(),
            name           = etName.text.toString().trim(),
            breed          = etBreed.text?.toString()?.trim()?.ifEmpty { null },
            gender         = spGender.selectedItem?.toString()?.lowercase(),
            ageMonths      = toMonths(spAge.selectedItem?.toString()),
            weightLbs      = etWeight.text?.toString()?.trim()?.toDoubleOrNull(),
            spayedNeutered = cbSpayedNeutered.isChecked,
            vaccinated     = cbVaccinated.isChecked,
            microchipped   = cbMicrochipped.isChecked,
            goodWithKids   = cbGoodWithKids.isChecked,
            goodWithDogs   = cbGoodWithDogs.isChecked,
            goodWithCats   = cbGoodWithCats.isChecked,
            houseTrained   = cbHouseTrained.isChecked,
            medicalNotes   = etMedicalNotes.text?.toString()?.trim()?.ifEmpty { null },
            description    = etDescription.text?.toString()?.trim()?.ifEmpty { null },
            contactInfo    = etContactInfo.text?.toString()?.trim()?.ifEmpty { null },
            location       = etLocation.text?.toString()?.trim()?.ifEmpty { null }
        )

        setBusy(true)
        lifecycleScope.launch {
            try {
                repo.updateListing(updated)
                Toast.makeText(this@OrganizationEditListingActivity, "Saved", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@OrganizationEditListingActivity, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setBusy(false)
            }
        }
    }

    companion object {
        const val EXTRA_LISTING_ID = "extra_listing_id"
    }
}
