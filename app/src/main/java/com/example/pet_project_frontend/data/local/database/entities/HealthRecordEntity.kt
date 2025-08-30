package com.example.pet_project_frontend.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_records")
data class HealthRecordEntity(
    @PrimaryKey
    val id: String,
    val petId: String,
    val recordType: String,
    val value: String,
    val unit: String?,
    val date: String,
    val notes: String?,
    val createdAt: String
)
