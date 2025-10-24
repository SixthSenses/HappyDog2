package com.example.pet_project_frontend.domain.model

import java.time.LocalDateTime

/**
 * 안구 분석 히스토리 아이템 Domain Model
 */
data class EyeAnalysisHistoryItem(
    val analysisId: String,
    val petId: String,
    val diseaseName: String,
    val createdAt: LocalDateTime,
    val probabilityPercent: Int,
    val imageUrl: String?,
    val predictions: List<EyeAnalysisPrediction>,
    val isNormal: Boolean
) {
    /**
     * 날짜 표시용 텍스트 (예: "2024.01.15")
     */
    val displayDate: String
        get() = "${createdAt.year}.${createdAt.monthValue.toString().padStart(2, '0')}.${createdAt.dayOfMonth.toString().padStart(2, '0')}"
    
    /**
     * 시간 표시용 텍스트 (예: "10:30")
     */
    val displayTime: String
        get() = "${createdAt.hour.toString().padStart(2, '0')}:${createdAt.minute.toString().padStart(2, '0')}"
}