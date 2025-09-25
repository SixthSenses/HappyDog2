package com.example.pet_project_frontend.presentation.eye_health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.model.EyeAnalysisHistoryItem
import com.example.pet_project_frontend.domain.usecase.eye_health.GetEyeAnalysisHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 안구 건강 히스토리 화면 ViewModel
 */
@HiltViewModel
class EyeHealthHistoryViewModel @Inject constructor(
    private val getEyeAnalysisHistoryUseCase: GetEyeAnalysisHistoryUseCase
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val historyItems: List<EyeAnalysisHistoryItem> = emptyList(),
        val error: String? = null,
        val nextCursor: String? = null,
        val hasMore: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    /**
     * 히스토리 조회 (초기 로드)
     */
    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getEyeAnalysisHistoryUseCase(
                petId = null, // 전체 조회
                limit = 20,
                cursor = null
            ).fold(
                onSuccess = { history ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        historyItems = history.items,
                        nextCursor = history.nextCursor,
                        hasMore = history.nextCursor != null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "히스토리 조회에 실패했습니다"
                    )
                }
            )
        }
    }

    /**
     * 더 많은 히스토리 로드 (페이지네이션)
     */
    fun loadMoreHistory() {
        val currentState = _uiState.value
        if (!currentState.hasMore || currentState.isLoading) return

        viewModelScope.launch {
            getEyeAnalysisHistoryUseCase(
                petId = null,
                limit = 20,
                cursor = currentState.nextCursor
            ).fold(
                onSuccess = { history ->
                    _uiState.value = currentState.copy(
                        historyItems = currentState.historyItems + history.items,
                        nextCursor = history.nextCursor,
                        hasMore = history.nextCursor != null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = currentState.copy(
                        error = exception.message ?: "추가 히스토리 로드에 실패했습니다"
                    )
                }
            )
        }
    }

    /**
     * 에러 상태 클리어
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * 새로고침
     */
    fun refresh() {
        loadHistory()
    }
}