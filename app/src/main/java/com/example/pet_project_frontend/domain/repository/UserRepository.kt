package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.UserUpdateRequest
import com.example.pet_project_frontend.domain.model.NotificationPreferences
import com.example.pet_project_frontend.domain.model.User

interface UserRepository {
    suspend fun getUserInfo(): AppResult<User>
    suspend fun getUserProfileImageUrl(): String? // 프로필 이미지 URL만 간단히 가져오기
    suspend fun updateUserProfile(request: UserUpdateRequest): AppResult<User>
    // Update only the profile image; returns updated User domain model
    suspend fun updateProfileImage(filePath: String): AppResult<User>
    suspend fun deleteUser(): AppResult<Unit>
    suspend fun saveAccessToken(token: String)
    fun getAccessToken(): kotlinx.coroutines.flow.Flow<String?>
    
    /**
     * GET /api/users/me/notification-preferences
     * 현재 사용자의 알림 설정 조회
     */
    suspend fun getNotificationPreferences(): AppResult<NotificationPreferences>
    
    /**
     * PUT /api/users/me/notification-preferences
     * 현재 사용자의 알림 설정 수정
     */
    suspend fun updateNotificationPreferences(preferences: NotificationPreferences): AppResult<NotificationPreferences>
}