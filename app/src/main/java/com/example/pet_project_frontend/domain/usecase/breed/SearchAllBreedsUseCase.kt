package com.example.pet_project_frontend.domain.usecase.breed

import com.example.pet_project_frontend.data.remote.dto.response.BreedResponse
import javax.inject.Inject

class SearchAllBreedsUseCase @Inject constructor(
    private val getAllBreedsUseCase: GetAllBreedsUseCase
) {
    suspend operator fun invoke(query: String): List<BreedResponse> {
        return try {
            val allBreeds = getAllBreedsUseCase()
            
            if (query.isBlank()) {
                allBreeds
            } else {
                allBreeds.filter { breed ->
                    breed.breedName.contains(query.trim(), ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}