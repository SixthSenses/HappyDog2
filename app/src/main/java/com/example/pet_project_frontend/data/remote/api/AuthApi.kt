package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.response.SocialLoginResponse
import com.example.pet_project_frontend.data.remote.dto.response.TokenRefreshResponse
import com.example.pet_project_frontend.data.remote.dto.request.LogoutRequest
import com.example.pet_project_frontend.data.remote.dto.request.SocialLoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    // 소셜 로그인 API
    @POST("api/auth/social")
    suspend fun socialLogin(@Body socialLoginRequest: SocialLoginRequest): Response<SocialLoginResponse>
    
    // 로그아웃 API
    @POST("api/auth/logout")
    suspend fun logout(@Body logoutRequest: LogoutRequest): Response<Unit>
    
    // Access Token 재발급 API
    @POST("api/auth/token/refresh")
    suspend fun refreshToken(@Header("Authorization") refreshToken: String): Response<TokenRefreshResponse>
    
    // 사용자 프로필 조회 API
    @retrofit2.http.GET("api/users/{userId}")
    suspend fun getUserProfile(@retrofit2.http.Path("userId") userId: String): Response<com.example.pet_project_frontend.data.remote.dto.response.UserProfileResponse>
}