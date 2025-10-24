package com.example.pet_project_frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 생체 분석 요청 DTO
 * OpenAPI BiometricAnalysisRequestSchema 매핑
 */
data class BiometricAnalysisRequestDto(
    @SerializedName("file_path")
    val filePath: String
)