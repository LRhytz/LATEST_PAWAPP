// File: app/src/main/java/com/ucb/pawapp/citizen/viewmodel/CitizenAdoptionViewModel.kt
package com.ucb.pawapp.citizen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucb.pawapp.citizen.model.CitizenPreferences
import com.ucb.pawapp.citizen.repository.CitizenAdoptionRepository
import com.ucb.pawapp.citizen.repository.CitizenPreferenceRepository
import com.ucb.pawapp.shared.model.AdoptionListing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

class CitizenAdoptionViewModel : ViewModel() {

    private val adoptionRepo = CitizenAdoptionRepository()
    private val prefRepo = CitizenPreferenceRepository()

    private val _recommended = MutableStateFlow<List<AdoptionListing>>(emptyList())
    val recommended: StateFlow<List<AdoptionListing>> = _recommended

    private var prefs: CitizenPreferences = CitizenPreferences()
    private var listings: List<AdoptionListing> = emptyList()

    fun load() = viewModelScope.launch {
        prefs = prefRepo.loadPreferences()
        listings = adoptionRepo.loadListings()
        reRank()
    }

    private fun decayMap(map: Map<String, Double>, last: Long): Map<String, Double> {
        val now = System.currentTimeMillis()
        val days = ((now - last) / 86_400_000L).coerceAtLeast(0)
        val decay = Math.pow(0.99, days.toDouble())
        return map.mapValues { it.value * decay }
    }

    private fun reRank() {
        val tagW = decayMap(prefs.adoptionWeights, prefs.lastUpdated)
        val orgW = decayMap(prefs.orgAffinity, prefs.lastUpdated)
        val filterSpecies = prefs.preferredSpecies.toSet()
        val now = System.currentTimeMillis()

        _recommended.value = listings
            .filter { filterSpecies.isEmpty() || it.species in filterSpecies }
            .map { p ->
                val personal =
                    (tagW["species_${p.species.lowercase()}"] ?: 0.0) +
                            (tagW["size_${p.size.lowercase()}"] ?: 0.0) +
                            (p.breed?.let { tagW["breed_${it.lowercase()}"] } ?: 0.0)

                val ageDays = max(0.0, (now - p.createdAt) / 86_400_000.0)
                val recency = 1.0 / (1.0 + ageDays)
                val org = orgW[p.orgId] ?: 0.0
                val explore = Random.nextDouble(0.0, 1.0)

                val score = 0.45 * personal + 0.25 * recency + 0.20 * org + 0.10 * explore
                p to score
            }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    fun favorite(pet: AdoptionListing) = viewModelScope.launch {
        val tags = buildMap<String, Double> {
            put("species_${pet.species.lowercase()}", 0.35)
            put("size_${pet.size.lowercase()}", 0.35)
            pet.breed?.let { put("breed_${it.lowercase()}", 0.35) }
        }
        val orgs = mapOf(pet.orgId to 0.2)
        prefRepo.applyWeightDeltas(tags, orgs)

        // local bump for immediate UI feedback
        prefs = prefs.copy(
            adoptionWeights = prefs.adoptionWeights.toMutableMap().apply {
                tags.forEach { (k, v) -> this[k] = ((this[k] ?: 0.0) + v).coerceIn(-1.0, 3.0) }
            },
            orgAffinity = prefs.orgAffinity.toMutableMap().apply {
                orgs.forEach { (k, v) -> this[k] = ((this[k] ?: 0.0) + v).coerceIn(0.0, 2.0) }
            },
            lastUpdated = System.currentTimeMillis()
        )
        reRank()
    }

    fun viewedOrganization(orgId: String) = viewModelScope.launch {
        prefRepo.applyWeightDeltas(emptyMap(), mapOf(orgId to 0.1))
        prefs = prefs.copy(
            orgAffinity = prefs.orgAffinity.toMutableMap().apply {
                this[orgId] = ((this[orgId] ?: 0.0) + 0.1).coerceIn(0.0, 2.0)
            }
        )
        reRank()
    }
}
