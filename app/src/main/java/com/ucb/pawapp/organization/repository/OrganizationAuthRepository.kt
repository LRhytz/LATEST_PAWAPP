package com.ucb.pawapp.organization.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ucb.pawapp.organization.model.OrganizationSignupData

class OrganizationAuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().reference

    /**
     * Sign up an organization account, then write its profile under /users/{uid} in RTDB.
     */
    fun signupOrganization(
        data: OrganizationSignupData,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(data.email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid.isNullOrBlank()) {
                    onFailure(Exception("UID was null"))
                    return@addOnSuccessListener
                }

                // Build the map you want in RTDB:
                val userMap = mapOf(
                    "organizationName"   to data.organizationName,
                    "representativeName" to data.representativeName,
                    "email"              to data.email,
                    "phone"              to data.phone,
                    "address"            to data.address,
                    "licenseNumber"      to data.licenseNumber,
                    "organizationType"   to data.organizationType,
                    "logoImageUri"       to (data.logoImageUri ?: ""),
                    "documentImageUri"   to (data.documentImageUri ?: ""),
                    "role"               to "organization"
                )

                // Write to RTDB:
                dbRef.child("users")
                    .child(uid)
                    .setValue(userMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e) }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    /**
     * Log in, then verify that /users/{uid}/role == "organization" in RTDB.
     */
    fun loginOrganization(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid.isNullOrBlank()) {
                    onFailure("User ID not found")
                    return@addOnSuccessListener
                }

                // Read /users/{uid}/role:
                dbRef.child("users")
                    .child(uid)
                    .child("role")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val role = snapshot.getValue(String::class.java)?.lowercase()
                        if (role == "organization") {
                            onSuccess()
                        } else {
                            auth.signOut()
                            onFailure("Access denied: not an organization account.")
                        }
                    }
                    .addOnFailureListener { e ->
                        onFailure("Failed to fetch role: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onFailure("Login failed: ${e.message}")
            }
    }
}
