package com.example.pet_project_frontend.domain.model

/**
 * 견종 정보 도메인 모델
 */
data class Breed(
    val id: String,
    val name: String,
    val koreanName: String,
    val size: String, // Small, Medium, Large
    val origin: String,
    val lifeSpan: String,
    val temperament: String,
    val description: String,
    val imageUrl: String? = null,
    val weight: String,
    val height: String,
    val careLevel: String, // Easy, Moderate, High
    val exerciseNeeds: String, // Low, Medium, High
    val groomingNeeds: String // Low, Medium, High
)