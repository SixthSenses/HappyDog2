package com.example.pet_project_frontend.presentation.eye_health

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.EyeAnalysis
import com.example.pet_project_frontend.domain.repository.EyeHealthRepository
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 안구 건강 분석 ViewModel (MVVM + UDF 패턴)
 * Single Source of Truth 패턴으로 UI 상태 관리
 */
@HiltViewModel
class EyeHealthViewModel @Inject constructor(
    private val eyeHealthRepository: EyeHealthRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _analysis = MutableStateFlow<EyeAnalysis?>(null)
    private val _error = MutableStateFlow<String?>(null)
    private val _petId = MutableStateFlow<String?>(null)

    /**
     * UI State (Single Source of Truth)
     * WhileSubscribed(5000) 패턴으로 lifecycle-aware 구독
     */
    val uiState: StateFlow<EyeHealthUiState> = combine(
        _isLoading,
        _analysis,
        _error,
        _petId
    ) { isLoading, analysis, error, petId ->
        EyeHealthUiState(
            isLoading = isLoading,
            analysis = analysis,
            error = error,
            petId = petId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EyeHealthUiState()
    )

    init {
        loadPetId()
    }

    /**
     * 현재 사용자의 반려동물 ID 로드
     */
    private fun loadPetId() {
        viewModelScope.launch {
            when (val result = petRepository.getMyPetProfile()) {
                is AppResult.Success -> {
                    _petId.value = result.data.id
                }
                is AppResult.Error -> {
                    _error.value = "반려동물 정보를 불러올 수 없습니다"
                }
                is AppResult.Exception -> {
                    _error.value = "반려동물 정보를 불러올 수 없습니다"
                }
            }
        }
    }

    /**
     * 안구 이미지 분석 요청 (User Event Handler)
     */
    fun analyzeEyeHealth(imageUri: Uri) {
        val currentPetId = _petId.value
        if (currentPetId == null) {
            _error.value = "반려동물 정보가 없습니다"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            eyeHealthRepository.analyzeEyeHealth(currentPetId, imageUri)
                .onSuccess { analysis ->
                    _analysis.value = analysis
                }
                .onFailure { throwable ->
                    _error.value = throwable.message ?: "분석 중 오류가 발생했습니다"
                }
                .also {
                    _isLoading.value = false
                }
        }
    }

    /**
     * 에러 상태 클리어
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 분석 결과 초기화 (새 분석을 위해)
     */
    fun resetAnalysis() {
        _analysis.value = null
        _error.value = null
    }
}

/**
 * UI State Data Class
 * Immutable 상태 객체로 UI 렌더링 정보 캡슐화
 */
data class EyeHealthUiState(
    val isLoading: Boolean = false,
    val analysis: EyeAnalysis? = null,
    val error: String? = null,
    val petId: String? = null
)
