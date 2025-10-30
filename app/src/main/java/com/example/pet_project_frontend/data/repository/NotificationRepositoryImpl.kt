package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.remote.api.NotificationApi
import com.example.pet_project_frontend.data.remote.dto.request.EmptyRequest
import com.example.pet_project_frontend.domain.model.Notification
import com.example.pet_project_frontend.domain.model.NotificationList
import com.example.pet_project_frontend.domain.model.UnreadCount
import com.example.pet_project_frontend.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {
    
    companion object {
        private const val TAG = "NotificationRepository"
    }
    
    override suspend fun getNotifications(
        limit: Int,
        cursor: String?,
        format: String
    ): AppResult<NotificationList> {
        return when (val result = SafeApi.response { 
            notificationApi.getNotifications(limit, cursor, format) 
        }) {
            is AppResult.Success -> {
                val dto = result.data
                val notifications = dto.items.map { item ->
                    Notification(
                        id = item.id,
                        type = item.type,
                        title = item.title,
                        message = item.message,
                        createdAt = item.createdAt,
                        deeplink = item.deeplink,
                        read = item.read
                    )
                }
                
                // meta에서 next_cursor 추출
                val nextCursor = dto.meta["next_cursor"]
                
                AppResult.Success(
                    NotificationList(
                        items = notifications,
                        nextCursor = nextCursor
                    )
                )
            }
            is AppResult.Error -> {
                Log.e(TAG, "알림 목록 조회 실패: ${result.message}")
                result
            }
            is AppResult.Exception -> {
                Log.e(TAG, "알림 목록 조회 예외", result.throwable)
                result
            }
        }
    }
    
    override suspend fun markAsRead(notificationId: String): AppResult<Unit> {
        return when (val result = SafeApi.response { 
            notificationApi.markNotificationAsRead(notificationId, EmptyRequest()) 
        }) {
            is AppResult.Success -> {
                AppResult.Success(Unit)
            }
            is AppResult.Error -> {
                Log.e(TAG, "알림 확인 처리 실패: ${result.message}")
                result
            }
            is AppResult.Exception -> {
                Log.e(TAG, "알림 확인 처리 예외", result.throwable)
                result
            }
        }
    }
    
    override suspend fun getUnreadCount(): AppResult<UnreadCount> {
        return when (val result = SafeApi.response { 
            notificationApi.getUnreadCount() 
        }) {
            is AppResult.Success -> {
                AppResult.Success(
                    UnreadCount(unreadCount = result.data.unreadCount)
                )
            }
            is AppResult.Error -> {
                Log.e(TAG, "미확인 알림 개수 조회 실패: ${result.message}")
                result
            }
            is AppResult.Exception -> {
                Log.e(TAG, "미확인 알림 개수 조회 예외", result.throwable)
                result
            }
        }
    }
}
