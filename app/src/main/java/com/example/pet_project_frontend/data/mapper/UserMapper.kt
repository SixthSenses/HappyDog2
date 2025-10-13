package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.response.*
import com.example.pet_project_frontend.domain.model.User
import java.time.LocalDateTime

object UserMapper {
    
    /**
     * UserMeResponse (GET /api/users/me) -> Domain User
     */
    fun mapToDomainModel(dto: UserMeResponse): User {
        return User(
            id = dto.userId,
            email = dto.email,
            name = dto.nickname,
            profileImageUrl = null, // Profile image는 Pet에서 관리됨
            phoneNumber = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isEmailVerified = false,
            notificationSettings = com.example.pet_project_frontend.domain.model.NotificationSettings()
        )
    }

    // SocialLoginResponse.userInfo 는 AuthUserInfo 이므로 별도 변환 제공
    fun fromAuthUserInfo(dto: AuthUserInfo): User {
        return User(
            id = dto.userId,
            email = dto.email,
            name = dto.nickname,
            profileImageUrl = null,
            phoneNumber = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isEmailVerified = false,
            notificationSettings = com.example.pet_project_frontend.domain.model.NotificationSettings()
        )
    }
}
