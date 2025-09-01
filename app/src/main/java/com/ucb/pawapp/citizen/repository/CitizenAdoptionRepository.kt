package com.ucb.pawapp.citizen.repository

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ucb.pawapp.shared.model.AdoptionListing
import kotlinx.coroutines.tasks.await

class CitizenAdoptionRepository {
    private val db = Firebase.database.reference

    suspend fun loadListings(limit: Int = 200): List<AdoptionListing> {
        val snap = db.child("adoptions").limitToLast(limit).get().await()
        val items = mutableListOf<AdoptionListing>()
        for (c in snap.children) {
            c.getValue(AdoptionListing::class.java)?.let { items += it.copy(id = c.key ?: it.id) }
        }
        return items.sortedByDescending { it.createdAt }
    }
}
