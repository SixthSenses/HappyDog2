package com.example.pet_project_frontend.data.repository

import com.example.pet_project_frontend.data.local.database.dao.UserDao
import com.example.pet_project_frontend.data.local.database.entities.UserEntity
import com.example.pet_project_frontend.data.mapper.UserMapper
import com.example.pet_project_frontend.data.remote.api.AuthApi
import com.example.pet_project_frontend.data.remote.dto.request.UserUpdateRequest
import com.example.pet_project_frontend.data.remote.dto.response.UserProfileResponse
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.User
import com.example.pet_project_frontend.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao
) : UserRepository {
    
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
}

// Extension functions for data conversion
private fun UserProfileResponse.toUserEntity(): UserEntity {
    return UserEntity(
        id = userId,
        email = "", // API에서 제공되지 않는 경우 빈 문자열
        name = nickname,
        profileImageUrl = profileImageUrl,
        createdAt = "", // API에서 제공되지 않는 경우 빈 문자열
        updatedAt = ""  // API에서 제공되지 않는 경우 빈 문자열
    )
}

private fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        email = email,
        name = name,
        profileImageUrl = profileImageUrl,
        phoneNumber = null, // Entity에는 전화번호 정보가 없으므로 null
        createdAt = java.time.LocalDateTime.now(), // Entity에는 생성 시간이 없으므로 현재 시간
        updatedAt = java.time.LocalDateTime.now(), // Entity에는 수정 시간이 없으므로 현재 시간
        isEmailVerified = false, // Entity에는 이메일 인증 정보가 없으므로 기본값
        notificationSettings = com.example.pet_project_frontend.domain.model.NotificationSettings()
    )
}
