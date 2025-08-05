package com.ucb.pawapp.citizen.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ucb.pawapp.citizen.model.CitizenPreferences

class CitizenPreferenceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun savePreferences(
        preferences: CitizenPreferences,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userId?.let {
            db.collection("users").document(it)
                .set(preferences)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        }
    }

    fun loadPreferences(
        onSuccess: (CitizenPreferences?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userId?.let {
            db.collection("users").document(it)
                .get()
                .addOnSuccessListener { doc ->
                    val prefs = doc.toObject(CitizenPreferences::class.java)
                    onSuccess(prefs)
                }
                .addOnFailureListener { onFailure(it) }
        }
    }
}
