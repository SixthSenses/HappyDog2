package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.BiometricAnalysisRequestDto
import com.example.pet_project_frontend.data.remote.dto.EyeAnalysisResponseDto
import com.example.pet_project_frontend.data.remote.dto.eye_health.EyeAnalysisHistoryResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 안구 건강 분석 API 인터페이스
 * - POST /api/pets/{pet_id}/eye-analysis: 안구 분석 실행
 * - GET /api/pets/eye-analyses: 안구 분석 히스토리 조회
 */
interface EyeHealthApi {
    
    /**
     * 안구 분석 실행
     */
    @POST("api/pets/{pet_id}/eye-analysis")
    suspend fun analyzeEyeHealth(
        @Path("pet_id") petId: String,
        @Body request: BiometricAnalysisRequestDto
    ): Response<EyeAnalysisResponseDto>
    
    /**
     * 안구 분석 히스토리 조회
     */
    @GET("api/pets/eye-analyses")
    suspend fun getEyeAnalysisHistory(
        @Query("pet_id") petId: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null
    ): Response<EyeAnalysisHistoryResponseDto>
}