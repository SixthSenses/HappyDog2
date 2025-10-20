package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.Breed

/**
 * 품종(Breed) Repository 인터페이스
 */
interface BreedRepository {
    /**
     * 모든 품종 목록 조회
     */
    suspend fun getAllBreeds(): AppResult<List<Breed>>
    
    /**
     * 품종 검색
     * @param query 검색어
     */
    suspend fun searchBreeds(query: String): AppResult<List<Breed>>
}
