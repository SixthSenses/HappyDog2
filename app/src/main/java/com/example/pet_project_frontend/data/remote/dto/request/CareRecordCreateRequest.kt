package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * 케어 기록 생성 요청 DTO
 */
data class CareRecordCreateRequest(
    @SerializedName("record_type")
    val recordType: String,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("data")
    val data: Any,
    
    @SerializedName("memo")
    val memo: String? = null
)
