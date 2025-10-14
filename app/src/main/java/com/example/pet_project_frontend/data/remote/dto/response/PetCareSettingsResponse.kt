package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PetCareSettingsResponse(
    @SerializedName("goalWeight")
    val goalWeight: Double, // API 예시에서 200이었지만, 체중이므로 Double이 더 적절
    @SerializedName("goalMealCount")
    val goalMealCount: Int,
    @SerializedName("goalActivityMinutes")
    val goalActivityMinutes: Int,
    @SerializedName("goalActivitySessions")
    val goalActivitySessions: Int,
    @SerializedName("activitySessionMinutes")
    val activitySessionMinutes: Int,
    @SerializedName("activityIncrementMinutes")
    val activityIncrementMinutes: Int,
    @SerializedName("pet_id")
    val petId: String,
    @SerializedName("updated_at")
    val updatedAt: String // 혹은 java.time.Instant, LocalDateTime 등으로 파싱 가능
)