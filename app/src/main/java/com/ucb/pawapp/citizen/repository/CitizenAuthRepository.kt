// File: app/src/main/java/com/ucb/pawapp/citizen/repository/CitizenAuthRepository.kt
package com.ucb.pawapp.citizen.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ucb.pawapp.citizen.model.CitizenSignupData

class CitizenAuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")

    /**
     * Registers a new citizen, uploads optional avatar to Storage,
     * then writes their profile (with public download URL) into RTDB at /users/{uid}.
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

                fun writeUser(downloadUrl: String?) {
                    val userMap = mapOf(
                        "fullName"    to data.fullName,
                        "email"       to data.email,
                        "phone"       to data.phone,
                        "address"     to data.address,
                        "dateOfBirth" to data.dateOfBirth,
                        "photoUrl"    to downloadUrl,
                        "role"        to "citizen"
                    )
                    usersRef.child(uid)
                        .setValue(userMap)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                }

                val local = data.profileImageUri
                if (local.isNullOrBlank()) {
                    // No image picked
                    writeUser(null)
                } else {
                    // Upload to Storage, then save download URL
                    val storage = FirebaseStorage.getInstance()
                    val ref = storage.getReference("profile_photos/$uid.jpg")
                    ref.putFile(Uri.parse(local))
                        .continueWithTask { ref.downloadUrl }
                        .addOnSuccessListener { writeUser(it.toString()) }
                        .addOnFailureListener { onFailure(it) }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }
}
