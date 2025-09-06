package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// ===== 품종 정보 (Breeds) Response =====

data class BreedsResponse(
    @SerializedName("breeds")
    val breeds: List<BreedResponse>,
    @SerializedName("total_count")
    val totalCount: Int
)

data class BreedResponse(
    @SerializedName("breed_name")
    val breedName: String,
    @SerializedName("life_expectancy")
    val lifeExpectancy: Float,
    @SerializedName("height_cm")
    val heightCm: HeightWeightInfo,
    @SerializedName("weight_kg")
    val weightKg: HeightWeightInfo,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class HeightWeightInfo(
    @SerializedName("male")
    val male: Float?,
    @SerializedName("female")
    val female: Float?
)

data class BreedSummaryResponse(
    @SerializedName("breed_name")
    val breedName: String,
    @SerializedName("life_expectancy")
    val lifeExpectancy: Float
)
