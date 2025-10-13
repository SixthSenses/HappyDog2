package com.example.pet_project_frontend.presentation.mypage.profile.birth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BirthEditUiState(
    val text: String = "",      // YYYY/MM/DD
    val error: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class BirthEditViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BirthEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val initial = savedStateHandle.get<String>("initialBirth") ?: ""
        _uiState.update { it.copy(text = initial, error = null) }
    }

    fun onTextChange(new: String) {
        _uiState.update { it.copy(text = new, error = null) }
    }

    fun onClear() {
        _uiState.update { it.copy(text = "", error = null) }
    }

    /**
     * 생년월일 저장
     * OpenAPI PetUpdateSchema: birthdate 필드 업데이트 가능 (2025-10-13 백엔드 업데이트)
     * PATCH /api/pets/{pet_id} with { birthdate: "yyyy-MM-dd" }
     */
    fun onSave(petId: String, onSuccess: (String) -> Unit) {
        val input = _uiState.value.text.trim()
        val validationMessage = validateBirth(input)
        if (validationMessage != null) {
            _uiState.update { it.copy(error = validationMessage) }
            return
        }

        // YYYY/MM/DD -> yyyy-MM-dd 포맷 변환
        val birthdate = input.replace("/", "-")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            val request = UpdatePetRequest(birthdate = birthdate)
            
            when (val result = petRepository.updatePetProfile(petId, request)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess(input) // UI 포맷으로 전달
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = result.message ?: "생년월일 업데이트에 실패했어요"
                        )
                    }
                }
                is AppResult.Exception -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = result.throwable.message ?: "네트워크 오류가 발생했어요"
                        )
                    }
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateBirth(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return "생년월일을 입력해 주세요"

        val regex = Regex("""^\d{4}/\d{1,2}/\d{1,2}$""")
        if (!regex.matches(trimmed)) return "YYYY/MM/DD 형식으로 입력해 주세요"

        val parts = trimmed.split("/")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()

        if (year !in 1900..2100) return "연도 값을 확인해 주세요"
        if (month !in 1..12) return "월은 1~12 사이여야 해요"

        val maxDay = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
            else -> 31
        }
        if (day !in 1..maxDay) return "유효한 날짜가 아니에요"

        return null
    }
}
