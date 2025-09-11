package com.example.pet_project_frontend.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val address: String,
    val shortAddress: String,
    val phoneNumber: String,
    val operateTime: String,
    val homePage: String
)
