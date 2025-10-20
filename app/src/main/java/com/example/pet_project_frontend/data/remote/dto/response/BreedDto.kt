package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * GET /api/breeds/ 응답 스키마
 * OpenAPI BreedListSchema
 */
data class BreedListResponse(
    @SerializedName("breeds")
    val breeds: List<BreedDto>,
    @SerializedName("total_count")
    val totalCount: Int
)

/**
 * 개별 품종 정보 스키마
 * OpenAPI BreedSchema
 */
data class BreedDto(
    @SerializedName("breed_name")
    val breedName: String,
    @SerializedName("life_expectancy")
    val lifeExpectancy: Float,
    @SerializedName("height_cm")
    val heightCm: HeightWeightRange,
    @SerializedName("weight_kg")
    val weightKg: HeightWeightRange,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

/**
 * 키/몸무게 범위 스키마
 */
data class HeightWeightRange(
    @SerializedName("min")
    val min: Float,
    @SerializedName("max")
    val max: Float
)

/**
 * 품종 존재 여부 확인 응답
 * OpenAPI BreedExistsResponseSchema
 */
data class BreedExistsResponse(
    @SerializedName("exists")
    val exists: Boolean,
    @SerializedName("breed_name")
    val breedName: String? = null
)
