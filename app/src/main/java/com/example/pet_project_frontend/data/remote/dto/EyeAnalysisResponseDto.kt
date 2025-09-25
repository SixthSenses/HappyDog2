package com.example.pet_project_frontend.data.remote.dto

import com.example.pet_project_frontend.data.remote.dto.eye_health.EyeAnalysisPredictionDto
import com.google.gson.annotations.SerializedName

/**
 * 안구 분석 응답 DTO (업데이트된 스펙)
 * 새로운 API 응답 구조 매핑
 */
data class EyeAnalysisResponseDto(
    @SerializedName("analysis_id")
    val analysisId: String,
    @SerializedName("disease_name")
    val diseaseName: String,
    @SerializedName("probability")
    val probability: Float,
    @SerializedName("probability_percent")
    val probabilityPercent: Int,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("predictions")
    val predictions: List<EyeAnalysisPredictionDto>,
    @SerializedName("is_normal")
    val isNormal: Boolean
)