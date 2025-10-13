package com.example.pet_project_frontend.presentation.mypage.profile.gender

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI에서 사용할 성별 타입
enum class GenderUi { MALE, FEMALE }

data class GenderUiState(
    val selected: GenderUi? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GenderSelectViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _ui = MutableStateFlow(GenderUiState())
    val uiState = _ui.asStateFlow()

    init {
        val initialRaw = savedStateHandle.get<String>("initialGender")?.lowercase(Locale.getDefault())
        val initial = when (initialRaw) {
            "male", "수컷" -> GenderUi.MALE
            "female", "암컷" -> GenderUi.FEMALE
            else -> null
        }
        _ui.value = _ui.value.copy(selected = initial)
    }

    fun onSelect(gender: GenderUi) {
        _ui.value = _ui.value.copy(selected = gender, error = null)
    }

    /**
     * 성별 저장
     * OpenAPI PetUpdateSchema: gender 필드 업데이트 가능 (2025-10-13 백엔드 업데이트)
     * PATCH /api/pets/{pet_id} with { gender: "MALE" | "FEMALE" }
     */
    fun onSave(petId: String, onSuccess: (GenderUi) -> Unit) = viewModelScope.launch {
        val selected = _ui.value.selected ?: run {
            _ui.update { it.copy(error = "성별을 선택해 주세요") }
            return@launch
        }

        _ui.update { it.copy(isSaving = true, error = null) }
        
        // GenderUi -> API 포맷 변환
        val genderValue = when (selected) {
            GenderUi.MALE -> "MALE"
            GenderUi.FEMALE -> "FEMALE"
        }
        
        val request = UpdatePetRequest(gender = genderValue)
        
        when (val result = petRepository.updatePetProfile(petId, request)) {
            is AppResult.Success -> {
                _ui.update { it.copy(isSaving = false) }
                onSuccess(selected)
            }
            is AppResult.Error -> {
                _ui.update {
                    it.copy(
                        isSaving = false,
                        error = result.message ?: "성별 업데이트에 실패했어요"
                    )
                }
            }
            is AppResult.Exception -> {
                _ui.update {
                    it.copy(
                        isSaving = false,
                        error = result.throwable.message ?: "네트워크 오류가 발생했어요"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _ui.update { it.copy(error = null) }
    }
}
