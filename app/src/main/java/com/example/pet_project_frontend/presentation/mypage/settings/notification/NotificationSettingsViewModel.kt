// 변경의도: 알림 토글 상태를 DataStore와 동기화하고 서버 연동까지 고려한 임시 저장 흐름을 구성한다.
package com.example.pet_project_frontend.presentation.mypage.settings.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.local.preferences.UserPreferences
import com.example.pet_project_frontend.domain.repository.UserRepository
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
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "NotificationSettingsVM"
    }

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
        // 초기 로딩: 서버에서 알림 설정 로드 후 로컬 DataStore와 동기화
        viewModelScope.launch {
            Log.d(TAG, "Initializing notification settings")
            
            // 1. 로컬 DataStore에서 먼저 로드 (빠른 UI 표시)
            userPreferences.notificationSettings.collectLatest { localPrefs ->
                _uiState.update {
                    it.copy(
                        pushEnabled = localPrefs.pushEnabled,
                        weeklyReport = localPrefs.weeklyReport,
                        likeEnabled = localPrefs.likeEnabled,
                        commentEnabled = localPrefs.commentEnabled,
                        loading = false
                    )
                }
            }
        }
        
        // 2. 백그라운드에서 서버 설정 동기화
        viewModelScope.launch {
            syncWithServer()
        }
    }
    
    /**
     * 서버에서 알림 설정을 가져와 로컬 DataStore 및 UI와 동기화
     */
    private suspend fun syncWithServer() {
        Log.d(TAG, "Syncing notification settings with server")
        
        when (val result = userRepository.getNotificationPreferences()) {
            is AppResult.Success -> {
                val serverPrefs = result.data
                Log.d(TAG, "Server preferences loaded: mode=${serverPrefs.mode}, weekly=${serverPrefs.weeklyReport}, like=${serverPrefs.likeEnabled}, comment=${serverPrefs.commentEnabled}")
                
                // 서버 설정을 로컬 DataStore에 반영
                userPreferences.setPushEnabled(serverPrefs.mode != "inapp") // "both" 또는 "push"면 true
                userPreferences.setWeeklyReport(serverPrefs.weeklyReport)
                userPreferences.setLikeEnabled(serverPrefs.likeEnabled)
                userPreferences.setCommentEnabled(serverPrefs.commentEnabled)
                
                // UI 상태 업데이트
                _uiState.update {
                    it.copy(
                        pushEnabled = serverPrefs.mode != "inapp",
                        weeklyReport = serverPrefs.weeklyReport,
                        likeEnabled = serverPrefs.likeEnabled,
                        commentEnabled = serverPrefs.commentEnabled,
                        loading = false
                    )
                }
            }
            is AppResult.Error -> {
                Log.e(TAG, "Failed to sync with server: ${result.message}")
                // 서버 동기화 실패 시 로컬 설정 유지
            }
            is AppResult.Exception -> {
                Log.e(TAG, "Exception while syncing with server", result.throwable)
                // 예외 발생 시 로컬 설정 유지
            }
        }
    }

    fun onTogglePush(enabled: Boolean) {
        Log.d(TAG, "onTogglePush: $enabled")
        _uiState.update { it.copy(pushEnabled = enabled) }
        persist {
            // 1. 로컬 DataStore에 저장
            userPreferences.setPushEnabled(enabled)
            
            // 2. 서버에 동기화
            updateServerPreferences()
        }
        if (!enabled) {
            // 정책: 전체 알림 비활성화 시 하위 항목도 함께 비활성화
            onToggleWeekly(false)
            onToggleLike(false)
            onToggleComment(false)
        }
    }

    fun onToggleWeekly(enabled: Boolean) {
        Log.d(TAG, "onToggleWeekly: $enabled")
        _uiState.update { it.copy(weeklyReport = enabled) }
        persist {
            // 1. 로컬 DataStore에 저장
            userPreferences.setWeeklyReport(enabled)
            
            // 2. 서버에 동기화 (PET_CARE_DAILY_SUMMARY 타입)
            updateServerPreferences()
        }
    }

    fun onToggleLike(enabled: Boolean) {
        Log.d(TAG, "onToggleLike: $enabled")
        _uiState.update { it.copy(likeEnabled = enabled) }
        persist {
            // 1. 로컬 DataStore에 저장
            userPreferences.setLikeEnabled(enabled)
            
            // 2. 서버에 동기화 (POST_LIKE + COMMENT_LIKE 타입)
            updateServerPreferences()
        }
    }

    fun onToggleComment(enabled: Boolean) {
        Log.d(TAG, "onToggleComment: $enabled")
        _uiState.update { it.copy(commentEnabled = enabled) }
        persist {
            // 1. 로컬 DataStore에 저장
            userPreferences.setCommentEnabled(enabled)
            
            // 2. 서버에 동기화 (COMMENT + MENTION 타입)
            updateServerPreferences()
        }
    }

    /**
     * 현재 UI 상태를 서버에 반영하는 헬퍼 함수
     * 
     * DataStore 저장과 서버 API 호출을 모두 수행
     */
    private suspend fun updateServerPreferences() {
        val currentState = _uiState.value
        
        // 1. UI 상태를 Domain Model로 변환
        val mode = when {
            !currentState.pushEnabled -> "inapp" // 푸시 비활성화 시 인앱만
            else -> "both" // 푸시 활성화 시 인앱+푸시
        }
        
        val preferences = com.example.pet_project_frontend.domain.model.NotificationPreferences(
            mode = mode,
            weeklyReport = currentState.weeklyReport,
            likeEnabled = currentState.likeEnabled,
            commentEnabled = currentState.commentEnabled
        )
        
        Log.d(TAG, "Updating server preferences: mode=$mode, weekly=${currentState.weeklyReport}, like=${currentState.likeEnabled}, comment=${currentState.commentEnabled}")
        
        // 2. 서버 API 호출
        when (val result = userRepository.updateNotificationPreferences(preferences)) {
            is AppResult.Success -> {
                Log.d(TAG, "Server preferences updated successfully")
            }
            is AppResult.Error -> {
                Log.e(TAG, "Failed to update server preferences: ${result.message}")
                // 서버 실패 시 로컬 설정은 유지 (UI는 이미 변경된 상태)
            }
            is AppResult.Exception -> {
                Log.e(TAG, "Exception while updating server preferences", result.throwable)
            }
        }
    }
    
    private fun persist(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
