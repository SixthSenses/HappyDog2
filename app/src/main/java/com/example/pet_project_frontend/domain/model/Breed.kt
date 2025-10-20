package com.example.pet_project_frontend.domain.model

/**
 * 품종 도메인 모델
 */
data class Breed(
    val breedName: String,
    val lifeExpectancy: Float,
    val heightMin: Float,
    val heightMax: Float,
    val weightMin: Float,
    val weightMax: Float
)
