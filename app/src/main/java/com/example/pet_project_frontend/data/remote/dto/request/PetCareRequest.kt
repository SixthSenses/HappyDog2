package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

// ===== 펫 케어 (Pet Care) Request =====

data class CareRecordCreateRequest(
    @SerializedName("record_type")
    val recordType: String, // "weight", "water", "activity", "meal"
    @SerializedName("timestamp")
    val timestamp: Long, // Unix timestamp in milliseconds
    @SerializedName("data")
    val data: Any, // Float for weight, Int for others
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("request_id")
    val requestId: String? = null
)

// PATCH 업데이트용: 부분 업데이트. 제공된 필드만 갱신
data class CareRecordUpdateRequest(
    @SerializedName("data")
    val data: Any? = null,
    @SerializedName("notes")
    val notes: String? = null, // 명시적으로 null 전달 시 서버에서 null로 저장
    @SerializedName("timestamp")
    val timestamp: Long? = null // ms
)
