package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * 만화 변환 작업 생성 요청
 * POST /api/cartoon-jobs/
 */
data class CartoonJobCreateRequest(
    @SerializedName("user_text")
    val userText: String?,
    
    @SerializedName("file_paths")
    val filePaths: List<String> // 이미지 URL 리스트 (최소 1개, 최대 1개)
)
