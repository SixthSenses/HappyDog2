package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.response.PetProfileResponse
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.domain.model.Pet
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PetMapper {
    
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
