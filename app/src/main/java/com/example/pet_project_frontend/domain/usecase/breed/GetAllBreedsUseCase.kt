package com.example.pet_project_frontend.domain.usecase.breed

import com.example.pet_project_frontend.data.remote.dto.response.BreedResponse
import com.example.pet_project_frontend.domain.repository.BreedRepository
import javax.inject.Inject

class GetAllBreedsUseCase @Inject constructor(
    private val breedRepository: BreedRepository
) {
    suspend operator fun invoke(): List<BreedResponse> {
        return try {
            breedRepository.getAllBreeds(summary = false)
                .fold(
                    onSuccess = { response ->
                        response.breeds
                    },
                    onFailure = { error ->
                        emptyList()
                    }
                )
        } catch (e: Exception) {
            emptyList()
        }
    }
}