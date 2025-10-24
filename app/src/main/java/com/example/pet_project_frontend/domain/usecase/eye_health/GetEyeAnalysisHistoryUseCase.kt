package com.example.pet_project_frontend.domain.usecase.eye_health

import com.example.pet_project_frontend.domain.model.EyeAnalysisHistory
import com.example.pet_project_frontend.domain.repository.EyeHealthRepository
import javax.inject.Inject

/**
 * 안구 분석 히스토리 조회 UseCase
 */
class GetEyeAnalysisHistoryUseCase @Inject constructor(
    private val eyeHealthRepository: EyeHealthRepository
) {
    /**
     * 안구 분석 히스토리 조회
     * @param petId 반려동물 ID (null이면 전체 조회)
     * @param limit 조회할 개수 (기본 20)
     * @param cursor 페이지네이션 커서
     */
    suspend operator fun invoke(
        petId: String? = null,
        limit: Int = 20,
        cursor: String? = null
    ): Result<EyeAnalysisHistory> {
        return eyeHealthRepository.getEyeAnalysisHistory(petId, limit, cursor)
    }
}