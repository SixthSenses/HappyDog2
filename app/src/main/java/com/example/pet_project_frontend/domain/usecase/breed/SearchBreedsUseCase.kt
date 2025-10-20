package com.example.pet_project_frontend.domain.usecase.breed

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.Breed
import com.example.pet_project_frontend.domain.repository.BreedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchBreedsUseCase @Inject constructor(
    private val breedRepository: BreedRepository
) {
    operator fun invoke(query: String): Flow<List<Breed>> = flow {
        val trimmed = query.trim()
        val result = if (trimmed.isBlank()) {
            breedRepository.getAllBreeds()
        } else {
            breedRepository.searchBreeds(trimmed)
        }

        when (result) {
            is AppResult.Success -> emit(result.data)
            is AppResult.Error -> emit(emptyList())
            is AppResult.Exception -> emit(emptyList())
        }
    }
}
