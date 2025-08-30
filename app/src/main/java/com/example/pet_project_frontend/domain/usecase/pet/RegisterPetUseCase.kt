// ===== 1. Pet Registration UseCase =====
package com.example.pet_project_frontend.domain.usecase.pet

import com.example.pet_project_frontend.data.remote.dto.request.PetRegistrationRequest
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.repository.PetRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RegisterPetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(
        name: String,
        gender: Gender,
        breed: String,
        birthDate: LocalDate,
        currentWeight: Float,
        furColor: String? = null,
        healthConcerns: List<String> = emptyList()
    ): NetworkResult<Pet> {
        // 입력값 검증
        if (name.isBlank()) {
            return NetworkResult.Error(400, "반려동물 이름을 입력해주세요")
        }
        
        if (name.length > 20) {
            return NetworkResult.Error(400, "반려동물 이름은 20자 이내로 입력해주세요")
        }
        
        if (currentWeight < 0.1 || currentWeight > 200) {
            return NetworkResult.Error(400, "체중은 0.1kg ~ 200kg 사이로 입력해주세요")
        }
        
        if (birthDate.isAfter(LocalDate.now())) {
            return NetworkResult.Error(400, "생년월일이 올바르지 않습니다")
        }
        
        val request = PetRegistrationRequest(
            name = name.trim(),
            gender = when (gender) {
                Gender.MALE -> "MALE"
                Gender.FEMALE -> "FEMALE"
                Gender.UNKNOWN -> "MALE" // 기본값
            },
            breed = breed,
            birthdate = birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            currentWeight = currentWeight,
            furColor = furColor?.trim()?.takeIf { it.isNotEmpty() },
            healthConcerns = healthConcerns.filter { it.isNotBlank() }
        )
        
        return petRepository.registerPet(request)
    }
}