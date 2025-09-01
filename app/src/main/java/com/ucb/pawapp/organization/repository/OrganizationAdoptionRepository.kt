// File: app/src/main/java/com/ucb/pawapp/organization/repository/OrganizationAdoptionRepository.kt
package com.ucb.pawapp.organization.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ucb.pawapp.shared.model.AdoptionListing
import kotlinx.coroutines.tasks.await

class OrganizationAdoptionRepository {

    private val db = Firebase.database.reference
    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Org not logged in")

    suspend fun postListing(input: AdoptionListing): String {
        val id = db.child("adoptions").push().key ?: error("Failed to create key")

        // Required + common fields
        val data = mutableMapOf<String, Any?>(
            "id" to id,
            "orgId" to uid,
            "species" to input.species.lowercase(),
            "breed" to input.breed?.lowercase(),
            "size" to input.size.lowercase(),
            "name" to input.name,
            "photoUrl" to input.photoUrl,
            "createdAt" to ServerValue.TIMESTAMP
        )

        // Optional extras: only include if non-null
        fun putIf(k: String, v: Any?) { if (v != null) data[k] = v }
        putIf("gender", input.gender?.lowercase())
        putIf("ageMonths", input.ageMonths)
        putIf("weightLbs", input.weightLbs)

        putIf("spayedNeutered", input.spayedNeutered)
        putIf("vaccinated", input.vaccinated)
        putIf("microchipped", input.microchipped)

        putIf("goodWithKids", input.goodWithKids)
        putIf("goodWithDogs", input.goodWithDogs)
        putIf("goodWithCats", input.goodWithCats)
        putIf("houseTrained", input.houseTrained)

        putIf("medicalNotes", input.medicalNotes)
        putIf("description", input.description)
        putIf("contactInfo", input.contactInfo)
        putIf("location", input.location)

        val updates = hashMapOf<String, Any?>(
            "/adoptions/$id" to data,
            "/adoptionsBySpecies/${input.species.lowercase()}/$id" to true
        )
        db.updateChildren(updates).await()
        return id
    }

    suspend fun loadMyListings(limit: Int = 200): List<AdoptionListing> {
        val snap = db.child("adoptions")
            .orderByChild("orgId").equalTo(uid)
            .limitToLast(limit)
            .get().await()

        val list = mutableListOf<AdoptionListing>()
        for (c in snap.children) {
            c.getValue(AdoptionListing::class.java)?.let {
                list += it.copy(id = c.key ?: it.id)
            }
        }
        return list.sortedByDescending { it.createdAt }
    }

    suspend fun deleteListing(id: String) {
        val species = db.child("adoptions").child(id).child("species")
            .get().await().getValue(String::class.java)

        val updates = hashMapOf<String, Any?>("/adoptions/$id" to null)
        if (!species.isNullOrBlank()) {
            updates["/adoptionsBySpecies/${species.lowercase()}/$id"] = null
        }
        db.updateChildren(updates).await()
    }
}
