package com.ucb.pawapp.organization.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ucb.pawapp.organization.model.OrganizationSignupData

class OrganizationAuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    fun signupOrganization(
        data: OrganizationSignupData,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(data.email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                // Convert data to map + inject role
                val userMap = hashMapOf(
                    "organizationName" to data.organizationName,
                    "representativeName" to data.representativeName,
                    "email" to data.email,
                    "phone" to data.phone,
                    "address" to data.address,
                    "licenseNumber" to data.licenseNumber,
                    "organizationType" to data.organizationType,
                    "logoImageUri" to data.logoImageUri,
                    "documentImageUri" to data.documentImageUri,
                    "role" to "organization"
                )

                db.collection("users").document(uid)
                    .set(userMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
}
