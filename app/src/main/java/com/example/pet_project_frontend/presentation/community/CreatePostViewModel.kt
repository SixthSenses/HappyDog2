package com.example.pet_project_frontend.presentation.community

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.UploadType
import com.example.pet_project_frontend.domain.model.CartoonJobStatus
import com.example.pet_project_frontend.domain.repository.CartoonJobRepository
import com.example.pet_project_frontend.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * 게시글 작성 화면 ViewModel
 */
@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val cartoonJobRepository: CartoonJobRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()
    
    private var pollingJob: Job? = null
    
    fun addImages(uris: List<Uri>) {
        val currentImages = _uiState.value.imageUris
        val newImages = (currentImages + uris).take(10) // 최대 10장
        _uiState.update { it.copy(imageUris = newImages) }
    }
    
    fun removeImage(uri: Uri) {
        _uiState.update { 
            it.copy(imageUris = it.imageUris.filter { u -> u != uri })
        }
    }
    
    fun updateText(text: String) {
        if (text.length <= 2000) {
            _uiState.update { it.copy(text = text) }
        }
    }
    
    fun toggleCartoonMode() {
        _uiState.update { it.copy(isCartoonMode = !it.isCartoonMode) }
    }
    
    /**
     * 게시글 제출
     */
    fun submitPost(getFileFromUri: (Uri) -> File?) = viewModelScope.launch {
        if (_uiState.value.isSubmitting) return@launch
        
        val text = _uiState.value.text
        val imageUris = _uiState.value.imageUris
        val isCartoonMode = _uiState.value.isCartoonMode
        
        if (text.isBlank()) {
            _uiState.update { it.copy(errorMessage = "텍스트를 입력해주세요") }
            return@launch
        }
        
        if (imageUris.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "이미지를 선택해주세요") }
            return@launch
        }
        
        _uiState.update { 
            it.copy(
                isSubmitting = true,
                uploadProgress = emptyMap(),
                errorMessage = null,
                currentStep = "이미지 업로드 중..."
            )
        }
        
        try {
            // 1. 이미지 업로드
            val filePaths = uploadImages(imageUris, getFileFromUri)
            
            // 2. 만화 변환 (선택 사항)
            val finalFilePaths = if (isCartoonMode && filePaths.isNotEmpty()) {
                convertToCartoon(filePaths.first())
            } else {
                filePaths
            }
            
            // 3. 게시글 생성
            _uiState.update { it.copy(currentStep = "게시글 생성 중...") }
            
            val idempotencyKey = UUID.randomUUID().toString()
            val result = communityRepository.createPost(
                text = text,
                filePaths = finalFilePaths,
                idempotencyKey = idempotencyKey
            )
            
            when (result) {
                is AppResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isSubmitting = false,
                            isPostCreated = true,
                            currentStep = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message ?: "게시글 생성 실패",
                            currentStep = null
                        )
                    }
                }
                is AppResult.Exception -> {
                    _uiState.update { 
                        it.copy(
                            isSubmitting = false,
                            errorMessage = "네트워크 오류가 발생했습니다",
                            currentStep = null
                        )
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    isSubmitting = false,
                    errorMessage = "오류가 발생했습니다: ${e.message}",
                    currentStep = null
                )
            }
        }
    }
    
    /**
     * 이미지 업로드
     */
    private suspend fun uploadImages(
        uris: List<Uri>,
        getFileFromUri: (Uri) -> File?
    ): List<String> {
        val filePaths = mutableListOf<String>()
        
        for ((index, uri) in uris.withIndex()) {
            _uiState.update { 
                it.copy(currentStep = "이미지 업로드 중... (${index + 1}/${uris.size})")
            }
            
            val file = getFileFromUri(uri) ?: continue
            
            val result = cartoonJobRepository.uploadFile(
                file = file,
                uploadType = if (_uiState.value.isCartoonMode && index == 0) {
                    UploadType.CARTOON_SOURCE_IMAGE
                } else {
                    UploadType.POST_IMAGE
                },
                contentType = "image/jpeg"
            )
            
            when (result) {
                is AppResult.Success -> {
                    filePaths.add(result.data)
                    _uiState.update { state ->
                        state.copy(
                            uploadProgress = state.uploadProgress + (uri to 1f)
                        )
                    }
                }
                is AppResult.Error -> {
                    throw Exception(result.message ?: "업로드 실패")
                }
                is AppResult.Exception -> {
                    throw result.throwable
                }
            }
        }
        
        return filePaths
    }
    
    /**
     * 만화 변환
     */
    private suspend fun convertToCartoon(filePath: String): List<String> {
        _uiState.update { it.copy(currentStep = "만화 변환 중...") }
        
        // 1. 만화 변환 작업 생성
        val createResult = cartoonJobRepository.createCartoonJob(
            filePath = filePath,
            userText = null
        )
        
        val jobId = when (createResult) {
            is AppResult.Success -> createResult.data.jobId
            is AppResult.Error -> throw Exception(createResult.message ?: "변환 작업 생성 실패")
            is AppResult.Exception -> throw createResult.throwable
        }
        
        // 2. 작업 상태 폴링
        _uiState.update { it.copy(cartoonJobId = jobId) }
        
        var retryCount = 0
        val maxRetries = 60 // 최대 2분 (2초 * 60)
        
        while (retryCount < maxRetries) {
            delay(2000) // 2초 대기
            
            val statusResult = cartoonJobRepository.getCartoonJob(jobId)
            
            when (statusResult) {
                is AppResult.Success -> {
                    val job = statusResult.data
                    
                    when (job.status) {
                        CartoonJobStatus.COMPLETED -> {
                            val resultUrl = job.resultImageUrl 
                                ?: throw Exception("결과 이미지 URL이 없습니다")
                            return listOf(resultUrl)
                        }
                        CartoonJobStatus.FAILED -> {
                            throw Exception(job.errorMessage ?: "변환 실패")
                        }
                        CartoonJobStatus.CANCELLED -> {
                            throw Exception("변환이 취소되었습니다")
                        }
                        CartoonJobStatus.PENDING, CartoonJobStatus.PROCESSING -> {
                            retryCount++
                            _uiState.update { 
                                it.copy(currentStep = "만화 변환 중... ($retryCount/$maxRetries)")
                            }
                        }
                    }
                }
                is AppResult.Error -> {
                    throw Exception(statusResult.message ?: "상태 조회 실패")
                }
                is AppResult.Exception -> {
                    throw statusResult.throwable
                }
            }
        }
        
        throw Exception("변환 시간이 초과되었습니다")
    }
    
    fun cancelCartoonJob() = viewModelScope.launch {
        val jobId = _uiState.value.cartoonJobId ?: return@launch
        
        cartoonJobRepository.cancelCartoonJob(jobId)
        
        _uiState.update { 
            it.copy(
                isSubmitting = false,
                cartoonJobId = null,
                currentStep = null,
                errorMessage = "변환이 취소되었습니다"
            )
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun resetState() {
        _uiState.value = CreatePostUiState()
    }
}

/**
 * 게시글 작성 UI 상태
 */
data class CreatePostUiState(
    val imageUris: List<Uri> = emptyList(),
    val text: String = "",
    val isCartoonMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val uploadProgress: Map<Uri, Float> = emptyMap(),
    val cartoonJobId: String? = null,
    val currentStep: String? = null,
    val errorMessage: String? = null,
    val isPostCreated: Boolean = false
)
