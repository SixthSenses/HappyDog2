package com.example.pet_project_frontend.data.remote.dto.eye_health

import com.example.pet_project_frontend.data.remote.dto.eye_health.EyeAnalysisPredictionDto
import com.google.gson.annotations.SerializedName

/**
 * 안구 분석 히스토리 아이템 DTO
 * GET /api/pets/eye-analyses 응답의 각 아이템
 */
data class EyeAnalysisHistoryItemDto(
    @SerializedName("analysis_id")
    val analysisId: String,
    @SerializedName("pet_id")
    val petId: String,
    @SerializedName("disease_name")
    val diseaseName: String,
    @SerializedName("created_at")
    val createdAt: String, // ISO 8601 UTC 형식
    @SerializedName("probability_percent")
    val probabilityPercent: Int,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("predictions")
    val predictions: List<EyeAnalysisPredictionDto>,
    @SerializedName("is_normal")
    val isNormal: Boolean
)