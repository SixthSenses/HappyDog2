package com.example.pet_project_frontend.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

// Breed Entity
@Entity(
    tableName = "breeds",
    indices = [Index(value = ["breedName"], unique = true)]
)
data class BreedEntity(
    @PrimaryKey
    val breedName: String,
    val lifeExpectancy: Float,
    val heightMale: Float? = null,
    val heightFemale: Float? = null,
    val weightMale: Float? = null,
    val weightFemale: Float? = null,
    val cachedAt: LocalDateTime = LocalDateTime.now()
)