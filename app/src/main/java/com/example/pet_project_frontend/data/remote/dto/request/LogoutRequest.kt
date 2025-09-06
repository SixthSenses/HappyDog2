package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class LogoutRequest(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)
