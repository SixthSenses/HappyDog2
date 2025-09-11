package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

// ===== 인증 (Authentication) Request =====

data class SocialLoginRequest(
    @SerializedName("provider")
    val provider: String = "google",
    @SerializedName("auth_code")
    val authCode: String
)
