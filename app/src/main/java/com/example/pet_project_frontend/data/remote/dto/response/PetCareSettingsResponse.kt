package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PetCareSettingsResponse(
    @SerializedName("pet_id")
    val petId: String,
    @SerializedName("goalWeight")
    val goalWeight: Double,
    @SerializedName("goalActivityMinutes")
    val goalActivityMinutes: Int,
    @SerializedName("goalMealCount")
    val goalMealCount: Int,
    @SerializedName("activityIncrementMinutes")
    val activityIncrementMinutes: Int,
    @SerializedName("mealIncrementCount")
    val mealIncrementCount: Int,
    @SerializedName("waterBowlCapacity")
    val waterBowlCapacity: Int,
    @SerializedName("waterIncrementAmount")
    val waterIncrementAmount: Int,
    @SerializedName("updated_at")
    val updatedAt: String
)
