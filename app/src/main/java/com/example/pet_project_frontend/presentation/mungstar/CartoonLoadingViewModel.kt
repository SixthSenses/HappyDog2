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

    init {
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
            
            while (_uiState.value.isLoading && !_uiState.value.isCancelled) {
                try {
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
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                
                                // 만화 이미지로 게시글 자동 생성
                                if (status.resultImageUrl != null) {
                                    createCartoonPost(status.resultImageUrl)
                                }
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

    /**
     * 완성된 만화 이미지로 게시글 생성
     */
    private fun createCartoonPost(imageUrl: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "createCartoonPost: Creating post with imageUrl=$imageUrl")
                
                // 빈 텍스트와 만화 이미지 URL로 게시글 생성
                val result = postRepository.createPost(
                    text = "", // 만화는 텍스트 없이
                    filePaths = listOf(imageUrl)
                )
                
                result.onSuccess { post ->
                    Log.d(TAG, "createCartoonPost: Post created successfully, postId=${post.postId}")
                    _uiState.value = _uiState.value.copy(isCompleted = true)
                }.onFailure { error ->
                    Log.e(TAG, "createCartoonPost: Failed to create post", error)
                    _uiState.value = _uiState.value.copy(
                        error = "게시글 생성에 실패했습니다: ${error.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "createCartoonPost: Exception", e)
                _uiState.value = _uiState.value.copy(
                    error = "게시글 생성 중 오류가 발생했습니다"
                )
            }
        }
    }
}
