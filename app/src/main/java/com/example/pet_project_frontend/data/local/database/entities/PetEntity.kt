package com.example.pet_project_frontend.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

// Pet Entity
@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey
    val petId: String,
    val userId: String,
    val name: String,
    val gender: String, // "MALE" or "FEMALE"
    val breed: String,
    val birthDate: LocalDate,
    val currentWeight: Float,
    val furColor: String? = null,
    val healthConcerns: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val nosePrintUrl: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
