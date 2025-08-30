package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.UpdateFcmTokenRequest
import com.example.pet_project_frontend.data.remote.dto.request.UpdateProfileImageRequest
import com.example.pet_project_frontend.data.remote.dto.response.UserProfileResponse
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("api/users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<UserProfileResponse>
    
    @PATCH("api/users/me/profile-image")
    suspend fun updateProfileImage(@Body request: UpdateProfileImageRequest): Response<UserProfileResponse>
    
    @POST("api/users/me/fcm-token")
    suspend fun updateFcmToken(@Body request: UpdateFcmTokenRequest): Response<Unit>
}