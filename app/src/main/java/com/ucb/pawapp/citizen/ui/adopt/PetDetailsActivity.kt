package com.ucb.pawapp.citizen.ui.adopt

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.ucb.pawapp.R
import com.ucb.pawapp.citizen.repository.FavoriteRepository
import com.ucb.pawapp.databinding.ActivityPetDetailsBinding
import java.text.SimpleDateFormat
import java.util.*

class PetDetailsActivity : AppCompatActivity() {

    companion object { const val EXTRA_PET_ID = "pet_id" }

    private lateinit var binding: ActivityPetDetailsBinding
    private lateinit var db: FirebaseDatabase
    private lateinit var adoptionsRef: DatabaseReference
    private lateinit var favRepo: FavoriteRepository

    private var petId: String? = null
    private var petListener: ValueEventListener? = null
    private var favListener: ValueEventListener? = null
    private var isFav: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityPetDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        petId = intent.getStringExtra(EXTRA_PET_ID)
        if (petId.isNullOrBlank()) {
            Toast.makeText(this, "Missing pet id", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        db = FirebaseDatabase.getInstance()
        adoptionsRef = db.reference.child("adoptions")
        favRepo = FavoriteRepository()

        setupToolbar()
        observePet(petId!!)
        observeFavorite(petId!!)

        binding.fabFavorite.setOnClickListener {
            val id = petId ?: return@setOnClickListener
            favRepo.setFavorite(this, id, !isFav)
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_SHORT).show()
                }
        }

        binding.btnAdopt.setOnClickListener {
            Toast.makeText(this, "Adoption flow coming soon ✨", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToolbar() = with(binding) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        collapsingToolbar.title = ""
    }

    private fun observePet(id: String) {
        val ref = adoptionsRef.child(id)
        petListener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val pet = snap.getValue(AdoptionPet::class.java) ?: return
                bindPet(pet)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PetDetailsActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        }
        ref.addValueEventListener(petListener!!)
    }

    private fun observeFavorite(id: String) {
        favListener = favRepo.observeFavoritePet(this, id) { fav ->
            isFav = fav
            binding.fabFavorite.setImageResource(
                if (fav) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp
            )
        }
    }

    private fun bindPet(p: AdoptionPet) = with(binding) {
        collapsingToolbar.title = p.name?.takeIf { it.isNotBlank() } ?: "Pet details"

        Glide.with(ivPetPhoto)
            .load(p.photoUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image)
            .into(ivPetPhoto)

        tvPetName.text = p.name ?: "Unnamed"
        tvPetBreed.text = p.breed ?: p.species ?: "—"
        tvPetSpecies.text = p.species ?: "—"

        tvPetAge.text = p.ageMonths?.let { monthsToPretty(it) } ?: "—"
        tvPetGender.text = p.gender ?: "—"
        tvPetSize.text = p.size ?: "—"
        tvPetWeight.text = p.weightLbs?.takeIf { it > 0 }?.let { "$it lbs" } ?: "—"

        tvSpayedNeutered.text = yesNo(p.spayedNeutered)
        tvVaccinated.text = yesNo(p.vaccinated)
        tvMicrochipped.text = yesNo(p.microchipped)
        tvHouseTrained.text = yesNo(p.houseTrained)

        tvGoodWithKids.text = yesNo(p.goodWithKids)
        tvGoodWithDogs.text = yesNo(p.goodWithDogs)
        tvGoodWithCats.text = yesNo(p.goodWithCats)

        tvDescription.text = p.description ?: "No description."
        tvMedicalNotes.text = p.medicalNotes ?: "—"

        tvContactInfo.text = p.contactInfo ?: "—"
        tvLocation.text = p.location ?: "—"
        tvCreatedDate.text = p.createdAt?.let {
            val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            "Listed on: ${fmt.format(Date(it))}"
        } ?: ""
    }

    private fun monthsToPretty(months: Int): String {
        val m = months.coerceAtLeast(0)
        val yrs = m / 12
        val rem = m % 12
        return when {
            yrs > 0 && rem > 0 -> "${yrs} ${if (yrs == 1) "yr" else "yrs"} ${rem} mo"
            yrs > 0 -> "${yrs} ${if (yrs == 1) "yr" else "yrs"}"
            else -> "$rem mo"
        }
    }

    private fun yesNo(b: Boolean?): String = if (b == true) "Yes" else "No"

    override fun onDestroy() {
        super.onDestroy()
        petId?.let { id ->
            petListener?.let { adoptionsRef.child(id).removeEventListener(it) }
            favRepo.removePetObserver(this, id, favListener)
        }
    }
}
