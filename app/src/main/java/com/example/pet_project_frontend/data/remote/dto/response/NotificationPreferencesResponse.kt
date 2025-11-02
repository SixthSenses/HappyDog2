package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * GET/PUT /api/users/me/notification-preferences 응답 DTO
 * 
 * OpenAPI NotificationPreferencesResponseSchema:
 * - mode: 알림 전송 방식 설정
 * - types: 알림 타입별 수신 설정
 */
data class NotificationPreferencesResponse(
    /**
     * 알림 전송 방식
     * - "both": 인앱 + 푸시
     * - "push": 푸시만
     * - "inapp": 인앱만
     * - null: 기본값 사용
     */
    @SerializedName("mode")
    val mode: String?,
    
    /**
     * 알림 타입별 수신 설정
     * 
     * Key: NotificationType 문자열 (예: "POST_LIKE", "COMMENT")
     * Value: true (수신) / false (차단)
     * 
     * 백엔드 기본값 (신규 사용자):
     * - 모든 타입이 true로 설정됨 (Opt-out 방식)
     * - 사용자가 명시적으로 false로 변경한 타입만 차단됨
     * 
     * 주의: 서버가 빈 객체 {}를 반환할 경우 null일 수 있음
     */
    @SerializedName("types")
    val types: Map<String, Boolean>?
)
