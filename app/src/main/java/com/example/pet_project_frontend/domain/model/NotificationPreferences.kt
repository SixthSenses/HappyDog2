package com.example.pet_project_frontend.domain.model

/**
 * 알림 설정 도메인 모델
 * 
 * UI와 백엔드 API 간의 중간 표현
 */
data class NotificationPreferences(
    /**
     * 알림 전송 방식
     * - "both": 인앱 + 푸시 (기본값)
     * - "push": 푸시만
     * - "inapp": 인앱만
     */
    val mode: String = "both",
    
    /**
     * 주간 리포트 알림 (펫케어)
     * 백엔드 타입: PET_CARE_DAILY_SUMMARY
     */
    val weeklyReport: Boolean = true,
    
    /**
     * 좋아요 알림 (멍스타그램)
     * 백엔드 타입: POST_LIKE + COMMENT_LIKE
     */
    val likeEnabled: Boolean = true,
    
    /**
     * 댓글/멘션 알림 (멍스타그램)
     * 백엔드 타입: COMMENT + MENTION
     */
    val commentEnabled: Boolean = true,
    
    /**
     * 기타 알림 타입들 (UI에 노출되지 않지만 서버와 동기화)
     * 예: CARTOON_SUCCESS, CARTOON_FAILED, PET_CARE_GOAL_REACHED
     */
    val otherTypes: Map<String, Boolean> = emptyMap()
)
