package com.example.pet_project_frontend.domain.model

import java.time.LocalDate
import java.time.Period

data class Pet(
    val id: String,
    val name: String,
    val breed: String,
    val birthDate: LocalDate,
    val gender: Gender,
    val weight: Double,
    val ownerId: String,
    val isVerified: Boolean = false,
    val nosePrintUrl: String? = null,
    val healthConcerns: List<String> = emptyList()
) {
    // 비즈니스 로직: 나이 계산
    val age: Int
        get() = Period.between(birthDate, LocalDate.now()).years
    
    val ageDisplay: String
        get() = when {
            age == 0 -> "1살 미만"
            age == 1 -> "1살"
            else -> "${age}살"
        }
    
    // 비즈니스 로직: 성별 표시
    val genderDisplay: String
        get() = when (gender) {
            com.example.pet_project_frontend.domain.model.Gender.MALE -> "수컷"
            com.example.pet_project_frontend.domain.model.Gender.FEMALE -> "암컷"
            com.example.pet_project_frontend.domain.model.Gender.UNKNOWN -> "미상"
        }
    
    // 비즈니스 로직: 검증 상태 표시
    val verificationStatus: String
        get() = if (isVerified) "검증됨" else "미검증"
    
    // 비즈니스 로직: 건강 상태 요약
    val healthSummary: String
        get() = when {
            healthConcerns.isEmpty() -> "건강함"
            healthConcerns.size == 1 -> healthConcerns.first()
            else -> "${healthConcerns.first()} 외 ${healthConcerns.size - 1}개"
        }
}
