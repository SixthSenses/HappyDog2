package com.example.pet_project_frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

// 업로드 URL 요청 DTO
data class UploadUrlRequestDto(
    @SerializedName("content_type")
    val contentType: String,
    
    @SerializedName("upload_type")
    val uploadType: String, // "post_image", "cartoon_source_image", "pet_profile", etc.
    
    @SerializedName("filename")
    val filename: String
)

// 업로드 URL 응답 DTO
data class UploadUrlResponseDto(
    @SerializedName("upload_url")
    val uploadUrl: String,
    
    @SerializedName("file_path")
    val filePath: String,
    
    @SerializedName("public_url")
    val publicUrl: String?,
    
    @SerializedName("expires_at")
    val expiresAt: String? // 백엔드가 보내지 않는 경우 있음
)
