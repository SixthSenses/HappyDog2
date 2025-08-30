package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.data.remote.dto.response.SocialLoginResponse
import com.example.pet_project_frontend.data.remote.dto.response.TokenRefreshResponse
import com.example.pet_project_frontend.data.remote.dto.response.UserInfo
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun socialLogin(authCode: String): NetworkResult<SocialLoginResponse>
    suspend fun refreshToken(refreshToken: String): NetworkResult<TokenRefreshResponse>
    suspend fun logout(accessToken: String, refreshToken: String): NetworkResult<Unit>
    
    // 토큰 관리
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
    
    // 사용자 정보 관리
    suspend fun saveUserInfo(userInfo: UserInfo)
    suspend fun getUserInfo(): UserInfo?
    suspend fun clearUserInfo()
}