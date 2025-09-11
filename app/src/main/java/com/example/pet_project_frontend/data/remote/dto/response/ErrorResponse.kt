package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * 서버 표준 에러 응답 모델 (API 명세 준수)
 * {
 *   "error_code": "ERROR_TYPE",
 *   "message": "설명",
 *   "details": { ... }
 * }
 */
data class ErrorResponse(
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("details") val details: Map<String, Any>? = null
)
