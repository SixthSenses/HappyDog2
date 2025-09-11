package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.GetUploadUrlRequest
import com.example.pet_project_frontend.data.remote.dto.request.UpdateProfileImageRequest
import com.example.pet_project_frontend.data.remote.dto.response.UploadUrlResponse
import com.example.pet_project_frontend.data.remote.dto.response.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UploadApi {
    @POST("api/uploads/url")
    suspend fun getUploadUrl(@Body request: GetUploadUrlRequest): Response<UploadUrlResponse>
    
    @PATCH("api/users/me/profile-image")
    suspend fun updateProfileImage(@Body request: UpdateProfileImageRequest): Response<UserProfileResponse>
}