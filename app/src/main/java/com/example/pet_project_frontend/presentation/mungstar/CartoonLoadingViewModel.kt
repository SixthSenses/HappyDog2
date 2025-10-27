package com.example.pet_project_frontend.presentation.mungstar

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.repository.CartoonRepository
import com.example.pet_project_frontend.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartoonLoadingViewModel @Inject constructor(
    private val cartoonRepository: CartoonRepository,
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "CartoonLoadingViewModel"
        private const val POLL_INTERVAL_MS = 2000L // 2초마다 상태 확인
        private const val MAX_POLL_DURATION_MS = 180000L // 최대 3분 (180초)
        private const val MAX_POLL_ATTEMPTS = (MAX_POLL_DURATION_MS / POLL_INTERVAL_MS).toInt() // 90회
    }

    data class CartoonLoadingUiState(
        val isLoading: Boolean = true,
        val isCompleted: Boolean = false,
        val isCancelled: Boolean = false,
        val error: String? = null,
        val currentStatus: String = "pending", // pending, processing, completed, failed, cancelled
        val resultImageUrl: String? = null
    )

    private val _uiState = MutableStateFlow(CartoonLoadingUiState())
    val uiState: StateFlow<CartoonLoadingUiState> = _uiState.asStateFlow()

    private val jobId: String = savedStateHandle.get<String>("jobId") ?: ""
    private val userText: String? = savedStateHandle.get<String>("userText")?.let { encodedText ->
        // Base64 디코딩
        if (encodedText.isNotBlank()) {
            try {
                val decodedBytes = android.util.Base64.decode(
                    encodedText,
                    android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
                )
                String(decodedBytes, Charsets.UTF_8)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode userText", e)
                null
            }
        } else {
            null
        }
    }
    
    // 중복 게시 방지 플래그
    private var hasCreatedPost = false

    init {
        Log.d(TAG, "init: ViewModel created with jobId=$jobId, userText=$userText")
        if (jobId.isNotEmpty()) {
            startPolling()
        } else {
            Log.e(TAG, "init: jobId is empty")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "작업 ID가 없습니다"
            )
        }
    }

    /**
     * 주기적으로 작업 상태 조회
     */
    private fun startPolling() {
        viewModelScope.launch {
            Log.d(TAG, "startPolling: Starting for jobId=$jobId")
            var pollAttempts = 0
            
            while (_uiState.value.isLoading && !_uiState.value.isCancelled && pollAttempts < MAX_POLL_ATTEMPTS) {
                try {
                    pollAttempts++
                    Log.d(TAG, "startPolling: Attempt $pollAttempts/$MAX_POLL_ATTEMPTS")
                    
                    val result = cartoonRepository.getCartoonJobStatus(jobId)
                    
                    result.onSuccess { status ->
                        Log.d(TAG, "startPolling: status=${status.status}")
                        
                        _uiState.value = _uiState.value.copy(
                            currentStatus = status.status,
                            resultImageUrl = status.resultImageUrl
                        )
                        
                        when (status.status) {
                            "completed" -> {
                                Log.d(TAG, "startPolling: Job completed, resultUrl=${status.resultImageUrl}")
                                
                                // 중복 방지: 플래그를 먼저 확인하고 즉시 설정
                                if (hasCreatedPost) {
                                    Log.d(TAG, "startPolling: Post already created, skipping")
                                    return@launch
                                }
                                
                                // 플래그를 먼저 설정하여 중복 호출 방지
                                hasCreatedPost = true
                                
                                // 폴링 즉시 중단
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                
                                // 백엔드에서 자동으로 게시물을 생성하므로
                                // 프론트엔드에서는 게시물을 생성하지 않고 단순히 완료 상태만 설정
                                Log.d(TAG, "startPolling: Backend will auto-create post, navigating to feed")
                                _uiState.value = _uiState.value.copy(isCompleted = true)
                                
                                return@launch
                            }
                            "failed" -> {
                                Log.e(TAG, "startPolling: Job failed, error=${status.errorMessage}")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = status.errorMessage ?: "만화 생성에 실패했습니다"
                                )
                                return@launch
                            }
                            "cancelled" -> {
                                Log.d(TAG, "startPolling: Job cancelled")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    isCancelled = true
                                )
                                return@launch
                            }
                            else -> {
                                // pending, processing 상태는 계속 폴링
                                Log.d(TAG, "startPolling: Job still in progress (${status.status})")
                            }
                        }
                    }.onFailure { error ->
                        Log.e(TAG, "startPolling: Failed to get status", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "상태 조회에 실패했습니다"
                        )
                        return@launch
                    }
                    
                    delay(POLL_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "startPolling: Exception", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "알 수 없는 오류가 발생했습니다"
                    )
                    return@launch
                }
            }
            
            // 최대 시도 횟수 초과 시 타임아웃 처리
            if (pollAttempts >= MAX_POLL_ATTEMPTS && _uiState.value.isLoading) {
                Log.e(TAG, "startPolling: Timeout after $pollAttempts attempts (${MAX_POLL_DURATION_MS}ms)")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "만화 생성 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."
                )
            }
        }
    }

    /**
     * 만화 작업 취소
     */
    fun cancelJob() {
        viewModelScope.launch {
            Log.d(TAG, "cancelJob: Cancelling jobId=$jobId")
            
            _uiState.value = _uiState.value.copy(isCancelled = true, isLoading = false)
            
            try {
                cartoonRepository.cancelCartoonJob(jobId)
                Log.d(TAG, "cancelJob: Cancel request sent")
            } catch (e: Exception) {
                Log.e(TAG, "cancelJob: Failed to cancel", e)
                // 취소 실패해도 UI는 뒤로 가기 처리
            }
        }
    }
}
