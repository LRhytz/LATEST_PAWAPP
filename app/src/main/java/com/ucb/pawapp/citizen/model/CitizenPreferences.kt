package com.ucb.pawapp.citizen.model

data class CitizenPreferences(
    val preferredSpecies: List<String> = emptyList(),
    val receiveNotifications: Boolean = false
)
