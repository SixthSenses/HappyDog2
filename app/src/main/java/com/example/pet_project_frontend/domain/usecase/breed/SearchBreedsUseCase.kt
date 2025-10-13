package com.example.pet_project_frontend.domain.usecase.breed

import com.example.pet_project_frontend.data.remote.dto.response.BreedResponse
import com.example.pet_project_frontend.domain.repository.BreedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchBreedsUseCase @Inject constructor(
    private val breedRepository: BreedRepository
) {
    operator fun invoke(query: String): Flow<List<BreedResponse>> = flow {
        val trimmed = query.trim()
        val result = if (trimmed.isBlank()) {
            breedRepository.getAllBreeds(limit = null)
        } else {
            breedRepository.searchBreeds(trimmed, limit = 50)
        }

        result
            .onSuccess { response -> emit(response.breeds) }
            .onFailure { emit(emptyList()) }
    }
}
