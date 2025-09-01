// app/src/main/java/com/example/pet_project_frontend/data/repository/UserRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.data.mapper.UserMapper
import com.example.pet_project_frontend.data.remote.api.UserApi
import com.example.pet_project_frontend.data.remote.dto.request.UpdateFcmTokenRequest
import com.example.pet_project_frontend.data.remote.dto.request.UpdateProfileImageRequest
import com.example.pet_project_frontend.data.remote.dto.request.UserUpdateRequest
import com.example.pet_project_frontend.data.remote.dto.response.UserProfileResponse
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.User
import com.example.pet_project_frontend.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val tokenManager: TokenManager
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"
    }

    override suspend fun getUserInfo(): NetworkResult<User> {
        return try {
            // TODO: 실제 사용자 ID를 가져오는 로직 구현 필요 (토큰에서 추출하거나 별도 API로)
            val userId = "current_user" // 임시 고정 ID
            
            Log.d(TAG, "Fetching user info from API: $userId")
            val response = userApi.getUserProfile(userId)

            if (response.isSuccessful) {
                response.body()?.let { userProfile ->
                    Log.d(TAG, "User profile fetched successfully: ${userProfile.userId}")
                    // 매퍼를 사용하여 DTO를 도메인 모델로 변환
                    NetworkResult.Success(UserMapper.mapToDomainModel(userProfile))
                } ?: run {
                    Log.e(TAG, "User profile response body is null")
                    NetworkResult.Error(response.code(), "Empty response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to get user profile. Code: ${response.code()}, Error: $errorBody")
                NetworkResult.Error(response.code(), errorBody ?: "Failed to get user profile")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting user info", e)
            NetworkResult.Exception(e)
        }
    }

    override suspend fun updateUserProfile(request: UserUpdateRequest): NetworkResult<User> {
        return try {
            Log.d(TAG, "Updating user profile")
            // TODO: UserApi에 updateUserProfile 메소드 추가 필요
            // 현재는 updateProfileImage만 있음
            
            // 임시로 기본 사용자 정보 반환 (실제 API 구현 시 제거)
            val userProfile = UserProfileResponse(
                userId = "current_user",
                nickname = request.name,
                profileImageUrl = null,
                postCount = 0
            )
            
            Log.d(TAG, "User profile updated successfully (mock)")
            NetworkResult.Success(UserMapper.mapToDomainModel(userProfile))
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating user profile", e)
            NetworkResult.Exception(e)
        }
    }

    suspend fun updateProfileImage(filePath: String): NetworkResult<UserProfileResponse> {
        return try {
            Log.d(TAG, "Updating profile image with path: $filePath")
            val request = UpdateProfileImageRequest(filePath = filePath)
            val response = userApi.updateProfileImage(request)

            if (response.isSuccessful) {
                response.body()?.let { userProfile ->
                    Log.d(TAG, "Profile image updated successfully: ${userProfile.userId}")
                    NetworkResult.Success(userProfile)
                } ?: run {
                    Log.e(TAG, "Profile image update response body is null")
                    NetworkResult.Error(response.code(), "Empty response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to update profile image. Code: ${response.code()}, Error: $errorBody")
                NetworkResult.Error(response.code(), errorBody ?: "Failed to update profile image")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating profile image", e)
            NetworkResult.Exception(e)
        }
    }

    suspend fun updateFcmToken(fcmToken: String): NetworkResult<Unit> {
        return try {
            Log.d(TAG, "Updating FCM token")
            val request = UpdateFcmTokenRequest(fcmToken = fcmToken)
            val response = userApi.updateFcmToken(request)

            if (response.isSuccessful) {
                Log.d(TAG, "FCM token updated successfully")
                NetworkResult.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to update FCM token. Code: ${response.code()}, Error: $errorBody")
                NetworkResult.Error(response.code(), errorBody ?: "Failed to update FCM token")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating FCM token", e)
            NetworkResult.Exception(e)
        }
    }

    override suspend fun deleteUser(): NetworkResult<Unit> {
        return try {
            Log.d(TAG, "Deleting user account")
            // TODO: UserApi에 deleteUser 메소드 추가 필요
            
            // 로컬 토큰 삭제
            tokenManager.clearTokens()
            
            Log.d(TAG, "User account deleted successfully (local tokens cleared)")
            NetworkResult.Success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception while deleting user", e)
            // 예외 발생해도 로컬 토큰은 삭제
            tokenManager.clearTokens()
            NetworkResult.Exception(e)
        }
    }

    override suspend fun saveAccessToken(token: String) {
        Log.d(TAG, "Saving access token")
        tokenManager.saveAccessToken(token)
    }

    override fun getAccessToken(): Flow<String?> {
        return tokenManager.getAccessTokenFlow()
    }
}