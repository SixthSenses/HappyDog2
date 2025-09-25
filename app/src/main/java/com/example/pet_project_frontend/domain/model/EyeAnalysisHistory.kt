package com.example.pet_project_frontend.domain.model

/**
 * 안구 분석 히스토리 응답 Domain Model
 * 페이지네이션 지원
 */
data class EyeAnalysisHistory(
    val items: List<EyeAnalysisHistoryItem>,
    val nextCursor: String?,
    val hasMore: Boolean = nextCursor != null
)