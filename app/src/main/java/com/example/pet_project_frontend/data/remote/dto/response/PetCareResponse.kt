package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// ===== 펫 케어 (Pet Care) Response =====

data class CareRecordResponse(
    @SerializedName("log_id")
    val logId: String,
    @SerializedName("pet_id")
    val petId: String,
    @SerializedName("record_type")
    val recordType: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("data")
    val data: Any,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("searchDate")
    val searchDate: String
)

data class CareRecordsResponse(
    @SerializedName("records")
    val records: List<CareRecordResponse>,
    @SerializedName("meta")
    val meta: RecordsMeta,
    @SerializedName("grouped")
    val grouped: Map<String, List<CareRecordResponse>>? = null
)

data class RecordsMeta(
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("has_more")
    val hasMore: Boolean,
    @SerializedName("next_cursor")
    val nextCursor: String?
)

// 펫 케어 설정 관련
data class PetCareSettings(
    @SerializedName("goalWeight")
    val goalWeight: Float,
    @SerializedName("waterBowlCapacity")
    val waterBowlCapacity: Int,
    @SerializedName("waterIncrementAmount")
    val waterIncrementAmount: Int,
    @SerializedName("goalActivityMinutes")
    val goalActivityMinutes: Int,
    @SerializedName("activityIncrementMinutes")
    val activityIncrementMinutes: Int,
    @SerializedName("goalMealCount")
    val goalMealCount: Int,
    @SerializedName("mealIncrementCount")
    val mealIncrementCount: Int
)
