package com.example.pet_project_frontend.data.repository

import com.example.pet_project_frontend.data.remote.api.BreedApi
import com.example.pet_project_frontend.data.remote.dto.response.*
import com.example.pet_project_frontend.domain.repository.BreedRepository
import javax.inject.Inject

class BreedRepositoryImpl @Inject constructor(
    private val breedApi: BreedApi
) : BreedRepository {
    
    override suspend fun getAllBreeds(
        limit: Int?,
        offset: Int?,
        summary: Boolean
    ): Result<BreedsResponse> = runCatching {
        breedApi.getAllBreeds(limit, offset, summary)
    }
    
    override suspend fun searchBreeds(
        query: String,
        limit: Int,
        offset: Int
    ): Result<BreedsResponse> = runCatching {
        breedApi.searchBreeds(query, limit, offset)
    }
    
    override suspend fun getBreedByName(breedName: String): Result<BreedResponse> = runCatching {
        breedApi.getBreedByName(breedName)
    }
}
