package com.example.pet_project_frontend.domain.usecase.pet

import com.example.pet_project_frontend.data.remote.dto.request.PetRegistrationRequest
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.repository.PetRepository
import javax.inject.Inject

class RegisterPetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(
        name: String,
        gender: String,
        breed: String,
        birthdate: String,
        currentWeight: Float,
        furColor: String?,
        healthConcerns: List<String>
    ): NetworkResult<Pet> {
        val request = PetRegistrationRequest(
            name = name,
            gender = gender,
            breed = breed,
            birthdate = birthdate,
            currentWeight = currentWeight,
            furColor = furColor,
            healthConcerns = healthConcerns
        )
        
        return petRepository.registerPet(request)
    }
}
