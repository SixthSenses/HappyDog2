package com.example.pet_project_frontend.presentation.model

/**
 * UI 표시 전용 반려동물 상태 모델
 * - 도메인 모델(Pet)을 직접 노출하지 않기 위해 사용
 */
data class PetUiState(
    val id: String,
    val name: String,
    val breed: String,
    val ageText: String? = null,
    val birthDateText: String? = null,
    val genderText: String? = null,
    val weightText: String? = null,
    val furColorText: String? = null,
    val profileImageUrl: String? = null
)
