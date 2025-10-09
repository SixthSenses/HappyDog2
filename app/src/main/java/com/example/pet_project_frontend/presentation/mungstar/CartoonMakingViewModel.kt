package com.example.pet_project_frontend.presentation.mungstar

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.repository.CartoonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartoonMakingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedImages: List<Uri> = emptyList(),
    val jobId: String? = null // 생성된 작업 ID
)

@HiltViewModel
class CartoonMakingViewModel @Inject constructor(
    private val cartoonRepository: CartoonRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CartoonMakingViewModel"
    }

    private val _uiState = MutableStateFlow(CartoonMakingUiState())
    val uiState: StateFlow<CartoonMakingUiState> = _uiState.asStateFlow()

    // 이미지 추가
    fun addImages(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(
            selectedImages = _uiState.value.selectedImages + uris
        )
    }

    // 이미지 제거
    fun removeImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedImages = _uiState.value.selectedImages.filter { it != uri }
        )
    }

    // 만화 생성 시작
    fun generateCartoon(text: String) {
        val selectedImages = _uiState.value.selectedImages
        if (selectedImages.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "이미지를 선택해주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                Log.d(TAG, "generateCartoon: Starting, text=$text, imageCount=${selectedImages.size}")
                
                // 첫 번째 이미지로 만화 작업 생성 (API는 1개만 허용)
                val imageUri = selectedImages.first()
                val userText = text.ifBlank { null }
                
                val result = cartoonRepository.createCartoonJob(userText, imageUri)
                
                result.onSuccess { jobId ->
                    Log.d(TAG, "generateCartoon: Success, jobId=$jobId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        jobId = jobId
                    )
                }.onFailure { error ->
                    Log.e(TAG, "generateCartoon: Failed", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "만화 생성에 실패했습니다"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "generateCartoon: Exception", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "알 수 없는 오류가 발생했습니다"
                )
            }
        }
    }

    // 에러 클리어
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // jobId 초기화
    fun resetJobId() {
        _uiState.value = _uiState.value.copy(jobId = null)
    }
}
