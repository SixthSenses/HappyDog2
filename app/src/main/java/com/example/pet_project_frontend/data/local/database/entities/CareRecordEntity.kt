package com.example.pet_project_frontend.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

// Care Record Entity
@Entity(
    tableName = "care_records",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["petId"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["petId"]),
        Index(value = ["recordType"]),
        Index(value = ["searchDate"]),
        Index(value = ["timestamp"])
    ]
)
data class CareRecordEntity(
    @PrimaryKey
    val logId: String,
    val petId: String,
    val recordType: String, // "weight", "water", "activity", "meal"
    val data: Double,
    val timestamp: Long, // Unix timestamp in milliseconds
    val searchDate: String, // "YYYY-MM-DD" format for efficient date queries
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)