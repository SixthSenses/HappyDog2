package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.response.PetProfileResponse
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.domain.model.Pet
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PetMapper {
    
    fun mapToDomainModel(dto: PetProfileResponse): Pet {
        return Pet(
            id = dto.petId,
            name = dto.name,
            breed = dto.breed,
            birthDate = LocalDate.parse(dto.birthdate, DateTimeFormatter.ISO_LOCAL_DATE),
            gender = when (dto.gender.lowercase()) {
                "male", "수컷" -> com.example.pet_project_frontend.domain.model.Gender.MALE
                "female", "암컷" -> com.example.pet_project_frontend.domain.model.Gender.FEMALE
                else -> com.example.pet_project_frontend.domain.model.Gender.UNKNOWN
            },
            weight = dto.initialWeight.toDouble(),
            ownerId = dto.userId,
            isVerified = dto.isVerified,
            nosePrintUrl = dto.nosePrintUrl,
            healthConcerns = dto.healthConcerns ?: emptyList()
        )
    }
}
