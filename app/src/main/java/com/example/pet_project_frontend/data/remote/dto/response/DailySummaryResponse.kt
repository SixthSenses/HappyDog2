package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * 일별 요약 + 목표 진행률 응답 DTO
 * UI의 오늘 달성률(%)/달성 여부 및 기본 레코드 목록에 대응
 */
data class DailySummaryResponse(
    @SerializedName("date")
    val date: String,

    @SerializedName("goal_progress")
    val goalProgress: GoalProgress?, // 목표 설정이 없는 날도 있으므로 Nullable

    @SerializedName("meta")
    val meta: Meta, // Map 대신 명확한 Meta 클래스 사용

    @SerializedName("record_counts")
    val recordCounts: RecordCounts, // Map 대신 명확한 RecordCounts 클래스 사용

    @SerializedName("records")
    val records: List<CareRecordResponse>
)

// meta 객체를 위한 명확한 데이터 클래스
data class Meta(
    @SerializedName("activity_minutes")
    val activityMinutes: Int?, // 값이 없을 수 있으므로 Nullable

    @SerializedName("bcs")
    val bcs: Int?,

    @SerializedName("meal_count")
    val mealCount: Int?, // 값이 없을 수 있으므로 Nullable

    @SerializedName("stool")
    val stool: String?,

    @SerializedName("vomit")
    val vomit: String?,

    @SerializedName("weight")
    val weight: Float?
)

// record_counts 객체를 위한 명확한 데이터 클래스
data class RecordCounts(
    @SerializedName("activity")
    val activity: Int?,

    @SerializedName("meal_count")
    val mealCount: Int?,

    @SerializedName("stool")
    val stool: Int?,

    @SerializedName("vomit")
    val vomit: Int?,

    @SerializedName("weight")
    val weight: Int?
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
    // 서버에서 숫자가 올 수도, 다른 타입이 올 수도 있으므로 Number 타입 사용
    @SerializedName("actual")
    val actual: Number,

    @SerializedName("goal")
    val goal: Int,

    @SerializedName("percentage")
    val percentage: Float?,

    @SerializedName("achieved")
    val achieved: Boolean?
)

/**
 * 활동 달성 정보
 */
data class ActivityAchievementInfo(
    @SerializedName("actual")
    val actual: Number,

    @SerializedName("goal")
    val goal: Int,

    @SerializedName("percentage")
    val percentage: Float?,

    @SerializedName("achieved")
    val achieved: Boolean?,

    @SerializedName("detail")
    val detail: ActivityDetail?
)

/**
 * 활동 상세 정보
 */
data class ActivityDetail(
    @SerializedName("derived_goal_minutes")
    val derivedGoalMinutes: Int?,
    @SerializedName("minutes_per_session")
    val minutesPerSession: Int?,
    @SerializedName("sessions")
    val sessions: Int?
)

/**
 * 몸무게 달성 정보
 */
data class WeightAchievementInfo(
    @SerializedName("actual")
    val actual: Float,

    @SerializedName("goal")
    val goal: Float,

    @SerializedName("at_goal")
    val atGoal: Boolean?,

    @SerializedName("diff")
    val diff: Float?
)