package com.ucb.pawapp.shared.model

data class AdoptionListing(
    val id: String = "",
    val orgId: String = "",
    val species: String = "",            // "dog" | "cat"
    val breed: String? = null,
    val size: String = "",               // "small" | "medium" | "large"
    val name: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = 0L,            // set by server on post

    // NEW fields from the form (all optional)
    val gender: String? = null,          // "male" | "female"
    val ageMonths: Int? = null,          // approx in months
    val weightLbs: Double? = null,

    val spayedNeutered: Boolean? = null,
    val vaccinated: Boolean? = null,
    val microchipped: Boolean? = null,

    val goodWithKids: Boolean? = null,
    val goodWithDogs: Boolean? = null,
    val goodWithCats: Boolean? = null,
    val houseTrained: Boolean? = null,

    val medicalNotes: String? = null,
    val description: String? = null,
    val contactInfo: String? = null,
    val location: String? = null
)
