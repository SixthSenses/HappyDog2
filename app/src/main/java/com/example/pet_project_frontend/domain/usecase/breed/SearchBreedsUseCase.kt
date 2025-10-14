package com.example.pet_project_frontend.domain.usecase.breed

import com.example.pet_project_frontend.data.remote.dto.response.BreedResponse
import com.example.pet_project_frontend.domain.repository.BreedRepository
import javax.inject.Inject

class SearchBreedsUseCase @Inject constructor(
    private val breedRepository: BreedRepository,
    private val getGuidebookBreedsUseCase: GetGuidebookBreedsUseCase
) {
    suspend operator fun invoke(query: String): List<BreedResponse> {
        return try {
            // 가이드북 품종 목록을 가져옴
            val guidebookBreeds = getGuidebookBreedsUseCase()
            
            if (query.isBlank()) {
                // 쿼리가 비어있으면 가이드북 품종 전체 반환
                guidebookBreeds
            } else {
                // 검색어가 있으면 가이드북 품종에서 클라이언트 사이드 검색
                guidebookBreeds.filter { breed ->
                    breed.breedName.contains(query.trim(), ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}