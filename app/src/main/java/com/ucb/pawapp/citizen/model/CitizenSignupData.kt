package com.ucb.pawapp.citizen.model

data class CitizenSignupData(
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val dateOfBirth: String,
    val profileImageUri: String? = null,
    val role: String = "citizen" // 👈 explicitly set role
)
