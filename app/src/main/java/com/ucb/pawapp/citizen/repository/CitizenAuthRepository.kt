// File: app/src/main/java/com/ucb/pawapp/citizen/repository/CitizenAuthRepository.kt
package com.ucb.pawapp.citizen.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ucb.pawapp.citizen.model.CitizenSignupData

class CitizenAuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase
        .getInstance()
        .getReference("users")

    /**
     * Registers a new citizen, then writes their profile (including chosen image URI)
     * under /users/{uid} in the Realtime Database.
     */
    fun signupCitizen(
        data: CitizenSignupData,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(data.email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val userMap = mapOf(
                    "fullName"    to data.fullName,
                    "email"       to data.email,
                    "phone"       to data.phone,
                    "address"     to data.address,
                    "dateOfBirth" to data.dateOfBirth,
                    "photoUrl"    to data.profileImageUri, // could be null
                    "role"        to "citizen"
                )

                usersRef.child(uid)
                    .setValue(userMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
}
