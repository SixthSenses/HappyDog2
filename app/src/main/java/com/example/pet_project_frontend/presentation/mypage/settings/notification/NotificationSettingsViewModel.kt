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

    // UI에서 사용할 알림 토글 상태
    data class UiState(
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
                _uiState.value = UiState(
                    weeklyReport = prefs.weeklyReport,
                    likeEnabled = prefs.likeEnabled,
                    commentEnabled = prefs.commentEnabled,
                    loading = false
                )
            }
        }
    }

    fun onToggleWeekly(enabled: Boolean) {
        _uiState.update { it.copy(weeklyReport = enabled) }
        persist { userPreferences.setWeeklyReport(enabled) }
    }

    fun onToggleLike(enabled: Boolean) {
        _uiState.update { it.copy(likeEnabled = enabled) }
        persist { userPreferences.setLikeEnabled(enabled) }
    }

    fun onToggleComment(enabled: Boolean) {
        _uiState.update { it.copy(commentEnabled = enabled) }
        persist { userPreferences.setCommentEnabled(enabled) }
    }

    private fun persist(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
