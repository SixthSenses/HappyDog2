package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// ===== 사용자 (Users) Response =====

data class UserProfileResponse(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("profile_image_url")
    val profileImageUrl: String?,
    @SerializedName("post_count")
    val postCount: Int
)

// ===== 파일 업로드 Response =====

data class UploadUrlResponse(
    @SerializedName("upload_url")
    val uploadUrl: String,
    @SerializedName("file_path")
    val filePath: String
)
