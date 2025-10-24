package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * 기간 요약 + 트렌드 + 목표 추적 응답 (From OpenAPI Spec)
 */
data class RangeSummaryResponse(
    @SerializedName("start_date")
    val startDate: String,

    @SerializedName("end_date")
    val endDate: String,

    /**
     * 날짜별 기록 요약.
     * OpenAPI 명세에 따라 'object' (Map) 타입으로 확정합니다.
     * Key: 날짜 (YYYY-MM-DD), Value: 해당 날짜의 기록 정보
     */
    // --- ▼▼▼ 최종 확정된 코드 (Map 사용) ▼▼▼ ---
    @SerializedName("records_by_date")
    val recordsByDate: Map<String, List<Map<String, Any>>>,

    @SerializedName("meta")
    val meta: Map<String, Any>? = null,

    @SerializedName("trends")
    val trends: Map<String, Any>? = null,

    @SerializedName("goal_tracking")
    val goalTracking: Map<String, Any>? = null
)

/**
 * 날짜별 목표 달성 정보를 추출하기 위한 헬퍼 함수
 */
fun RangeSummaryResponse.getAchievedDates(): Set<String> {
    val achievedDates = mutableSetOf<String>()

    goalTracking?.forEach { (date, value) ->
        if (value is Map<*, *>) {
            val achieved = value["achieved"] as? Boolean ?: false
            if (achieved) {
                achievedDates.add(date)
            }
        }
        else if (value is Boolean && value) {
            achievedDates.add(date)
        }
    }

    return achievedDates
}
