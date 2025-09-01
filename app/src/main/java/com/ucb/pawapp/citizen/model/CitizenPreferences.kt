package com.ucb.pawapp.citizen.model

data class CitizenPreferences(
    val preferredSpecies: List<String> = emptyList(),
    val receiveNotifications: Boolean = false,

    // dynamic weights for "Recommended for you"
    val adoptionWeights: Map<String, Double> = emptyMap(), // species_*, size_*, breed_*
    val articleWeights: Map<String, Double> = emptyMap(),  // reserved for Articles
    val orgAffinity: Map<String, Double> = emptyMap(),     // orgId -> weight
    val lastUpdated: Long = 0L,
    val onboardingCompleted: Boolean = false
)
