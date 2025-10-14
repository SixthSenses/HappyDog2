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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityRecordViewModel @Inject constructor(
    private val petCareRepository: PetCareRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 펫 정보 상태 (직접 API 호출 방식으로 변경)
    private var currentPetId: String? = null

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isSuccess: Boolean = false
    )

    fun saveActivityGoal(
        sessionMinutes: Int,  // 1회 활동 시간
        dailySessions: Int    // 1일 목표 활동 횟수
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // 현재 펫 정보 가져오기
                val petResult = petRepository.getMyPetProfile()
                if (petResult !is AppResult.Success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "펫 정보를 확인할 수 없습니다."
                    )
                    return@launch
                }
                
                val petId = petResult.data.id
            
                val request = PetCareSettingsRequest(
                    activitySessionMinutes = sessionMinutes,  // 1회 활동 시간 (예: 30분)
                    targetDailyActivitySessions = dailySessions  // 1일 목표 활동 횟수 (예: 3회)
                )
                
                when (val result = petCareRepository.updatePetCareSettings(petId, request)) {
                    is AppResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                    is AppResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "활동 목표 저장에 실패했습니다."
                        )
                    }
                    is AppResult.Exception -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.throwable.message ?: "활동 목표 저장에 실패했습니다."
                        )
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessState() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}