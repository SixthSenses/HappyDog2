package com.example.pet_project_frontend.presentation.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.Notification
import com.example.pet_project_frontend.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val error: String? = null,
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "NotificationViewModel"
    }
    
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    
    init {
        loadNotifications()
        loadUnreadCount()
    }
    
    /**
     * 알림 목록 조회
     */
    fun loadNotifications(cursor: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = notificationRepository.getNotifications(
                limit = 20,
                cursor = cursor,
                format = "mobile"
            )) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notifications = if (cursor == null) data.items 
                                       else _uiState.value.notifications + data.items,
                        nextCursor = data.nextCursor,
                        hasMore = data.nextCursor != null
                    )
                }
                is AppResult.Error -> {
                    Log.e(TAG, "알림 목록 조회 실패: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is AppResult.Exception -> {
                    Log.e(TAG, "알림 목록 조회 예외", result.throwable)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.throwable.message ?: "알 수 없는 오류"
                    )
                }
            }
        }
    }
    
    /**
     * 더 많은 알림 불러오기 (페이지네이션)
     */
    fun loadMoreNotifications() {
        val cursor = _uiState.value.nextCursor ?: return
        if (!_uiState.value.hasMore || _uiState.value.isLoading) return
        
        loadNotifications(cursor)
    }
    
    /**
     * 알림 확인 처리 (읽음 표시)
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            when (val result = notificationRepository.markAsRead(notificationId)) {
                is AppResult.Success -> {
                    // 로컬 상태 업데이트
                    _uiState.value = _uiState.value.copy(
                        notifications = _uiState.value.notifications.map { notification ->
                            if (notification.id == notificationId) {
                                notification.copy(read = true)
                            } else {
                                notification
                            }
                        }
                    )
                    // 미확인 알림 수 업데이트
                    loadUnreadCount()
                }
                is AppResult.Error -> {
                    Log.e(TAG, "알림 확인 처리 실패: ${result.message}")
                }
                is AppResult.Exception -> {
                    Log.e(TAG, "알림 확인 처리 예외", result.throwable)
                }
            }
        }
    }
    
    /**
     * 미확인 알림 개수 조회
     */
    fun loadUnreadCount() {
        viewModelScope.launch {
            when (val result = notificationRepository.getUnreadCount()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        unreadCount = result.data.unreadCount
                    )
                }
                is AppResult.Error -> {
                    Log.e(TAG, "미확인 알림 개수 조회 실패: ${result.message}")
                }
                is AppResult.Exception -> {
                    Log.e(TAG, "미확인 알림 개수 조회 예외", result.throwable)
                }
            }
        }
    }
    
    /**
     * 새로고침
     */
    fun refresh() {
        loadNotifications(cursor = null)
        loadUnreadCount()
    }
}
