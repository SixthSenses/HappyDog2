package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.eye_health.EyeAnalysisHistoryItemDto
import com.example.pet_project_frontend.data.remote.dto.eye_health.EyeAnalysisHistoryResponseDto
import com.example.pet_project_frontend.data.remote.dto.eye_health.EyeAnalysisPredictionDto
import com.example.pet_project_frontend.domain.model.EyeAnalysisHistory
import com.example.pet_project_frontend.domain.model.EyeAnalysisHistoryItem
import com.example.pet_project_frontend.domain.model.EyeAnalysisPrediction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 안구 건강 히스토리 데이터 매퍼
 * 히스토리 관련 DTO ↔ Domain Model 변환만 담당
 */
object EyeHealthMapper {
    
    /**
     * EyeAnalysisHistoryResponseDto → EyeAnalysisHistory
     */
    fun EyeAnalysisHistoryResponseDto.toDomain(): EyeAnalysisHistory {
        return EyeAnalysisHistory(
            items = items.map { it.toDomain() },
            nextCursor = nextCursor
        )
    }
    
    /**
     * EyeAnalysisHistoryItemDto → EyeAnalysisHistoryItem
     */
    fun EyeAnalysisHistoryItemDto.toDomain(): EyeAnalysisHistoryItem {
        return EyeAnalysisHistoryItem(
            analysisId = analysisId,
            petId = petId,
            diseaseName = diseaseName,
            createdAt = parseDateTime(createdAt),
            probabilityPercent = probabilityPercent,
            imageUrl = imageUrl,
            predictions = predictions.map { prediction -> prediction.toDomainPrediction() },
            isNormal = isNormal
        )
    }
    
    /**
     * EyeAnalysisPredictionDto → EyeAnalysisPrediction (히스토리용)
     */
    private fun EyeAnalysisPredictionDto.toDomainPrediction(): EyeAnalysisPrediction {
        return EyeAnalysisPrediction(
            diseaseName = diseaseName,
            probability = probability,
            probabilityPercent = probabilityPercent
        )
    }
    
    /**
     * ISO 8601 UTC 형식 문자열을 LocalDateTime으로 변환
     * 예: "2024-01-15T10:30:00Z"
     */
    private fun parseDateTime(dateTimeString: String): LocalDateTime {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return LocalDateTime.parse(
            dateTimeString.removeSuffix("Z"), // Z 제거
            formatter
        )
    }
}