package com.example.pet_project_frontend.domain.usecase.breed

import com.example.pet_project_frontend.data.remote.dto.response.BreedGuidebookResponse
import com.example.pet_project_frontend.domain.repository.BreedRepository
import javax.inject.Inject

class GetBreedGuidebookUseCase @Inject constructor(
    private val breedRepository: BreedRepository
) {
    suspend operator fun invoke(breedName: String): Result<BreedGuidebookResponse> {
        return breedRepository.getBreedGuidebook(breedName)
    }
}
