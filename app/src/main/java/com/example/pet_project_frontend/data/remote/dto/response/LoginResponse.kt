package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// 로그인 응답 데이터 모델
data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    // ✨ 신규 유저 여부를 판별하는 핵심 필드
    @SerializedName("is_new_user")
    val isNewUser: Boolean,
    @SerializedName("user_info")
    val userInfo: UserInfoResponse
)

data class UserInfoResponse(
    @SerializedName("user_id")
    val userId: String,
    val email: String?,
    val nickname: String?,
    @SerializedName("profile_image_url")
    val profileImageUrl: String?
)