// 변경의도: 알림 토글 상태를 DataStore와 동기화하고 서버 연동까지 고려한 임시 저장 흐름을 구성한다.
package com.example.pet_project_frontend.presentation.mypage.settings.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    data class UiState(
        val pushEnabled: Boolean = true,
        val weeklyReport: Boolean = true,
        val likeEnabled: Boolean = false,
        val commentEnabled: Boolean = false,
        val loading: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.notificationSettings.collectLatest { prefs ->
                _uiState.update {
                    it.copy(
                        pushEnabled = prefs.pushEnabled,
                        weeklyReport = prefs.weeklyReport,
                        likeEnabled = prefs.likeEnabled,
                        commentEnabled = prefs.commentEnabled,
                        loading = false
                    )
                }
            }
        }
    }

    fun onTogglePush(enabled: Boolean) {
        _uiState.update { it.copy(pushEnabled = enabled) }
        persist {
            userPreferences.setPushEnabled(enabled)
            // TODO 서버 연동 시 치환: 서버 API 추가 시 토글 상태를 원격에 반영
        }
        if (!enabled) {
            // 임시 정책: 전체 알림 비활성화 시 하위 항목도 함께 비활성화
            onToggleWeekly(false)
            onToggleLike(false)
            onToggleComment(false)
        }
    }

    fun onToggleWeekly(enabled: Boolean) {
        _uiState.update { it.copy(weeklyReport = enabled) }
        persist {
            userPreferences.setWeeklyReport(enabled)
            // TODO 서버 연동 시 치환: 서버 API와 동기화
        }
    }

    fun onToggleLike(enabled: Boolean) {
        _uiState.update { it.copy(likeEnabled = enabled) }
        persist {
            userPreferences.setLikeEnabled(enabled)
            // TODO 서버 연동 시 치환: 서버 API와 동기화
        }
    }

    fun onToggleComment(enabled: Boolean) {
        _uiState.update { it.copy(commentEnabled = enabled) }
        persist {
            userPreferences.setCommentEnabled(enabled)
            // TODO 서버 연동 시 치환: 서버 API와 동기화
        }
    }

    private fun persist(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
