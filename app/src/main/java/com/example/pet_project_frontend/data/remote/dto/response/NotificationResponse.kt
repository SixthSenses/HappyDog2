package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * 알림 아이템 응답 DTO
 */
data class NotificationItemDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("deeplink")
    val deeplink: String,
    
    @SerializedName("read")
    val read: Boolean
)

/**
 * 알림 목록 응답 DTO
 */
data class NotificationListResponseDto(
    @SerializedName("items")
    val items: List<NotificationItemDto>,
    
    @SerializedName("meta")
    val meta: Map<String, String>
)

/**
 * 알림 확인 응답 DTO
 */
data class NotificationAckResponseDto(
    @SerializedName("success")
    val success: Boolean? = null
)

/**
 * 미확인 알림 개수 응답 DTO
 */
data class NotificationUnreadCountResponseDto(
    @SerializedName("unread_count")
    val unreadCount: Int
)
