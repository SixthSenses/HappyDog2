package com.example.pet_project_frontend.presentation.mypage.settings.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.local.preferences.UserPreferences
import com.example.pet_project_frontend.domain.model.NotificationPreferences
import com.example.pet_project_frontend.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    companion object {
        private const val TAG = "NotificationSettingsVM"
    }

    // UI에서 사용할 알림 토글 상태
    data class UiState(
        val weeklyReport: Boolean = true,
        val likeEnabled: Boolean = true,
        val commentEnabled: Boolean = true,
        val loading: Boolean = true,
        val error: String? = null,
        val isSyncing: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 서버에서 가져온 최신 설정 (otherTypes 보존용)
    private var serverPreferences: NotificationPreferences? = null

    init {
        loadPreferences()
    }

    /**
     * 알림 설정 로드 전략:
     * 1. 먼저 로컬 DataStore에서 로드 (빠른 초기 렌더링)
     * 2. 동시에 서버에서 최신 설정 조회
     * 3. 서버 설정을 UI에 반영하고 로컬 DataStore에도 저장 (동기화)
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            // 1단계: 로컬 DataStore에서 빠르게 로드
            try {
                userPreferences.notificationSettings.collect { localPrefs ->
                    _uiState.update { 
                        it.copy(
                            weeklyReport = localPrefs.weeklyReport,
                            likeEnabled = localPrefs.likeEnabled,
                            commentEnabled = localPrefs.commentEnabled,
                            loading = false
                        )
                    }
                    // 첫 로드 후 collect 종료
                    return@collect
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load from DataStore", e)
            }

            // 2단계: 서버에서 최신 설정 조회
            fetchFromServer()
        }
    }

    /**
     * 서버에서 알림 설정 조회 및 로컬 동기화
     */
    private suspend fun fetchFromServer() {
        Log.d(TAG, "Fetching notification preferences from server")
        
        when (val result = userRepository.getNotificationPreferences()) {
            is AppResult.Success -> {
                val prefs = result.data
                serverPreferences = prefs
                
                Log.d(TAG, "Fetched from server: weeklyReport=${prefs.weeklyReport}, like=${prefs.likeEnabled}, comment=${prefs.commentEnabled}")
                
                // UI 업데이트
                _uiState.update { 
                    it.copy(
                        weeklyReport = prefs.weeklyReport,
                        likeEnabled = prefs.likeEnabled,
                        commentEnabled = prefs.commentEnabled,
                        loading = false,
                        error = null
                    )
                }
                
                // 로컬 DataStore에도 저장 (서버-로컬 동기화)
                syncToLocal(prefs)
            }
            is AppResult.Error -> {
                Log.e(TAG, "Failed to fetch from server: ${result.message}")
                _uiState.update { 
                    it.copy(
                        loading = false,
                        error = "알림 설정을 불러올 수 없습니다: ${result.message}"
                    )
                }
            }
            is AppResult.Exception -> {
                Log.e(TAG, "Network exception fetching from server", result.throwable)
                _uiState.update { 
                    it.copy(
                        loading = false,
                        error = "네트워크 연결을 확인해주세요"
                    )
                }
            }
        }
    }

    /**
     * 서버 설정을 로컬 DataStore에 동기화
     */
    private suspend fun syncToLocal(prefs: NotificationPreferences) {
        try {
            userPreferences.setWeeklyReport(prefs.weeklyReport)
            userPreferences.setLikeEnabled(prefs.likeEnabled)
            userPreferences.setCommentEnabled(prefs.commentEnabled)
            Log.d(TAG, "Synced server preferences to local DataStore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync to local DataStore", e)
        }
    }

    /**
     * 주간 리포트 토글 변경
     * 1. UI 즉시 업데이트 (낙관적 업데이트)
     * 2. 로컬 DataStore 저장
     * 3. 서버 API 호출 (백그라운드)
     */
    fun onToggleWeekly(enabled: Boolean) {
        Log.d(TAG, "Toggle weekly report: $enabled")
        
        // 낙관적 UI 업데이트
        _uiState.update { it.copy(weeklyReport = enabled, isSyncing = true) }
        
        viewModelScope.launch {
            // 로컬 저장
            userPreferences.setWeeklyReport(enabled)
            
            // 서버 업데이트
            updateServer(
                _uiState.value.copy(weeklyReport = enabled)
            )
        }
    }

    fun onToggleLike(enabled: Boolean) {
        Log.d(TAG, "Toggle like notifications: $enabled")
        
        _uiState.update { it.copy(likeEnabled = enabled, isSyncing = true) }
        
        viewModelScope.launch {
            userPreferences.setLikeEnabled(enabled)
            updateServer(
                _uiState.value.copy(likeEnabled = enabled)
            )
        }
    }

    fun onToggleComment(enabled: Boolean) {
        Log.d(TAG, "Toggle comment notifications: $enabled")
        
        _uiState.update { it.copy(commentEnabled = enabled, isSyncing = true) }
        
        viewModelScope.launch {
            userPreferences.setCommentEnabled(enabled)
            updateServer(
                _uiState.value.copy(commentEnabled = enabled)
            )
        }
    }

    /**
     * 서버에 변경사항 전송
     * 
     * UI 상태를 NotificationPreferences로 변환하여 서버에 업데이트
     * otherTypes는 서버에서 가져온 최신값 유지 (UI에 없는 타입들)
     */
    private suspend fun updateServer(uiState: UiState) {
        val prefs = NotificationPreferences(
            mode = serverPreferences?.mode ?: "both",
            weeklyReport = uiState.weeklyReport,
            likeEnabled = uiState.likeEnabled,
            commentEnabled = uiState.commentEnabled,
            otherTypes = serverPreferences?.otherTypes ?: emptyMap()
        )
        
        Log.d(TAG, "Updating server with: $prefs")
        
        when (val result = userRepository.updateNotificationPreferences(prefs)) {
            is AppResult.Success -> {
                serverPreferences = result.data
                _uiState.update { it.copy(isSyncing = false, error = null) }
                Log.d(TAG, "Server updated successfully")
            }
            is AppResult.Error -> {
                Log.e(TAG, "Failed to update server: ${result.message}")
                _uiState.update { 
                    it.copy(
                        isSyncing = false,
                        error = "설정 저장 실패: ${result.message}"
                    )
                }
                // 에러 시 로컬과 서버 재동기화 시도
                fetchFromServer()
            }
            is AppResult.Exception -> {
                Log.e(TAG, "Network exception updating server", result.throwable)
                _uiState.update { 
                    it.copy(
                        isSyncing = false,
                        error = "네트워크 오류: 변경사항이 저장되지 않았을 수 있습니다"
                    )
                }
            }
        }
    }

    /**
     * 에러 메시지 해제
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
