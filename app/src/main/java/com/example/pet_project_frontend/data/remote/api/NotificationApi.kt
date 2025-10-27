package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.EmptyRequest
import com.example.pet_project_frontend.data.remote.dto.response.NotificationAckResponseDto
import com.example.pet_project_frontend.data.remote.dto.response.NotificationListResponseDto
import com.example.pet_project_frontend.data.remote.dto.response.NotificationUnreadCountResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 알림 API 인터페이스
 */
interface NotificationApi {
    
    /**
     * GET /api/notifications
     * 알림 목록 조회
     * 
     * @param limit 한 번에 가져올 알림 수 (기본 20, 최대 100)
     * @param cursor 페이지네이션 커서
     * @param format 포맷 타입 (mobile/web)
     */
    @GET("/api/notifications")
    suspend fun getNotifications(
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
        @Query("format") format: String? = null
    ): Response<NotificationListResponseDto>
    
    /**
     * POST /api/notifications/{notification_id}/ack
     * 알림 확인 처리
     * 
     * @param notificationId 알림 ID
     */
    @POST("/api/notifications/{notification_id}/ack")
    suspend fun markNotificationAsRead(
        @Path("notification_id") notificationId: String,
        @Body request: EmptyRequest = EmptyRequest()
    ): Response<NotificationAckResponseDto>
    
    /**
     * GET /api/notifications/unread-count
     * 미확인 알림 개수 조회
     */
    @GET("/api/notifications/unread-count")
    suspend fun getUnreadCount(): Response<NotificationUnreadCountResponseDto>
}
