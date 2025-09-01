// File: app/src/main/java/com/ucb/pawapp/organization/model/OrganizationSignupData.kt
package com.ucb.pawapp.organization.model

data class OrganizationSignupData(
    val id: String = "", // Unique identifier (Firebase UID or generated ID)
    val organizationName: String = "",
    val representativeName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val licenseNumber: String = "",
    val organizationType: String = "",
    val logoImageUri: String? = null,
    val documentImageUri: String? = null,
    val role: String = "organization", // explicitly set role
    val createdAt: Long = System.currentTimeMillis(), // When the organization was created
    val updatedAt: Long = System.currentTimeMillis(), // When last updated
    val isVerified: Boolean = false, // Whether the organization is verified
    val status: String = "pending" // Status: "pending", "approved", "rejected", "active", "inactive"
) {
    // No-argument constructor for Firebase
    constructor() : this(
        id = "",
        organizationName = "",
        representativeName = "",
        email = "",
        phone = "",
        address = "",
        licenseNumber = "",
        organizationType = "",
        logoImageUri = null,
        documentImageUri = null,
        role = "organization",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        isVerified = false,
        status = "pending"
    )
}