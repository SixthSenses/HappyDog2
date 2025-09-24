package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// ===== 인증 (Authentication) Response =====

data class SocialLoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("is_new_user")
    val isNewUser: Boolean,
    @SerializedName("user_info")
    val userInfo: AuthUserInfo
)

/**
 * AuthUserInfoSchema (openapi) — 인증 토큰 응답 내의 최소 사용자 정보
 * 필드: user_id, email, nickname
 */
data class AuthUserInfo(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("nickname")
    val nickname: String
)

/**
 * Users 도메인 응답에서 사용하는 확장 사용자 정보 (프로필 이미지 포함)
 * openapi의 Users 관련 스키마에 맞춰 사용됩니다.
 */
data class UserInfo(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("profile_image_url")
    val profileImageUrl: String?
)


