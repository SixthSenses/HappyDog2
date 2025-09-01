// app/src/main/java/com/example/pet_project_frontend/data/repository/UserRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.data.local.database.dao.UserDao
import com.example.pet_project_frontend.data.local.database.entities.UserEntity
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.data.mapper.UserMapper
import com.example.pet_project_frontend.data.remote.api.UserApi
import com.example.pet_project_frontend.data.remote.dto.request.UpdateFcmTokenRequest
import com.example.pet_project_frontend.data.remote.dto.request.UpdateProfileImageRequest
import com.example.pet_project_frontend.data.remote.dto.request.UserUpdateRequest
import com.example.pet_project_frontend.data.remote.dto.response.UserProfileResponse
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.NotificationSettings
import com.example.pet_project_frontend.domain.model.User
import com.example.pet_project_frontend.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"
    }

    override suspend fun getUserInfo(): NetworkResult<User> {
        return try {
            // TODO: 실제 사용자 ID를 가져오는 로직 구현 필요
            val userId = "current_user" // 임시 고정 ID

            // 먼저 로컬 DB에서 확인
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                Log.d(TAG, "User found in local DB: $userId")
                return NetworkResult.Success(localUser.toDomainModel())
            }

            Log.d(TAG, "Fetching user info from API: $userId")
            // 로컬에 없으면 API 호출
            val response = userApi.getUserProfile(userId)

            if (response.isSuccessful) {
                response.body()?.let { userProfile ->
                    Log.d(TAG, "User profile fetched successfully")
                    // 로컬 DB에 저장
                    userDao.insertUser(userProfile.toUserEntity())
                    // 매퍼를 사용하여 DTO를 도메인 모델로 변환하여 반환
                    NetworkResult.Success(UserMapper.mapToDomainModel(userProfile))
                } ?: run {
                    Log.e(TAG, "User profile response body is null")
                    NetworkResult.Error(response.code(), "Empty response body")
                }
            } else {
                Log.e(TAG, "Failed to get user profile. Code: ${response.code()}")
                NetworkResult.Error(response.code(), "Failed to get user profile: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting user info", e)
            NetworkResult.Exception(e)
        }
    }

    override suspend fun updateUserProfile(request: UserUpdateRequest): NetworkResult<User> {
        return try {
            Log.d(TAG, "Updating user profile")
            // TODO: 실제 API 구현 필요
            // 임시로 성공 응답 반환
            NetworkResult.Success(
                User(
                    id = "current_user",
                    email = request.email,
                    name = request.name,
                    profileImageUrl = null,
                    phoneNumber = request.phoneNumber,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    isEmailVerified = false,
                    notificationSettings = NotificationSettings()
                )
            )
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
                    Log.d(TAG, "Profile image updated successfully")
                    // 로컬 DB 업데이트
                    userDao.updateUser(userProfile.toUserEntity())
                    NetworkResult.Success(userProfile)
                } ?: run {
                    Log.e(TAG, "Profile image update response body is null")
                    NetworkResult.Error(response.code(), "Empty response body")
                }
            } else {
                Log.e(TAG, "Failed to update profile image. Code: ${response.code()}")
                NetworkResult.Error(response.code(), "Failed to update profile image")
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
                Log.e(TAG, "Failed to update FCM token. Code: ${response.code()}")
                NetworkResult.Error(response.code(), "Failed to update FCM token")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating FCM token", e)
            NetworkResult.Exception(e)
        }
    }

    override suspend fun deleteUser(): NetworkResult<Unit> {
        return try {
            Log.d(TAG, "Deleting user account")
            // TODO: 실제 API 구현 필요
            // 로컬 데이터 삭제
            tokenManager.clearTokens()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while deleting user", e)
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

// Extension functions for data conversion
private fun UserProfileResponse.toUserEntity(): UserEntity {
    return UserEntity(
        userId = userId,
        email = "", // API에서 제공되지 않는 경우 빈 문자열
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        phoneNumber = null,
        fcmToken = null,
        isEmailVerified = false,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}

private fun UserEntity.toDomainModel(): User {
    return User(
        id = userId,
        email = email,
        name = nickname,
        profileImageUrl = profileImageUrl,
        phoneNumber = phoneNumber,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isEmailVerified = isEmailVerified,
        notificationSettings = NotificationSettings()
    )
}