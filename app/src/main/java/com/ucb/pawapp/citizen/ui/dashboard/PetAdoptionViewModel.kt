package com.ucb.pawapp.citizen.ui.dashboard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucb.pawapp.citizen.repository.CitizenAdoptionRepository
import com.ucb.pawapp.citizen.repository.CitizenPreferenceRepository
import com.ucb.pawapp.shared.model.AdoptionListing
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.random.Random

// --- UI model (kept simple to match your screen) ---
data class Pet(
    val id: String,
    val name: String,
    val type: String,          // "Dog" | "Cat"
    val breed: String,
    val age: String,           // (we don't have age in listing; show "—" or relative)
    val location: String,      // "Org: <orgId>" for now
    val adoptionFee: String,   // "—" (not present in listing)
    val imageUrl: String
)

class PetAdoptionViewModel : ViewModel() {

    private val adoptionRepo = CitizenAdoptionRepository()
    private val prefRepo     = CitizenPreferenceRepository()

    private val _pets = mutableStateOf<List<Pet>>(emptyList())
    val pets: State<List<Pet>> = _pets

    private val _favorites = mutableStateOf(setOf<String>())
    val favorites: State<Set<String>> = _favorites

    // cached
    private var listings: List<AdoptionListing> = emptyList()
    private var adoptionWeights: Map<String, Double> = emptyMap()
    private var orgAffinity: Map<String, Double> = emptyMap()

    init {
        refresh()
    }

    /** Loads prefs + listings and recomputes recommendations. */
    fun refresh() {
        viewModelScope.launch {
            // Load preferences
            val prefs = prefRepo.loadPreferences()
            adoptionWeights = prefs.adoptionWeights
            orgAffinity     = prefs.orgAffinity

            // Load latest listings
            listings = adoptionRepo.loadListings(limit = 200)

            // Score + sort
            val now = System.currentTimeMillis()
            val ranked = listings
                .map { it to score(it, now) }
                .sortedByDescending { it.second }
                .map { it.first }

            // Map to UI Pet model
            _pets.value = ranked.map { it.toUi() }
        }
    }

    /** Score a single listing. Larger is better. */
    private fun score(a: AdoptionListing, now: Long): Double {
        var s = 0.0

        // Learned weights from userPrefs.adoptionWeights
        s += (adoptionWeights["species_${a.species.lowercase()}"] ?: 0.0) * 1.0
        s += (adoptionWeights["size_${a.size.lowercase()}"] ?: 0.0) * 0.7
        a.breed?.takeIf { it.isNotBlank() }?.let {
            s += (adoptionWeights["breed_${it.lowercase()}"] ?: 0.0) * 0.6
        }

        // Affinity to organization
        s += (orgAffinity[a.orgId] ?: 0.0) * 0.3

        // Recency: ~1% decay per day old
        val days = if (a.createdAt > 0) ((now - a.createdAt).coerceAtLeast(0L) / 86_400_000L) else 0L
        val recency = 0.99.pow(days.toDouble())
        s *= recency

        // Tiny exploration noise so ties shuffle a bit
        s += Random.nextDouble(0.0, 0.01)

        return s
    }

    /** Called when user taps ❤ (favorite/save) on a listing. Teaches the model. */
    fun toggleFavorite(petId: String) {
        // Toggle UI state
        _favorites.value = if (_favorites.value.contains(petId)) {
            _favorites.value - petId
        } else {
            _favorites.value + petId
        }

        // Find listing for learning
        val item = listings.firstOrNull { it.id == petId } ?: return

        // Bump relevant weights and org affinity
        val deltas = buildMap<String, Double> {
            put("species_${item.species.lowercase()}", 0.30)
            put("size_${item.size.lowercase()}", 0.20)
            item.breed?.takeIf { it.isNotBlank() }?.let { put("breed_${it.lowercase()}", 0.25) }
        }
        viewModelScope.launch {
            prefRepo.applyWeightDeltas(
                tagDeltas = deltas,
                orgDeltas = mapOf(item.orgId to 0.15)
            )
            // reload weights + re-rank
            refresh()
        }
    }

    /** Optional: call this when user opens an org profile or spends time on it. */
    fun onViewedOrganization(orgId: String) {
        viewModelScope.launch {
            prefRepo.applyWeightDeltas(tagDeltas = emptyMap(), orgDeltas = mapOf(orgId to 0.10))
            refresh()
        }
    }

    // --- mapping ---
    private fun AdoptionListing.toUi(): Pet = Pet(
        id = id,
        name = name.ifBlank { (species.ifBlank { "Pet" }).replaceFirstChar { it.uppercase() } },
        type = species.replaceFirstChar { it.uppercase() },
        breed = (breed ?: "Mixed"),
        age = "—", // no age field in your listing; keep as dash
        location = "Org: $orgId",
        adoptionFee = "—",
        imageUrl = photoUrl.orEmpty()
    )
}
