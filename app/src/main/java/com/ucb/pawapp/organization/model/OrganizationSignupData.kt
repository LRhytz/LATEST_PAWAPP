package com.ucb.pawapp.organization.model

data class OrganizationSignupData(
    val organizationName: String,
    val representativeName: String,
    val email: String,
    val phone: String,
    val address: String,
    val licenseNumber: String,
    val organizationType: String,
    val logoImageUri: String? = null,
    val documentImageUri: String? = null,
    val role: String = "organization" // 👈 explicitly set role
)
