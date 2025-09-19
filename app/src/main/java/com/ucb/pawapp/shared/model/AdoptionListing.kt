package com.ucb.pawapp.shared.model

import androidx.annotation.Keep
import java.io.Serializable

/**
 * Model for an adoption listing stored in RTDB.
 * - All fields have defaults so Firebase can deserialize it.
 * - Implements Serializable so you can pass it via Intent extras.
 */
@Keep
data class AdoptionListing(
    val id: String = "",
    val orgId: String = "",

    // Required core fields
    val species: String = "",            // "dog" | "cat"
    val size: String = "",               // "small" | "medium" | "large"
    val name: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = 0L,            // set by server on post

    // Optional details
    val breed: String? = null,
    val gender: String? = null,          // "male" | "female"
    val ageMonths: Int? = null,          // approx in months
    val weightLbs: Double? = null,

    // Health flags
    val spayedNeutered: Boolean? = null,
    val vaccinated: Boolean? = null,
    val microchipped: Boolean? = null,

    // Temperament flags
    val goodWithKids: Boolean? = null,
    val goodWithDogs: Boolean? = null,
    val goodWithCats: Boolean? = null,
    val houseTrained: Boolean? = null,

    // Text fields
    val medicalNotes: String? = null,
    val description: String? = null,
    val contactInfo: String? = null,
    val location: String? = null
) : Serializable
