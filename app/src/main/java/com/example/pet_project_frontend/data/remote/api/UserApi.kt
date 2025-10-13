package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.NotificationPreferencesRequest
import com.example.pet_project_frontend.data.remote.dto.request.UpdateFcmTokenRequest
import com.example.pet_project_frontend.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

/**
 * User API - OpenAPI spec와 일치하도록 수정됨
 */
interface UserApi {
    /**
     * GET /api/users/me
     * 현재 로그인한 사용자의 기본 정보 조회
     */
    @GET("api/users/me")
    suspend fun getUserProfile(): Response<UserMeResponse>
    
    /**
     * GET /api/users/me/summary
     * 사용자, 반려동물, 펫케어 설정의 통합 요약 정보 조회
     */
    @GET("api/users/me/summary")
    suspend fun getUserSummary(): Response<UserSummaryResponse>
    
    /**
     * PUT /api/users/me/fcm-token
     * FCM 토큰 업데이트 (POST가 아닌 PUT)
     */
    @PUT("api/users/me/fcm-token")
    suspend fun updateFcmToken(@Body request: UpdateFcmTokenRequest): Response<Unit>

    /**
     * DELETE /api/users/me
     * 사용자 계정 삭제
     */
    @DELETE("api/users/me")
    suspend fun deleteMe(): Response<Unit>
    
    /**
     * GET /api/users/me/notification-preferences
     * 현재 사용자의 알림 설정 조회
     */
    @GET("api/users/me/notification-preferences")
    suspend fun getNotificationPreferences(): Response<NotificationPreferencesResponse>
    
    /**
     * PUT /api/users/me/notification-preferences
     * 현재 사용자의 알림 설정 수정
     */
    @PUT("api/users/me/notification-preferences")
    suspend fun updateNotificationPreferences(
        @Body request: NotificationPreferencesRequest
    ): Response<NotificationPreferencesResponse>
    
    /**
     * DEPRECATED: profile_image_url은 이제 Pet 엔티티에서 관리됨
     * PATCH /api/pets/{pet_id} 사용하여 profile_image_url 업데이트
     */
}