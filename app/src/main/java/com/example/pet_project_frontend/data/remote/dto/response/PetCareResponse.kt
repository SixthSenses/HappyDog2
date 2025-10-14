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

// 일일 기록 응답 (DailyRecordsResponseSchema)
data class DailyRecordsResponse(
    @SerializedName("date")
    val date: String,
    @SerializedName("records")
    val records: List<CareRecordResponse>,
    @SerializedName("summary")
    val summary: Map<String, String>? = null
)

// 펫 케어 설정 관련 - API 스키마에 정확히 맞춤!
data class PetCareSettings(
    @SerializedName("target_weight")
    val targetWeight: Double,
    @SerializedName("target_daily_meal_count")
    val targetDailyMealCount: Int,
    @SerializedName("target_daily_activity_sessions")  // 1일 목표 활동 횟수 (예: 3회)
    val targetDailyActivitySessions: Int,
    @SerializedName("activity_session_minutes")  // 1회 활동 시간 (예: 30분)
    val activitySessionMinutes: Int,
    @SerializedName("target_daily_activity_minutes")  // 1일 총 목표 활동 시간 (서버 계산, 예: 90분)
    val targetDailyActivityMinutes: Int,
    @SerializedName("daily_meal_count")
    val dailyMealCount: Int? = null,
    @SerializedName("pet_id")
    val petId: String,
    @SerializedName("updated_at")
    val updatedAt: String
)