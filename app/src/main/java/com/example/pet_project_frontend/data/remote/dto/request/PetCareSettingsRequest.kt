package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class PetCareSettingsRequest(
    @SerializedName("goalWeight")
    val goalWeight: Double? = null,
    @SerializedName("goalActivityMinutes")
    val goalActivityMinutes: Int? = null,
    @SerializedName("goalMealCount")
    val goalMealCount: Int? = null,
    @SerializedName("activityIncrementMinutes")
    val activityIncrementMinutes: Int? = null,
    @SerializedName("mealIncrementCount")
    val mealIncrementCount: Int? = null,
    @SerializedName("waterBowlCapacity")
    val waterBowlCapacity: Int? = null,
    @SerializedName("waterIncrementAmount")
    val waterIncrementAmount: Int? = null
)
