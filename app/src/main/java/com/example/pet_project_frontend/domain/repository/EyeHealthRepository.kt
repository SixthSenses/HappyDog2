package com.example.pet_project_frontend.domain.repository

import android.net.Uri
import com.example.pet_project_frontend.domain.model.EyeAnalysis
import com.example.pet_project_frontend.domain.model.EyeAnalysisHistory

/**
 * 안구 건강 분석 Repository Interface (Domain Layer)
 * Domain Layer에서 Data Layer 추상화
 */
interface EyeHealthRepository {
    /**
     * 안구 이미지 분석 요청
     * @param petId 반려동물 ID
     * @param imageUri 분석할 이미지 URI (Firebase 업로드 후 URL)
     * @return 분석 결과 또는 에러
     */
    suspend fun analyzeEyeHealth(petId: String, imageUri: Uri): Result<EyeAnalysis>
    
    /**
     * 안구 분석 히스토리 조회
     * @param petId 반려동물 ID (선택적)
     * @param limit 조회할 개수 (기본 20, 최대 50)
     * @param cursor 페이지네이션 커서
     * @return 히스토리 목록 또는 에러
     */
    suspend fun getEyeAnalysisHistory(
        petId: String? = null,
        limit: Int = 20,
        cursor: String? = null
    ): Result<EyeAnalysisHistory>
}
