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
    val notes: String? = null
)
