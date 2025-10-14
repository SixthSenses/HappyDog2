package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.data.remote.dto.response.BreedsResponse
import com.example.pet_project_frontend.data.remote.dto.response.BreedResponse
import com.example.pet_project_frontend.data.remote.dto.response.BreedGuidebookResponse

interface BreedRepository {
    suspend fun getAllBreeds(
        limit: Int? = null,
        offset: Int? = null,
        summary: Boolean = false
    ): Result<BreedsResponse>
    
    suspend fun searchBreeds(
        query: String,
        limit: Int = 10,
        offset: Int = 0
    ): Result<BreedsResponse>
    
    suspend fun getBreedByName(breedName: String): Result<BreedResponse>
    
    // 가이드북이 있는 품종만 조회
    suspend fun getGuidebookBreeds(): Result<BreedsResponse>
    
    // 특정 품종의 가이드북 정보 조회
    suspend fun getBreedGuidebook(breedName: String): Result<BreedGuidebookResponse>
}
