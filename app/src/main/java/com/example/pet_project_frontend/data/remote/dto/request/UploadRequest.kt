package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * 만화 변환 작업 생성 요청
 * POST /api/cartoon-jobs/
 */
data class CartoonJobCreateRequest(
    @SerializedName("file_paths")
    val filePaths: List<String>, // 최소 1개, 최대 1개 (현재는 단일 이미지만 지원)
    @SerializedName("user_text")
    val userText: String? = null // 선택 사항, 최대 500자
)

/**
 * 업로드 URL 요청
 * POST /api/uploads/url
 */
data class UploadUrlRequest(
    @SerializedName("upload_type")
    val uploadType: UploadType,
    @SerializedName("filename")
    val filename: String, // 1-200자
    @SerializedName("content_type")
    val contentType: String // 3-120자 (예: "image/jpeg")
)

/**
 * 업로드 타입 Enum
 */
enum class UploadType(val value: String) {
    @SerializedName("pet_profile")
    PET_PROFILE("pet_profile"),
    
    @SerializedName("pet_nose_print")
    PET_NOSE_PRINT("pet_nose_print"),
    
    @SerializedName("eye_analysis")
    EYE_ANALYSIS("eye_analysis"),
    
    @SerializedName("post_image")
    POST_IMAGE("post_image"),
    
    @SerializedName("cartoon_source_image")
    CARTOON_SOURCE_IMAGE("cartoon_source_image");
    
    override fun toString() = value
}

/**
 * 만화 이미지 공개 전환 요청
 * POST /api/uploads/finalize-cartoon
 */
data class FinalizeCartoonRequest(
    @SerializedName("file_path")
    val filePath: String
)
