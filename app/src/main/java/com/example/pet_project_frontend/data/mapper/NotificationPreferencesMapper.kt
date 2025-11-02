package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.request.NotificationPreferencesRequest
import com.example.pet_project_frontend.data.remote.dto.response.NotificationPreferencesResponse
import com.example.pet_project_frontend.domain.model.NotificationPreferences

/**
 * NotificationPreferences 매퍼
 * 
 * UI ↔ Domain ↔ DTO 변환 로직
 * 
 * 매핑 전략:
 * - UI weeklyReport → types.PET_CARE_DAILY_SUMMARY
 * - UI likeEnabled → types.POST_LIKE + types.COMMENT_LIKE
 * - UI commentEnabled → types.COMMENT + types.MENTION
 * - 기타 타입들 (CARTOON_SUCCESS, CARTOON_FAILED 등)은 otherTypes에 보존
 */
object NotificationPreferencesMapper {
    
    // 백엔드 알림 타입 상수
    private object NotificationType {
        const val POST_LIKE = "POST_LIKE"
        const val COMMENT_LIKE = "COMMENT_LIKE"
        const val COMMENT = "COMMENT"
        const val MENTION = "MENTION"
        const val CARTOON_SUCCESS = "CARTOON_SUCCESS"
        const val CARTOON_FAILED = "CARTOON_FAILED"
        const val PET_CARE_GOAL_REACHED = "PET_CARE_GOAL_REACHED"
        const val PET_CARE_DAILY_SUMMARY = "PET_CARE_DAILY_SUMMARY"
    }
    
    /**
     * DTO → Domain Model 변환
     * 
     * 백엔드 응답을 UI에서 사용하는 형태로 변환
     * 
     * 주의: 서버에서 빈 객체 {}를 반환할 수 있음 (신규 사용자 또는 설정 미저장 상태)
     */
    fun toDomainModel(dto: NotificationPreferencesResponse): NotificationPreferences {
        // types가 null이면 기본값 사용 (빈 Map)
        val types = dto.types ?: emptyMap()
        
        // UI에 매핑되는 타입들 (서버에 없으면 기본값 true)
        val weeklyReport = types[NotificationType.PET_CARE_DAILY_SUMMARY] ?: true
        val likeEnabled = types[NotificationType.POST_LIKE] ?: true
        val commentEnabled = types[NotificationType.COMMENT] ?: true
        
        // UI에 노출되지 않는 기타 타입들 보존
        val uiMappedKeys = setOf(
            NotificationType.POST_LIKE,
            NotificationType.COMMENT_LIKE,
            NotificationType.COMMENT,
            NotificationType.MENTION,
            NotificationType.PET_CARE_DAILY_SUMMARY
        )
        val otherTypes = types.filterKeys { it !in uiMappedKeys }
        
        return NotificationPreferences(
            mode = dto.mode ?: "both",
            weeklyReport = weeklyReport,
            likeEnabled = likeEnabled,
            commentEnabled = commentEnabled,
            otherTypes = otherTypes
        )
    }
    
    /**
     * Domain Model → Request DTO 변환
     * 
     * UI 상태를 백엔드 API 형식으로 변환
     */
    fun toRequest(domain: NotificationPreferences): NotificationPreferencesRequest {
        // UI 설정을 백엔드 타입으로 매핑
        val types = mutableMapOf<String, Boolean>()
        
        // 주간 리포트
        types[NotificationType.PET_CARE_DAILY_SUMMARY] = domain.weeklyReport
        
        // 좋아요 (게시글 + 댓글)
        types[NotificationType.POST_LIKE] = domain.likeEnabled
        types[NotificationType.COMMENT_LIKE] = domain.likeEnabled
        
        // 댓글/멘션
        types[NotificationType.COMMENT] = domain.commentEnabled
        types[NotificationType.MENTION] = domain.commentEnabled
        
        // 기타 타입들 보존 (UI에 없는 타입들)
        types.putAll(domain.otherTypes)
        
        // 기본 타입들 (서버와 호환성 유지)
        // 사용자가 명시적으로 비활성화하지 않은 타입들은 기본값(true) 적용
        val defaultTypes = listOf(
            NotificationType.CARTOON_SUCCESS,
            NotificationType.CARTOON_FAILED,
            NotificationType.PET_CARE_GOAL_REACHED
        )
        defaultTypes.forEach { type ->
            if (type !in types) {
                types[type] = true
            }
        }
        
        return NotificationPreferencesRequest(
            mode = domain.mode,
            types = types
        )
    }
    
    /**
     * DataStore NotificationSettings → Domain Model 변환
     * 
     * 로컬 DataStore 데이터를 Domain Model로 변환 (서버 연동 전 초기 상태)
     */
    fun fromLocalSettings(
        weeklyReport: Boolean,
        likeEnabled: Boolean,
        commentEnabled: Boolean
    ): NotificationPreferences {
        return NotificationPreferences(
            mode = "both",
            weeklyReport = weeklyReport,
            likeEnabled = likeEnabled,
            commentEnabled = commentEnabled,
            otherTypes = emptyMap()
        )
    }
}
