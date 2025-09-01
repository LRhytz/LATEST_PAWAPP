// app/src/main/java/com/ucb/pawapp/organization/ui/dashboard/OrganizationPetDetailsActivity.kt
package com.ucb.pawapp.organization.ui.dashboard

import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ucb.pawapp.R
import com.ucb.pawapp.shared.model.AdoptionListing

class OrganizationPetDetailsActivity : AppCompatActivity() {

    private lateinit var ivPhoto: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvSpeciesChip: TextView
    private lateinit var tvAgeGender: TextView
    private lateinit var tvBreedSize: TextView
    private lateinit var badgesRow: LinearLayout
    private lateinit var tvDescription: TextView
    private lateinit var tvMedical: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvContact: TextView
    private lateinit var tvTime: TextView
    private lateinit var progress: ProgressBar
    private lateinit var progressCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_pet_details)
        bindViews()

        val id = intent.getStringExtra(EXTRA_LISTING_ID)
        if (id.isNullOrBlank()) {
            Toast.makeText(this, "Missing listing id", Toast.LENGTH_LONG).show()
            finish(); return
        }
        loadListing(id)
    }

    private fun bindViews() {
        ivPhoto      = findViewById(R.id.ivPhoto)
        tvName       = findViewById(R.id.tvName)
        tvSpeciesChip= findViewById(R.id.tvSpeciesChip)
        tvAgeGender  = findViewById(R.id.tvAgeGender)
        tvBreedSize  = findViewById(R.id.tvBreedSize)
        badgesRow    = findViewById(R.id.badgesRow)
        tvDescription= findViewById(R.id.tvDescription)
        tvMedical    = findViewById(R.id.tvMedical)
        tvLocation   = findViewById(R.id.tvLocation)
        tvContact    = findViewById(R.id.tvContact)
        tvTime       = findViewById(R.id.tvTime)
        progress     = findViewById(R.id.progress)
        progressCard = findViewById(R.id.progressCard)
    }

    private fun loadListing(id: String) {
        setBusy(true)
        Firebase.database.reference.child("adoptions").child(id)
            .get()
            .addOnSuccessListener { snap ->
                setBusy(false)
                val listing = snap.getValue(AdoptionListing::class.java)
                if (listing == null) {
                    Toast.makeText(this, "Listing not found", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    setupUi(listing.copy(id = id))
                }
            }
            .addOnFailureListener { e ->
                setBusy(false)
                Toast.makeText(this, "Failed to load: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun setupUi(l: AdoptionListing) {
        // Photo
        val placeholder = R.drawable.gray_rect
        val url = l.photoUrl
        var loaded = false
        if (!url.isNullOrBlank()) {
            when (Uri.parse(url).scheme?.lowercase()) {
                "http", "https" -> {
                    Glide.with(this).load(url)
                        .centerCrop()
                        .placeholder(placeholder)
                        .error(placeholder)
                        .into(ivPhoto)
                    loaded = true
                }
                "content", "file" -> {
                    try { ivPhoto.setImageURI(Uri.parse(url)); loaded = true } catch (_: SecurityException) {}
                }
            }
        }
        if (!loaded) ivPhoto.setImageResource(placeholder)

        // Texts
        tvName.text = l.name.ifBlank { "Unnamed" }
        tvSpeciesChip.text = l.species.uppercase()

        val age = l.ageMonths?.let { monthsToLabel(it) } ?: "—"
        val gender = l.gender?.replaceFirstChar { it.titlecase() } ?: "—"
        tvAgeGender.text = "$age • $gender"

        val breed = l.breed?.replaceFirstChar { it.titlecase() }
        val size  = l.size.replaceFirstChar { it.titlecase() }
        tvBreedSize.text = listOfNotNull(breed, size).joinToString(" • ").ifBlank { "—" }

        tvDescription.text = l.description?.takeIf { it.isNotBlank() } ?: "No description."
        tvMedical.text = buildMedical(l).ifBlank { "No medical notes." }

        tvLocation.text = l.location?.takeIf { it.isNotBlank() }?.let { "📍 $it" } ?: "📍 —"
        tvContact.text  = l.contactInfo?.takeIf { it.isNotBlank() }?.let { "Contact: $it" } ?: "Contact: —"
        tvTime.text     = if (l.createdAt > 0) DateUtils.getRelativeTimeSpanString(l.createdAt).toString() else ""

        renderBadges(l)
    }

    private fun renderBadges(l: AdoptionListing) {
        badgesRow.removeAllViews()
        fun add(text: String) {
            val tv = TextView(this).apply {
                setPadding(20, 10, 20, 10)
                textSize = 12f
                setTextColor(0xFF2E7D32.toInt())
                background = resources.getDrawable(R.drawable.bg_chip_green, theme)
                setText(text)
                elevation = 2f
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 16, 0) }
            badgesRow.addView(tv, lp)
        }
        if (l.vaccinated == true) add("Vaccinated")
        if (l.spayedNeutered == true) add("Spayed/Neutered")
        if (l.microchipped == true) add("Microchipped")
        if (l.goodWithKids == true) add("Good w/ kids")
        if (l.goodWithDogs == true) add("Good w/ dogs")
        if (l.goodWithCats == true) add("Good w/ cats")
        if (l.houseTrained == true) add("House trained")
    }

    private fun buildMedical(l: AdoptionListing): String = listOfNotNull(
        l.medicalNotes?.takeIf { it.isNotBlank() }
    ).joinToString("\n")

    private fun monthsToLabel(m: Int): String =
        if (m < 12) "$m ${if (m == 1) "month" else "months"}"
        else {
            val y = m / 12
            "$y ${if (y == 1) "year" else "years"}"
        }

    private fun setBusy(b: Boolean) {
        progressCard.visibility = if (b) View.VISIBLE else View.GONE
    }

    companion object {
        const val EXTRA_LISTING_ID = "extra_listing_id"
    }
}