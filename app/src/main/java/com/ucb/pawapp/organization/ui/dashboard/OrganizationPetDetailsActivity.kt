package com.ucb.pawapp.organization.ui.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ucb.pawapp.R
import com.ucb.pawapp.shared.model.AdoptionListing
import java.io.Serializable

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
    private var contentRoot: View? = null
    private var loaded: AdoptionListing? = null   // <-- keep the item for FAB edit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_pet_details)
        bindViews()

        // Prefer a full object
        val listingFromExtra: AdoptionListing? =
            (intent.getSerializableExtra(EXTRA_LISTING) as? AdoptionListing)
                ?: (intent.getSerializableExtra(EXTRA_LISTING_FALLBACK) as? AdoptionListing)
                ?: intent.getParcelableExtra(EXTRA_LISTING)
                ?: intent.getParcelableExtra(EXTRA_LISTING_FALLBACK)

        if (listingFromExtra != null) {
            setBusy(false)
            setupUi(listingFromExtra)
        } else {
            // Fallback: by id
            val id: String? =
                intent.getStringExtra(EXTRA_LISTING_ID)
                    ?: intent.getStringExtra("listingId")
                    ?: intent.data?.lastPathSegment
            if (id.isNullOrBlank()) {
                Toast.makeText(this, "Nothing to show (no listing provided)", Toast.LENGTH_LONG).show()
                finish(); return
            }
            loadListing(id)
        }

        // Edit FAB
        findViewById<View>(R.id.fabEdit)?.setOnClickListener {
            val l = loaded
            if (l != null && l.id.isNotBlank()) {
                startActivity(
                    Intent(this, OrganizationEditListingActivity::class.java)
                        .putExtra(OrganizationEditListingActivity.EXTRA_LISTING_ID, l.id)
                )
            } else {
                Toast.makeText(this, "Listing not ready yet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindViews() {
        ivPhoto       = findViewById(R.id.ivPhoto)
        tvName        = findViewById(R.id.tvName)
        tvSpeciesChip = findViewById(R.id.tvSpeciesChip)
        tvAgeGender   = findViewById(R.id.tvAgeGender)
        tvBreedSize   = findViewById(R.id.tvBreedSize)
        badgesRow     = findViewById(R.id.badgesRow)
        tvDescription = findViewById(R.id.tvDescription)
        tvMedical     = findViewById(R.id.tvMedical)
        tvLocation    = findViewById(R.id.tvLocation)
        tvContact     = findViewById(R.id.tvContact)
        tvTime        = findViewById(R.id.tvTime)
        progress      = findViewById(R.id.progress)
        setBusy(true)
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
        loaded = l  // <-- keep for FAB edit

        // Photo
        val placeholder = R.drawable.gray_rect
        val url = l.photoUrl
        var shown = false
        if (!url.isNullOrBlank()) {
            when (Uri.parse(url).scheme?.lowercase()) {
                "http", "https" -> {
                    Glide.with(this).load(url)
                        .centerCrop()
                        .placeholder(placeholder)
                        .error(placeholder)
                        .into(ivPhoto)
                    shown = true
                }
                "content", "file" -> {
                    try { ivPhoto.setImageURI(Uri.parse(url)); shown = true } catch (_: SecurityException) {}
                }
            }
        }
        if (!shown) ivPhoto.setImageResource(placeholder)

        tvName.text = l.name.ifBlank { "Unnamed" }
        tvSpeciesChip.text = l.species.ifBlank { "—" }.uppercase()

        val age = l.ageMonths?.let { monthsToLabel(it) } ?: "—"
        val gender = l.gender?.replaceFirstChar { it.titlecase() } ?: "—"
        tvAgeGender.text = "$age • $gender"

        val breed = l.breed?.replaceFirstChar { it.titlecase() }
        val size  = l.size.replaceFirstChar { it.titlecase() }
        tvBreedSize.text = listOfNotNull(breed, size).joinToString(" • ").ifBlank { "—" }

        tvDescription.text = l.description?.takeIf { it.isNotBlank() } ?: "No description."
        tvMedical.text     = l.medicalNotes?.takeIf { it.isNotBlank() } ?: "No medical notes."
        tvLocation.text    = l.location?.takeIf { it.isNotBlank() }?.let { "📍 $it" } ?: "📍 —"
        tvContact.text     = l.contactInfo?.takeIf { it.isNotBlank() }?.let { "Contact: $it" } ?: "Contact: —"
        tvTime.text        = if (l.createdAt > 0) DateUtils.getRelativeTimeSpanString(l.createdAt).toString() else ""

        badgesRow.removeAllViews()
        fun addBadge(label: String) {
            val tv = TextView(this).apply {
                setPadding(16, 6, 16, 6)
                textSize = 12f
                text = label
                setTextColor(0xFF2E7D32.toInt())
                background = ResourcesCompat.getDrawable(resources, R.drawable.bg_chip_green, theme)
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 12, 0) }
            badgesRow.addView(tv, lp)
        }
        if (l.vaccinated == true) addBadge("Vaccinated")
        if (l.spayedNeutered == true) addBadge("Spayed/Neutered")
        if (l.microchipped == true) addBadge("Microchipped")
        if (l.goodWithKids == true) addBadge("Good w/ kids")
        if (l.goodWithDogs == true) addBadge("Good w/ dogs")
        if (l.goodWithCats == true) addBadge("Good w/ cats")
        if (l.houseTrained == true) addBadge("House trained")
    }

    private fun monthsToLabel(m: Int): String =
        if (m < 12) "$m ${if (m == 1) "month" else "months"}"
        else {
            val y = m / 12
            "$y ${if (y == 1) "year" else "years"}"
        }

    private fun setBusy(b: Boolean) {
        progress.visibility = if (b) View.VISIBLE else View.GONE
        contentRoot?.visibility = if (b) View.INVISIBLE else View.VISIBLE
    }

    companion object {
        const val EXTRA_LISTING_ID       = "extra_listing_id"
        const val EXTRA_LISTING          = "extra_listing"
        const val EXTRA_LISTING_FALLBACK = "extra_listing_fallback"

        fun createIntent(context: Context, listing: AdoptionListing): Intent =
            Intent(context, OrganizationPetDetailsActivity::class.java).apply {
                putExtra(EXTRA_LISTING, listing as Serializable)
            }

        fun createIntent(context: Context, listingId: String): Intent =
            Intent(context, OrganizationPetDetailsActivity::class.java).apply {
                putExtra(EXTRA_LISTING_ID, listingId)
            }
    }
}
