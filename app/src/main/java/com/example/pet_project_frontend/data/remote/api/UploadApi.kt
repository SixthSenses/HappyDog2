package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface UploadApi {
    // 업로드 URL 생성
    @POST("api/uploads/url")
    suspend fun getUploadUrl(
        @Body request: UploadUrlRequestDto
    ): Response<UploadUrlResponseDto>
    
    // Pre-signed URL로 파일 업로드 (S3 직접)
    @PUT
    suspend fun uploadFile(
        @Url url: String,
        @Header("Content-Type") contentType: String,
        @Body file: RequestBody
    ): Response<Unit>
}