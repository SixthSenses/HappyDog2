package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// ===== 사용자 (Users) Response =====

/**
 * GET /api/users/me 응답 스키마
 * OpenAPI UserMeResponseSchema와 일치
 */
data class UserMeResponse(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("has_pet")
    val hasPet: Boolean,
    @SerializedName("pet_id")
    val petId: String?
)

/**
 * GET /api/users/me/summary 응답 스키마
 * OpenAPI UserSummaryResponseSchema와 일치
 */
data class UserSummaryResponse(
    @SerializedName("user")
    val user: UserSummaryUserSchema,
    @SerializedName("pet")
    val pet: UserSummaryPetSchema?,
    @SerializedName("pet_care_settings")
    val petCareSettings: Map<String, Any>?
)

data class UserSummaryUserSchema(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("post_count")
    val postCount: Int
)

data class UserSummaryPetSchema(
    @SerializedName("pet_id")
    val petId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("breed")
    val breed: String,
    @SerializedName("profile_image_url")
    val profileImageUrl: String?,
    @SerializedName("is_verified")
    val isVerified: Boolean
)

/**
 * GET /api/users/{user_id}/public 응답 스키마 (DEPRECATED)
 * OpenAPI UserPublicResponseSchema와 일치
 */
data class UserPublicResponse(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("post_count")
    val postCount: Int
)

/**
 * @deprecated 레거시 응답. UserMeResponse 사용 권장
 */
@Deprecated("Use UserMeResponse instead")
typealias UserProfileResponse = UserMeResponse

// ===== 파일 업로드 Response =====

data class UploadUrlResponse(
    @SerializedName("upload_url")
    val uploadUrl: String,
    @SerializedName("file_path")
    val filePath: String
)
