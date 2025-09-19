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

        val data = mutableMapOf<String, Any?>(
            "id"        to id,
            "orgId"     to uid,
            "species"   to input.species.lowercase(),
            "breed"     to input.breed?.lowercase(),
            "size"      to input.size.lowercase(),
            "name"      to input.name,
            "photoUrl"  to input.photoUrl,
            "createdAt" to ServerValue.TIMESTAMP
        )

        fun putIf(k: String, v: Any?) { if (v != null) data[k] = v }
        putIf("gender",         input.gender?.lowercase())
        putIf("ageMonths",      input.ageMonths)
        putIf("weightLbs",      input.weightLbs)
        putIf("spayedNeutered", input.spayedNeutered)
        putIf("vaccinated",     input.vaccinated)
        putIf("microchipped",   input.microchipped)
        putIf("goodWithKids",   input.goodWithKids)
        putIf("goodWithDogs",   input.goodWithDogs)
        putIf("goodWithCats",   input.goodWithCats)
        putIf("houseTrained",   input.houseTrained)
        putIf("medicalNotes",   input.medicalNotes)
        putIf("description",    input.description)
        putIf("contactInfo",    input.contactInfo)
        putIf("location",       input.location)

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

    /** Update an existing listing. Keeps createdAt/orgId, and fixes species index if changed. */
    suspend fun updateListing(updated: AdoptionListing) {
        require(updated.id.isNotBlank()) { "Missing id" }

        val ref = db.child("adoptions").child(updated.id)
        val snap = ref.get().await()
        if (!snap.exists()) error("Listing not found")

        val ownerId = snap.child("orgId").getValue(String::class.java)
        if (ownerId != uid) error("Not owner")

        val oldSpecies = snap.child("species").getValue(String::class.java)?.lowercase() ?: ""
        val newSpecies = updated.species.lowercase()

        val f = hashMapOf<String, Any?>(
            "/adoptions/${updated.id}/species"         to newSpecies,
            "/adoptions/${updated.id}/size"            to updated.size.lowercase(),
            "/adoptions/${updated.id}/name"            to updated.name,
            "/adoptions/${updated.id}/breed"           to updated.breed?.lowercase(),
            "/adoptions/${updated.id}/gender"          to updated.gender?.lowercase(),
            "/adoptions/${updated.id}/ageMonths"       to updated.ageMonths,
            "/adoptions/${updated.id}/weightLbs"       to updated.weightLbs,
            "/adoptions/${updated.id}/spayedNeutered"  to updated.spayedNeutered,
            "/adoptions/${updated.id}/vaccinated"      to updated.vaccinated,
            "/adoptions/${updated.id}/microchipped"    to updated.microchipped,
            "/adoptions/${updated.id}/goodWithKids"    to updated.goodWithKids,
            "/adoptions/${updated.id}/goodWithDogs"    to updated.goodWithDogs,
            "/adoptions/${updated.id}/goodWithCats"    to updated.goodWithCats,
            "/adoptions/${updated.id}/houseTrained"    to updated.houseTrained,
            "/adoptions/${updated.id}/medicalNotes"    to updated.medicalNotes,
            "/adoptions/${updated.id}/description"     to updated.description,
            "/adoptions/${updated.id}/contactInfo"     to updated.contactInfo,
            "/adoptions/${updated.id}/location"        to updated.location
        )

        // Only update photo if you passed a non-null value.
        if (updated.photoUrl != null) {
            f["/adoptions/${updated.id}/photoUrl"] = updated.photoUrl
        }

        // Fix species index if changed
        if (oldSpecies != newSpecies) {
            if (oldSpecies.isNotBlank()) {
                f["/adoptionsBySpecies/$oldSpecies/${updated.id}"] = null
            }
            f["/adoptionsBySpecies/$newSpecies/${updated.id}"] = true
        }

        db.updateChildren(f).await()
    }
}
