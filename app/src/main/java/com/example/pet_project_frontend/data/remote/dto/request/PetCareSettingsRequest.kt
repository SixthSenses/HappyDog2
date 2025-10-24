package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class PetCareSettingsRequest(
    @SerializedName("target_daily_meal_count")
    val targetDailyMealCount: Int? = null,

    @SerializedName("target_daily_activity_sessions")  // 1일 목표 활동 횟수 (예: 3회)
    val targetDailyActivitySessions: Int? = null,

    @SerializedName("activity_session_minutes")  // 1회 활동 시간 (예: 30분)
    val activitySessionMinutes: Int? = null,

    @SerializedName("target_weight")
    val targetWeight: Double? = null,

    @SerializedName("daily_meal_count")
    val dailyMealCount: Int? = null
)