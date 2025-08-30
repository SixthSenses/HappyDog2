package com.example.pet_project_frontend.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val breed: String,
    val birthDate: String,
    val gender: String,
    val weight: Double,
    val ownerId: String,
    val createdAt: String,
    val updatedAt: String
)
