// app/src/main/java/com/example/pet_project_frontend/data/repository/UserRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.data.mapper.NotificationPreferencesMapper
import com.example.pet_project_frontend.data.mapper.UserMapper
import com.example.pet_project_frontend.data.remote.api.UserApi
import com.example.pet_project_frontend.data.remote.dto.request.UpdateFcmTokenRequest
import com.example.pet_project_frontend.data.remote.dto.request.UpdateProfileImageRequest
import com.example.pet_project_frontend.data.remote.dto.request.UserUpdateRequest
import com.example.pet_project_frontend.domain.model.NotificationPreferences
import com.example.pet_project_frontend.domain.model.User
import com.example.pet_project_frontend.domain.repository.UserRepository
import com.example.pet_project_frontend.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"
    }

    override suspend fun getUserInfo(): AppResult<User> {
        val saved = authRepository.getUserInfo()
        if (saved == null) {
            Log.w(TAG, "No logged-in user. Cannot fetch profile.")
            return AppResult.Error(code = 401, message = "로그인이 필요합니다.")
        }
        Log.d(TAG, "Fetching current user info from API")
        return SafeApi.response { userApi.getUserProfile() }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        Log.d(TAG, "User profile fetched successfully: ${res.data.userId}")
                        AppResult.Success(UserMapper.mapToDomainModel(res.data))
                    }
                    is AppResult.Error -> res
                    is AppResult.Exception -> res
                }
            }
    }

    /**
     * 현재 사용자의 프로필 이미지 URL만 가져오기
     * /api/users/me는 profile_image_url을 반환하지 않으므로
     * /api/users/me/summary를 사용하여 pet의 profile_image_url을 가져옴
     */
    override suspend fun getUserProfileImageUrl(): String? {
        val saved = authRepository.getUserInfo()
        if (saved == null) {
            Log.w(TAG, "No logged-in user. Cannot fetch profile image.")
            return null
        }
        
        Log.d(TAG, "Fetching user summary for profile image")
        return try {
            val response = userApi.getUserSummary()
            if (response.isSuccessful) {
                val profileImageUrl = response.body()?.pet?.profileImageUrl
                Log.d(TAG, "Profile image URL: $profileImageUrl")
                profileImageUrl
            } else {
                Log.e(TAG, "Failed to fetch user summary: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching profile image", e)
            null
        }
    }

    /**
     * 일반 프로필 업데이트는 OpenAPI spec에 정의되지 않음
     * Pet 프로필 업데이트를 사용하세요 (PetRepository.updatePetProfile)
     */
    override suspend fun updateUserProfile(request: UserUpdateRequest): AppResult<User> =
        AppResult.Error(code = 400, message = "일반 프로필 수정 API가 명세에 없습니다. Pet 프로필 업데이트를 사용하세요.")

    /**
     * DEPRECATED: profile_image_url은 이제 Pet 엔티티에서 관리됨
     * PetRepository.updatePetProfile()을 사용하여 프로필 이미지 업데이트
     */
    @Deprecated("Use PetRepository.updatePetProfile() instead")
    override suspend fun updateProfileImage(filePath: String): AppResult<User> {
        Log.w(TAG, "updateProfileImage is deprecated. Profile image is now managed in Pet entity.")
        return AppResult.Error(
            code = 400, 
            message = "프로필 이미지는 이제 Pet 프로필에서 관리됩니다. PetRepository.updatePetProfile()을 사용하세요."
        )
    }

    suspend fun updateFcmToken(fcmToken: String): AppResult<Unit> {
        Log.d(TAG, "Updating FCM token")
        val request = UpdateFcmTokenRequest(fcmToken = fcmToken)
        return SafeApi.responseUnit { userApi.updateFcmToken(request) }
    }

    override suspend fun deleteUser(): AppResult<Unit> {
        Log.d(TAG, "Deleting user account")
        val result = SafeApi.responseUnit { userApi.deleteMe() }
        // 서버 성공/실패와 무관하게 로컬 정리 수행
        tokenManager.clearTokens()
        authRepository.clearUserInfo()
        return result
    }

    override suspend fun saveAccessToken(token: String) {
        Log.d(TAG, "Saving access token")
        tokenManager.saveAccessToken(token)
    }

    override fun getAccessToken(): Flow<String?> {
        return tokenManager.getAccessTokenFlow()
    }

    /**
     * GET /api/users/me/notification-preferences
     * 현재 사용자의 알림 설정 조회
     */
    override suspend fun getNotificationPreferences(): AppResult<NotificationPreferences> {
        Log.d(TAG, "Fetching notification preferences")
        return SafeApi.response { userApi.getNotificationPreferences() }
            .let { result ->
                when (result) {
                    is AppResult.Success -> {
                        val domain = NotificationPreferencesMapper.toDomainModel(result.data)
                        Log.d(TAG, "Notification preferences fetched: mode=${domain.mode}, weeklyReport=${domain.weeklyReport}, like=${domain.likeEnabled}, comment=${domain.commentEnabled}")
                        AppResult.Success(domain)
                    }
                    is AppResult.Error -> {
                        Log.e(TAG, "Failed to fetch notification preferences: ${result.message}")
                        result
                    }
                    is AppResult.Exception -> {
                        Log.e(TAG, "Exception fetching notification preferences", result.throwable)
                        result
                    }
                }
            }
    }

    /**
     * PUT /api/users/me/notification-preferences
     * 현재 사용자의 알림 설정 수정
     */
    override suspend fun updateNotificationPreferences(preferences: NotificationPreferences): AppResult<NotificationPreferences> {
        Log.d(TAG, "Updating notification preferences: mode=${preferences.mode}, weeklyReport=${preferences.weeklyReport}, like=${preferences.likeEnabled}, comment=${preferences.commentEnabled}")
        
        val request = NotificationPreferencesMapper.toRequest(preferences)
        
        return SafeApi.response { userApi.updateNotificationPreferences(request) }
            .let { result ->
                when (result) {
                    is AppResult.Success -> {
                        val domain = NotificationPreferencesMapper.toDomainModel(result.data)
                        Log.d(TAG, "Notification preferences updated successfully")
                        AppResult.Success(domain)
                    }
                    is AppResult.Error -> {
                        Log.e(TAG, "Failed to update notification preferences: ${result.message}")
                        result
                    }
                    is AppResult.Exception -> {
                        Log.e(TAG, "Exception updating notification preferences", result.throwable)
                        result
                    }
                }
            }
    }
}
