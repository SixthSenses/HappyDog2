package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.response.PetProfileResponse
import com.example.pet_project_frontend.data.remote.dto.response.PetViewBasedResponse
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.domain.model.Pet
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PetMapper {
    
    /**
     * PetViewBasedResponse를 Pet 도메인 모델로 변환
     * GET /api/pets/profile 엔드포인트의 응답 매핑
     */
    fun mapFromViewBasedResponse(dto: PetViewBasedResponse): Pet {
        val birthDate = runCatching {
            dto.birthdate?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
        }.getOrNull() ?: LocalDate.now()

        val gender = when (dto.gender?.uppercase()) {
            "MALE" -> Gender.MALE
            "FEMALE" -> Gender.FEMALE
            else -> Gender.UNKNOWN
        }

        return Pet(
            id = dto.petId,
            name = dto.name,
            breed = dto.breed ?: "",
            birthDate = birthDate,
            gender = gender,
            ownerId = "", // PetViewBasedResponse에는 user_id가 없으므로 빈 문자열
            isVerified = dto.isVerified,
            nosePrintUrl = null, // PetViewBasedResponse에는 없음
            profileImageUrl = dto.profileImageUrl,
            healthConcerns = emptyList() // PetViewBasedResponse에는 없음
        )
    }
    
    /**
     * PetProfileResponse를 Pet 도메인 모델로 변환
     * GET /api/pets/{petId} 엔드포인트의 응답 매핑
     */
    fun mapToDomainModel(dto: PetProfileResponse): Pet {
        val birthDate = runCatching {
            dto.birthdate?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
        }.getOrNull() ?: LocalDate.now()

        val gender = when (dto.gender?.lowercase()) {
            "male", "수컷" -> Gender.MALE
            "female", "암컷" -> Gender.FEMALE
            else -> Gender.UNKNOWN
        }

        return Pet(
            id = dto.petId,
            name = dto.name,
            breed = dto.breed ?: "",
            birthDate = birthDate,
            gender = gender,
            ownerId = dto.userId,
            isVerified = dto.isVerified,
            nosePrintUrl = dto.nosePrintUrl,
            profileImageUrl = dto.profileImageUrl, // 프로필 이미지 URL 매핑 추가
            healthConcerns = dto.healthConcerns ?: emptyList()
        )
    }
}
