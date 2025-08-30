package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.data.remote.dto.response.BreedsResponse
import com.example.pet_project_frontend.data.remote.dto.response.BreedResponse

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
}
