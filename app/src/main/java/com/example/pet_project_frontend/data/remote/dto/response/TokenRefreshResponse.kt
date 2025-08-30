package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class TokenRefreshResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)
