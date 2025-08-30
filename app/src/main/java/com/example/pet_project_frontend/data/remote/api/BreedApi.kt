package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.response.BreedsResponse
import com.example.pet_project_frontend.data.remote.dto.response.BreedResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BreedApi {
    @GET("api/breeds")
    suspend fun getAllBreeds(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("summary") summary: Boolean = false
    ): BreedsResponse
    
    @GET("api/breeds/search")
    suspend fun searchBreeds(
        @Query("q") query: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): BreedsResponse
    
    @GET("api/breeds/{breed_name}")
    suspend fun getBreedByName(
        @Query("breed_name") breedName: String
    ): BreedResponse
}
