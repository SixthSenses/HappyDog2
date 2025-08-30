package com.example.pet_project_frontend.domain.model

import java.time.LocalDateTime

data class User(
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String? = null,
    val phoneNumber: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isEmailVerified: Boolean = false,
    val notificationSettings: NotificationSettings = NotificationSettings()
) {
    // 비즈니스 로직: 사용자 표시명
    val displayName: String
        get() = name.ifEmpty { email.split("@").first() }
    
    // 비즈니스 로직: 프로필 이미지 상태
    val hasProfileImage: Boolean
        get() = !profileImageUrl.isNullOrEmpty()
    
    // 비즈니스 로직: 계정 상태
    val accountStatus: String
        get() = when {
            isEmailVerified -> "인증됨"
            else -> "미인증"
        }
    
    // 비즈니스 로직: 가입 기간
    val membershipDuration: String
        get() {
            val days = java.time.Duration.between(createdAt, LocalDateTime.now()).toDays()
            return when {
                days < 30 -> "${days}일"
                days < 365 -> "${days / 30}개월"
                else -> "${days / 365}년"
            }
        }
}
