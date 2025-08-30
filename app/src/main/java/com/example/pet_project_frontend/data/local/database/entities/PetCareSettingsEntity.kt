package com.example.pet_project_frontend.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

// Pet Care Settings Entity
@Entity(
    tableName = "pet_care_settings",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["petId"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PetCareSettingsEntity(
    @PrimaryKey
    val petId: String,
    val goalWeight: Float,
    val waterBowlCapacity: Int = 500,
    val waterIncrementAmount: Int = 50,
    val goalActivityMinutes: Int = 30,
    val activityIncrementMinutes: Int = 5,
    val goalMealCount: Int = 2,
    val mealIncrementCount: Int = 1,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)