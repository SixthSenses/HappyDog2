package com.example.pet_project_frontend.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * PUT /api/users/me/notification-preferences 요청 DTO
 * 
 * OpenAPI NotificationPreferencesSchema:
 * - mode: "both" | "push" | "inapp" | null (알림 전송 방식)
 * - types: 알림 타입별 수신 on/off
 *   - Key: NotificationType 문자열 (예: "POST_LIKE", "COMMENT")
 *   - Value: true (수신) / false (차단)
 */
data class NotificationPreferencesRequest(
    /**
     * 알림 전송 방식
     * - "both": 인앱 + 푸시 (기본값)
     * - "push": 푸시만
     * - "inapp": 인앱만
     * - null: 서비스 기본값 사용
     */
    @SerializedName("mode")
    val mode: String? = "both",
    
    /**
     * 알림 타입별 수신 설정
     * 
     * 지원되는 타입 (백엔드 NotificationType Enum):
     * - POST_LIKE: 게시글 좋아요
     * - COMMENT_LIKE: 댓글 좋아요
     * - COMMENT: 게시글에 댓글 작성
     * - MENTION: 댓글에서 @멘션
     * - CARTOON_SUCCESS: 카툰 생성 성공
     * - CARTOON_FAILED: 카툰 생성 실패
     * - PET_CARE_GOAL_REACHED: 목표 달성 (예약)
     * - PET_CARE_DAILY_SUMMARY: 일일 요약 (예약)
     */
    @SerializedName("types")
    val types: Map<String, Boolean>
)
