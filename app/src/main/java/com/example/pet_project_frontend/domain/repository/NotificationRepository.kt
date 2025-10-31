package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.Notification
import com.example.pet_project_frontend.domain.model.NotificationList
import com.example.pet_project_frontend.domain.model.UnreadCount

/**
 * 알림 관련 Repository 인터페이스
 */
interface NotificationRepository {
    
    /**
     * 알림 목록 조회
     * 
     * @param limit 한 번에 가져올 알림 수 (기본 20, 최대 100)
     * @param cursor 페이지네이션 커서 (null이면 첫 페이지)
     * @param format 포맷 타입 (mobile/web)
     * @return 알림 목록과 다음 커서
     */
    suspend fun getNotifications(
        limit: Int = 20,
        cursor: String? = null,
        format: String = "mobile"
    ): AppResult<NotificationList>
    
    /**
     * 알림 확인 처리 (읽음 표시)
     * 
     * @param notificationId 알림 ID
     * @return 성공 여부
     */
    suspend fun markAsRead(notificationId: String): AppResult<Unit>
    
    /**
     * 미확인 알림 개수 조회
     * 
     * @return 미확인 알림 수
     */
    suspend fun getUnreadCount(): AppResult<UnreadCount>
}
