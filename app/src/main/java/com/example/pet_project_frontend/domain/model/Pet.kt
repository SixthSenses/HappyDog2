package com.example.pet_project_frontend.domain.model

import java.time.LocalDate
import java.time.Period

data class Pet(
    val id: String,
    val name: String,
    val breed: String,
    val birthDate: LocalDate,
    val gender: Gender,
    val weight: Float,
    val ownerId: String,
    val isVerified: Boolean = false,
    val nosePrintUrl: String? = null,
    val healthConcerns: List<String> = emptyList()
) {
    // UI 표현 책임 제거: 필요한 경우 ViewModel에서 가공하십시오.
}
