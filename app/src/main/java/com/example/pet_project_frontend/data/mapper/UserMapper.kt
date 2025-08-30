package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.response.UserProfileResponse
import com.example.pet_project_frontend.domain.model.User
import java.time.LocalDateTime

object UserMapper {
    
    fun mapToDomainModel(dto: UserProfileResponse): User {
        return User(
            id = dto.userId,
            email = "", // API에서 제공되지 않는 경우 빈 문자열
            name = dto.nickname,
            profileImageUrl = dto.profileImageUrl,
            phoneNumber = null, // API에서 제공되지 않는 경우 null
            createdAt = LocalDateTime.now(), // API에서 제공되지 않는 경우 현재 시간
            updatedAt = LocalDateTime.now(), // API에서 제공되지 않는 경우 현재 시간
            isEmailVerified = false, // API에서 제공되지 않는 경우 기본값
            notificationSettings = com.example.pet_project_frontend.domain.model.NotificationSettings()
        )
    }
}
