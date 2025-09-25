package com.example.pet_project_frontend.data.remote.dto.eye_health

import com.google.gson.annotations.SerializedName

/**
 * 안구 분석 예측 결과 DTO
 * 각 질병별 확률 정보
 */
data class EyeAnalysisPredictionDto(
    @SerializedName("disease_name")
    val diseaseName: String,
    @SerializedName("probability")
    val probability: Float,
    @SerializedName("probability_percent")
    val probabilityPercent: Int
)