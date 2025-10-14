package com.example.pet_project_frontend.presentation.care_record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.PetCareSettingsRequest
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedRecordViewModel @Inject constructor(
    private val petCareRepository: PetCareRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSaveSuccess: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 사료 목표 섭취 횟수 저장
     */
    fun saveFeedGoal(targetCount: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // 최소한의 API 호출로 펫 ID만 가져오기
                val petResult = petRepository.getMyPetProfile()
                if (petResult !is AppResult.Success) {
                    handleError("펫 정보를 확인할 수 없습니다.")
                    return@launch
                }
                
                val petId = petResult.data.id
                
                // 목표 사료 횟수만 업데이트 (부분 업데이트)
                val updateRequest = PetCareSettingsRequest(targetDailyMealCount = targetCount)
                
                val updateResult = petCareRepository.updatePetCareSettings(petId, updateRequest)
                when (updateResult) {
                    is AppResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSaveSuccess = true
                        )
                    }
                    is AppResult.Error -> {
                        handleError(updateResult.message ?: "사료 목표 저장에 실패했습니다.")
                    }
                    is AppResult.Exception -> {
                        handleError(updateResult.throwable.message ?: "사료 목표 저장에 실패했습니다.")
                    }
                }
                
            } catch (e: Exception) {
                handleError(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }
    
    private fun handleError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = message
        )
    }

    /**
     * 에러 상태 초기화
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 성공 상태 초기화
     */
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSaveSuccess = false)
    }
}