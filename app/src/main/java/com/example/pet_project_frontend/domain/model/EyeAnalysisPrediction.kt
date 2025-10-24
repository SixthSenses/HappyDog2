package com.example.pet_project_frontend.domain.model

/**
 * 안구 분석 예측 결과 Domain Model
 * 각 질병별 확률 정보
 */
data class EyeAnalysisPrediction(
    val diseaseName: String,
    val probability: Float,
    val probabilityPercent: Int
)