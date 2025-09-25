package com.example.pet_project_frontend.data.remote.dto.eye_health

import com.google.gson.annotations.SerializedName

/**
 * 안구 분석 히스토리 응답 DTO
 * GET /api/pets/eye-analyses 응답 전체 구조
 */
data class EyeAnalysisHistoryResponseDto(
    @SerializedName("items")
    val items: List<EyeAnalysisHistoryItemDto>,
    @SerializedName("next_cursor")
    val nextCursor: String?
)