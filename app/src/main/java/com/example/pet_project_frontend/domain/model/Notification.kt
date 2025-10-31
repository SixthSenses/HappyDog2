package com.example.pet_project_frontend.domain.model

/**
 * 알림 도메인 모델
 * 
 * @property id 알림 고유 ID
 * @property type 알림 타입 (POST_LIKE, COMMENT_LIKE, COMMENT, MENTION, CARTOON_SUCCESS, etc.)
 * @property title 알림 제목
 * @property message 알림 메시지
 * @property createdAt 알림 생성 시각 (ISO 8601 문자열)
 * @property deeplink 딥링크 URL (예: app://posts/{post_id})
 * @property read 읽음 여부
 */
data class Notification(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val createdAt: String,
    val deeplink: String,
    val read: Boolean
)

/**
 * 알림 목록 응답
 * 
 * @property items 알림 목록
 * @property nextCursor 다음 페이지 커서 (null이면 마지막 페이지)
 */
data class NotificationList(
    val items: List<Notification>,
    val nextCursor: String?
)

/**
 * 미확인 알림 개수
 * 
 * @property unreadCount 미확인 알림 수
 */
data class UnreadCount(
    val unreadCount: Int
)
