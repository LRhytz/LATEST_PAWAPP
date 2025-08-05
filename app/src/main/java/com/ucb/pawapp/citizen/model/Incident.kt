package com.ucb.pawapp.citizen.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Data class representing an animal incident report
 */
@Entity(tableName = "incidents")
data class Incident(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val status: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val reportedBy: String,
    val contactPhone: String?,
    val contactEmail: String?,
    val imageUrl: String?,
    val animalType: String?,
    val breed: String?
) : Serializable