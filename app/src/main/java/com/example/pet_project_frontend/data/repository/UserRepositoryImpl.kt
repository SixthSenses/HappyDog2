// app/src/main/java/com/example/pet_project_frontend/data/repository/UserRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.data.mapper.UserMapper
import com.example.pet_project_frontend.data.remote.api.UserApi
import com.example.pet_project_frontend.data.remote.dto.request.UpdateFcmTokenRequest
import com.example.pet_project_frontend.data.remote.dto.request.UpdateProfileImageRequest
import com.example.pet_project_frontend.data.remote.dto.request.UserUpdateRequest
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
        val userId = saved?.userId
        if (userId == null) {
            Log.w(TAG, "No logged-in user. Cannot fetch profile.")
            return AppResult.Error(code = 401, message = "로그인이 필요합니다.")
        }
        Log.d(TAG, "Fetching user info from API: $userId")
        return SafeApi.response { userApi.getUserProfile(userId) }
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

    // TODO: General profile update API is not specified in the spec. Keep only updateProfileImage.
    override suspend fun updateUserProfile(request: UserUpdateRequest): AppResult<User> =
        AppResult.Error(code = 400, message = "일반 프로필 수정 API가 명세에 없습니다.")

    override suspend fun updateProfileImage(filePath: String): AppResult<User> {
        Log.d(TAG, "Updating profile image with path: $filePath")
        val request = UpdateProfileImageRequest(filePath = filePath)
        return SafeApi.response { userApi.updateProfileImage(request) }
            .let { res ->
                when (res) {
                    is AppResult.Success -> AppResult.Success(UserMapper.mapToDomainModel(res.data))
                    is AppResult.Error -> res
                    is AppResult.Exception -> res
                }
            }
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
}
