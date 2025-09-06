package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class UpdateFcmTokenRequest(
    @SerializedName("fcm_token")
    val fcmToken: String
)
