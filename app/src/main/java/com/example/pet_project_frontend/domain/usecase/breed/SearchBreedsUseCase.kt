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
        if (query.isBlank()) {
            // 쿼리가 비어있으면 전체 목록 반환
            breedRepository.getAllBreeds(limit = 50)
                .onSuccess { response ->
                    emit(response.breeds)
                }
                .onFailure {
                    emit(emptyList())
                }
        } else {
            // 검색어가 있으면 검색 수행
            breedRepository.searchBreeds(query.trim(), limit = 50)
                .onSuccess { response ->
                    emit(response.breeds)
                }
                .onFailure {
                    emit(emptyList())
                }
        }
    }
}