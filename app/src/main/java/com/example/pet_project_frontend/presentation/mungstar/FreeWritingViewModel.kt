package com.example.pet_project_frontend.presentation.mungstar

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FreeWritingUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val uploadedImages: List<Uri> = emptyList(),
    val uploadProgress: Map<Uri, Float> = emptyMap(), // Uri별 업로드 진행률
    val isEditMode: Boolean = false,
    val editPostId: String? = null,
    val existingText: String = "",
    val existingImageUrls: List<String> = emptyList()
)

@HiltViewModel
class FreeWritingViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FreeWritingUiState())
    val uiState: StateFlow<FreeWritingUiState> = _uiState.asStateFlow()
    
    // postId로 게시글 불러오기 (수정 모드)
    fun loadPostForEdit(postId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = postRepository.getPost(postId)
            if (result.isSuccess) {
                val post = result.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isEditMode = true,
                    editPostId = postId,
                    existingText = post.text,
                    existingImageUrls = post.mediaUrls
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "게시글을 불러올 수 없습니다"
                )
            }
        }
    }
    
    // 수정 모드 초기화 (기존 메서드 유지 - 호환성)
    fun initEditMode(postId: String, text: String, imageUrls: List<String>) {
        _uiState.value = _uiState.value.copy(
            isEditMode = true,
            editPostId = postId,
            existingText = text,
            existingImageUrls = imageUrls
        )
    }

    // 이미지 추가
    fun addImages(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(
            uploadedImages = _uiState.value.uploadedImages + uris
        )
    }

    // 이미지 제거
    fun removeImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            uploadedImages = _uiState.value.uploadedImages.filter { it != uri }
        )
    }

    // 게시글 작성
    fun createPost(text: String) {
        Log.d(TAG, "createPost called - text: ${text.take(50)}, images: ${_uiState.value.uploadedImages.size}")
        
        // 텍스트 검증
        if (text.isBlank()) {
            Log.w(TAG, "Text is blank")
            _uiState.value = _uiState.value.copy(error = "내용을 입력해주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 1. 이미지 업로드 (이미지가 있는 경우에만)
                val filePaths = mutableListOf<String>()
                
                for ((index, uri) in _uiState.value.uploadedImages.withIndex()) {
                    Log.d(TAG, "Uploading image $index: $uri")
                    
                    // 업로드 URL 생성
                    val filename = "post_${System.currentTimeMillis()}_$index.jpg"
                    val uploadUrlResult = postRepository.getUploadUrl(
                        contentType = "image/jpeg",
                        uploadType = "post_image",
                        filename = filename
                    )

                    if (uploadUrlResult.isFailure) {
                        val errorMsg = "업로드 URL 생성 실패: ${uploadUrlResult.exceptionOrNull()?.message}"
                        Log.e(TAG, errorMsg, uploadUrlResult.exceptionOrNull())
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMsg
                        )
                        return@launch
                    }

                    val uploadUrl = uploadUrlResult.getOrNull()!!
                    Log.d(TAG, "Got upload URL: ${uploadUrl.uploadUrl}")

                    // 파일 업로드
                    val uploadResult = postRepository.uploadFile(
                        uploadUrl = uploadUrl.uploadUrl,
                        fileUri = uri,
                        contentType = "image/jpeg"
                    )

                    if (uploadResult.isFailure) {
                        val errorMsg = "파일 업로드 실패: ${uploadResult.exceptionOrNull()?.message}"
                        Log.e(TAG, errorMsg, uploadResult.exceptionOrNull())
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMsg
                        )
                        return@launch
                    }

                    Log.d(TAG, "File uploaded successfully")
                    Log.d(TAG, "filePath: ${uploadUrl.filePath}")
                    Log.d(TAG, "publicUrl: ${uploadUrl.publicUrl}")
                    
                    // ⚠️ 원본 filePath를 그대로 사용 (Firebase 인증 토큰 포함)
                    // publicUrl이 있으면 사용, 없으면 filePath 그대로 사용
                    val urlToUse = uploadUrl.publicUrl ?: uploadUrl.filePath
                    
                    Log.d(TAG, "Using URL: $urlToUse")
                    filePaths.add(urlToUse)
                }

                // 2. 게시글 생성
                Log.d(TAG, "Creating post with ${filePaths.size} images")
                val createResult = postRepository.createPost(
                    text = text,
                    filePaths = filePaths
                )

                if (createResult.isSuccess) {
                    Log.d(TAG, "Post created successfully")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                } else {
                    val errorMsg = "게시글 작성 실패: ${createResult.exceptionOrNull()?.message}"
                    Log.e(TAG, errorMsg, createResult.exceptionOrNull())
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                }
            } catch (e: Exception) {
                val errorMsg = "오류 발생: ${e.message}"
                Log.e(TAG, errorMsg, e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMsg
                )
            }
        }
    }

    // 게시글 수정
    fun updatePost(text: String) {
        val postId = _uiState.value.editPostId
        if (postId == null) {
            _uiState.value = _uiState.value.copy(error = "수정할 게시글 정보가 없습니다")
            return
        }
        
        Log.d(TAG, "updatePost called - postId: $postId, text: ${text.take(50)}")
        
        // 텍스트 검증
        if (text.isBlank()) {
            Log.w(TAG, "Text is blank")
            _uiState.value = _uiState.value.copy(error = "내용을 입력해주세요")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = postRepository.updatePost(postId, text)
                
                if (result.isSuccess) {
                    Log.d(TAG, "Post updated successfully")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                } else {
                    val errorMsg = "게시글 수정 실패: ${result.exceptionOrNull()?.message}"
                    Log.e(TAG, errorMsg, result.exceptionOrNull())
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                }
            } catch (e: Exception) {
                val errorMsg = "오류 발생: ${e.message}"
                Log.e(TAG, errorMsg, e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMsg
                )
            }
        }
    }

    // 에러 초기화
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // 성공 상태 초기화
    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
    
    companion object {
        private const val TAG = "FreeWritingViewModel"
    }
}
