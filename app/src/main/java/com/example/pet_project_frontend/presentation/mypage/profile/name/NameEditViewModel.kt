// 변경 의도: 네비게이션 인자로 전달된 펫 ID를 활용해 이름 편집 결과를 서버에 반영하고 검증 상태를 명확히 분리.
package com.example.pet_project_frontend.presentation.mypage.profile.name

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.PetUpdateRequest
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NameEditUiState(
    val text: String = "",
    val error: String? = null,
    val isSaving: Boolean = false,
    val isValidationError: Boolean = false
)

@HiltViewModel
class NameEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository
) : ViewModel() {

    private val petId: String? = savedStateHandle
        .get<String>("petId")
        ?.takeUnless { it == "null" || it.isBlank() }

    private val _uiState = MutableStateFlow(NameEditUiState())
    val uiState: StateFlow<NameEditUiState> = _uiState

    init {
        val initialName = savedStateHandle
            .get<String>("initialName")
            ?.takeUnless { it == "null" }
            ?.trim()
            .orEmpty()
        _uiState.value = _uiState.value.copy(text = initialName)
    }

    fun onTextChange(newText: String) {
        _uiState.value = _uiState.value.copy(
            text = newText,
            error = null,
            isValidationError = false
        )
    }

    fun onClear() {
        _uiState.value = _uiState.value.copy(
            text = "",
            error = null,
            isValidationError = false
        )
    }

    fun onSave(onSaved: (String, Boolean) -> Unit) {
        val trimmed = _uiState.value.text.trim()
        val validationError = validate(trimmed)
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(
                error = validationError,
                isValidationError = true
            )
            return
        }

        val targetPetId = petId
        if (targetPetId.isNullOrBlank()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isSaving = true, error = null, isValidationError = false)
                _uiState.value = _uiState.value.copy(isSaving = false)
                onSaved(trimmed, false)
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, isValidationError = false)
            val request = PetUpdateRequest(name = trimmed)
            when (val result = petRepository.updatePetProfile(targetPetId, request)) {
                is AppResult.Success -> {
                    val updatedName = result.data.name.takeUnless { it.isNullOrBlank() } ?: trimmed
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    onSaved(updatedName, true)
                }
                is AppResult.Error -> {
                    val message = result.message ?: GENERIC_SAVE_ERROR_MESSAGE
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = message,
                        isValidationError = false
                    )
                }
                is AppResult.Exception -> {
                    val message = result.throwable.message ?: GENERIC_SAVE_ERROR_MESSAGE
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = message,
                        isValidationError = false
                    )
                }
            }
        }
    }

    private fun validate(name: String): String? {
        if (name.isBlank()) return VALIDATION_ERROR_MESSAGE
        if (name.length > MAX_NAME_LENGTH) return VALIDATION_ERROR_MESSAGE
        return null
    }

    companion object {
        private const val MAX_NAME_LENGTH = 10
        private const val VALIDATION_ERROR_MESSAGE = "이름을 다시 확인해주세요."
        private const val GENERIC_SAVE_ERROR_MESSAGE = "이름 저장에 실패했습니다. 다시 시도해주세요."
    }
}
