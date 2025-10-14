package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * 일별 요약 + 목표 진행률 응답 DTO
 * UI의 오늘 달성률(%)/달성 여부 및 기본 레코드 목록에 대응
 */
data class DailySummaryResponse(
    @SerializedName("date")
    val date: String,
    
    @SerializedName("records")
    val records: List<CareRecordResponse>,  // CareRecord → CareRecordResponse
    
    @SerializedName("record_counts")
    val recordCounts: Map<String, Any>,  // 각 타입별 기록 개수

    @SerializedName("meal_count")
    val meal_count: Any?,

    @SerializedName("meta")
    val meta: Map<String, Any>,  // 메타 정보
    
    @SerializedName("goal_progress")
    val goalProgress: GoalProgress?  // 목표 진행률
)

/**
 * 목표 진행률 정보
 */
data class GoalProgress(
    @SerializedName("achievements")
    val achievements: Achievements?,
    
    @SerializedName("date")
    val date: String?
)

/**
 * 전체 달성 정보
 */
data class Achievements(
    @SerializedName("meal")
    val meal: AchievementInfo?,
    
    @SerializedName("activity")
    val activity: ActivityAchievementInfo?,
    
    @SerializedName("weight")
    val weight: WeightAchievementInfo?
)

/**
 * 일반 달성 정보 (식사)
 */
data class AchievementInfo(
    @SerializedName("actual")
    val actual: Any,  // 현재 값
    
    @SerializedName("goal")
    val goal: Any,  // 목표 값
    
    @SerializedName("percentage")
    val percentage: Float?,  // 달성률
    
    @SerializedName("achieved")
    val achieved: Boolean?  // 목표 달성 여부
)

/**
 * 활동 달성 정보
 */
data class ActivityAchievementInfo(
    @SerializedName("actual")
    val actual: Any,  // 현재 활동 시간
    
    @SerializedName("goal")
    val goal: Any,  // 목표 시간
    
    @SerializedName("percentage")
    val percentage: Float?,  // 달성률
    
    @SerializedName("achieved")
    val achieved: Boolean?,  // 목표 달성 여부
    
    @SerializedName("detail")
    val detail: ActivityDetail?  // 활동 상세 정보
)

/**
 * 활동 상세 정보
 */
data class ActivityDetail(
    @SerializedName("derived_goal_minutes")
    val derivedGoalMinutes: Int?,  // 계산된 목표 시간
    
    @SerializedName("minutes_per_session")
    val minutesPerSession: Int?,  // 1회 활동 시간
    
    @SerializedName("sessions")
    val sessions: Int?  // 목표 횟수
)

/**
 * 몸무게 달성 정보
 */
data class WeightAchievementInfo(
    @SerializedName("actual")
    val actual: Any,  // 현재 몸무게
    
    @SerializedName("goal")
    val goal: Any,  // 목표 몸무게
    
    @SerializedName("at_goal")
    val atGoal: Boolean?,  // 목표 달성 여부
    
    @SerializedName("diff")
    val diff: Float?  // 차이
)
