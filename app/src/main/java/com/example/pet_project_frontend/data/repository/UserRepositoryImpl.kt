package com.example.pet_project_frontend.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.pet_project_frontend.data.local.database.dao.UserDao
import com.example.pet_project_frontend.data.local.database.entities.UserEntity
import com.example.pet_project_frontend.data.mapper.UserMapper
import com.example.pet_project_frontend.data.remote.api.AuthApi
import com.example.pet_project_frontend.data.remote.dto.request.UserUpdateRequest
import com.example.pet_project_frontend.data.remote.dto.response.UserProfileResponse
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.User
import com.example.pet_project_frontend.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    // [수정됨] Hilt를 통해 DataStore를 주입받습니다.
    private val dataStore: DataStore<Preferences>
) : UserRepository {

    // [추가됨] DataStore에서 사용할 키(Key)를 정의합니다.
    private companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    }

    override suspend fun getUserInfo(): NetworkResult<User> {
        return try {
            // 먼저 로컬 DB에서 확인
            val localUser = userDao.getUserById("current_user") // 임시로 고정 ID 사용
            if (localUser != null) {
                return NetworkResult.Success(localUser.toDomainModel())
            }

            // 로컬에 없으면 API 호출
            val response = authApi.getUserProfile("current_user") // 임시로 고정 ID 사용
            if (response.isSuccessful) {
                response.body()?.let { userProfile ->
                    // 로컬 DB에 저장
                    userDao.insertUser(userProfile.toUserEntity())
                    // 매퍼를 사용하여 DTO를 도메인 모델로 변환하여 반환
                    NetworkResult.Success(UserMapper.mapToDomainModel(userProfile))
                } ?: NetworkResult.Error(response.code(), "Empty response body")
            } else {
                NetworkResult.Error(response.code(), "Failed to get user profile: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun updateUserProfile(request: UserUpdateRequest): NetworkResult<User> {
        return try {
            // 임시로 빈 응답 반환 (실제 API 구현 필요)
            NetworkResult.Success(User(
                id = "current_user",
                email = request.email,
                name = request.name,
                profileImageUrl = null,
                phoneNumber = request.phoneNumber,
                createdAt = java.time.LocalDateTime.now(),
                updatedAt = java.time.LocalDateTime.now(),
                isEmailVerified = false,
                notificationSettings = com.example.pet_project_frontend.domain.model.NotificationSettings()
            ))
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun deleteUser(): NetworkResult<Unit> {
        return try {
            // 임시로 성공 응답 반환 (실제 API 구현 필요)
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // [추가됨] 액세스 토큰 저장 기능 구현
    override suspend fun saveAccessToken(token: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }

    // [추가됨] 액세스 토큰 조회 기능 구현
    override fun getAccessToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }
}

private fun UserProfileResponse.toUserEntity(): UserEntity {
    return UserEntity(
        userId = userId,
        email = "", // email이 null일 수 있으므로 안전 호출 처리
        nickname = nickname,
        profileImageUrl = profileImageUrl
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
        notificationSettings = com.example.pet_project_frontend.domain.model.NotificationSettings()
    )
}