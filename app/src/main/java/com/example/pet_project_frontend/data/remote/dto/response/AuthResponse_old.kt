package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class SocialLoginResponse_old(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("is_new_user") val isNewUser: Boolean,
    @SerializedName("user_info") val userInfo: AuthUserInfo_old
)

data class AuthUserInfo_old(
    @SerializedName("user_id") val userId: String,
    @SerializedName("email") val email: String,
    @SerializedName("nickname") val nickname: String
)