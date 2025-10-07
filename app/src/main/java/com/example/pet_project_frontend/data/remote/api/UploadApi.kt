package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.FinalizeCartoonRequest
import com.example.pet_project_frontend.data.remote.dto.request.GetUploadUrlRequest
import com.example.pet_project_frontend.data.remote.dto.request.UpdateProfileImageRequest
import com.example.pet_project_frontend.data.remote.dto.request.UploadUrlRequest
import com.example.pet_project_frontend.data.remote.dto.response.FinalizeCartoonResponse
import com.example.pet_project_frontend.data.remote.dto.response.UploadUrlResponse
import com.example.pet_project_frontend.data.remote.dto.response.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UploadApi {
    @POST("api/uploads/url")
    suspend fun getUploadUrl(@Body request: GetUploadUrlRequest): Response<UploadUrlResponse>
    
    /**
     * 업로드 URL 생성 (신규)
     * POST /api/uploads/url
     */
    @POST("/api/uploads/url")
    suspend fun requestUploadUrl(@Body request: UploadUrlRequest): Response<UploadUrlResponse>
    
    /**
     * 만화 이미지 공개 전환
     * POST /api/uploads/finalize-cartoon
     */
    @POST("/api/uploads/finalize-cartoon")
    suspend fun finalizeCartoon(@Body request: FinalizeCartoonRequest): Response<FinalizeCartoonResponse>
    
    @PATCH("api/users/me/profile-image")
    suspend fun updateProfileImage(@Body request: UpdateProfileImageRequest): Response<UserProfileResponse>
}