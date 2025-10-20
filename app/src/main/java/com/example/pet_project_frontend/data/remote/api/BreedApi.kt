package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.response.BreedListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 품종(Breed) 관련 API 인터페이스
 * OpenAPI /api/breeds 엔드포인트
 */
interface BreedApi {
    
    /**
     * 품종 목록 조회
     * GET /api/breeds/
     */
    @GET("api/breeds/")
    suspend fun getBreedList(): Response<BreedListResponse>
    
    /**
     * 품종 검색
     * GET /api/breeds/search?q={query}
     */
    @GET("api/breeds/search")
    suspend fun searchBreeds(
        @Query("q") query: String
    ): Response<BreedListResponse>
}
