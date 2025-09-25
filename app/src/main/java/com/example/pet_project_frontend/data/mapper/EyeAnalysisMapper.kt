package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.EyeAnalysisResponseDto
import com.example.pet_project_frontend.data.remote.dto.eye_health.EyeAnalysisPredictionDto
import com.example.pet_project_frontend.domain.model.EyeAnalysis
import com.example.pet_project_frontend.domain.model.EyeAnalysisPrediction

/**
 * 안구 분석 DTO를 Domain Model로 변환하는 Mapper (업데이트된 스펙)
 * Data Layer → Domain Layer 변환 담당
 */
object EyeAnalysisMapper {
    
    fun EyeAnalysisResponseDto.toDomain(): EyeAnalysis {
    return EyeAnalysis(
        analysisId = analysisId,
        diseaseName = diseaseName,
        probability = probability,
        probabilityPercent = probabilityPercent,
        imageUrl = imageUrl,
        predictions = predictions.map { it.toDomain() },
        isNormal = isNormal,
        riskLevel = EyeAnalysis.RiskLevel.fromAnalysis(isNormal, probability)
    )
}

    /**
     * 안구 분석 예측 DTO를 Domain Model로 변환
     */
    fun EyeAnalysisPredictionDto.toDomain(): EyeAnalysisPrediction {
        return EyeAnalysisPrediction(
            diseaseName = diseaseName,
            probability = probability,
            probabilityPercent = probabilityPercent
        )
    }
}