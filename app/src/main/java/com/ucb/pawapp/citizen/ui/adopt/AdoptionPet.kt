package com.ucb.pawapp.citizen.ui.adopt

data class AdoptionPet(
    var id: String? = null,
    var orgId: String? = null,
    var species: String? = null,
    var size: String? = null,
    var name: String? = null,
    var breed: String? = null,
    var photoUrl: String? = null,
    var createdAt: Long? = null,
    var gender: String? = null,
    var ageMonths: Int? = null,
    var weightLbs: Int? = null,
    var spayedNeutered: Boolean? = null,
    var vaccinated: Boolean? = null,
    var microchipped: Boolean? = null,
    var goodWithKids: Boolean? = null,
    var goodWithDogs: Boolean? = null,
    var goodWithCats: Boolean? = null,
    var houseTrained: Boolean? = null,
    var medicalNotes: String? = null,
    var description: String? = null,
    var contactInfo: String? = null,
    var location: String? = null
)
