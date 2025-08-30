package com.example.pet_project_frontend.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
