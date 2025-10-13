package com.example.pet_project_frontend.presentation.mypage.profile.name

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NameEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository
) : ViewModel() {

    companion object {
        private const val TAG = "NameEditViewModel"
    }

    data class UiState(
        val text: String = "",
        val error: String? = null,
        val isSaving: Boolean = false
    )

    private val _uiState = MutableStateFlow(
        UiState(text = savedStateHandle.get<String>("initialName").orEmpty())
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // 네비게이션에서 넘겨준 초기 이름 (없으면 공백)
        val initial = savedStateHandle.get<String>("initialName") ?: ""
        _uiState.update { it.copy(text = initial, error = null) }
    }

    fun onTextChange(new: String) {
        _uiState.update { it.copy(text = new, error = validate(new)) }
    }

    fun onClear() {
        _uiState.update { it.copy(text = "", error = null) }
    }

    /**
     * 이름 저장 - PATCH /api/pets/{pet_id} 호출
     */
    fun onSave(petId: String, onSuccess: (String) -> Unit) {
        val name = _uiState.value.text.trim()
        val err = validate(name)
        if (err != null) {
            _uiState.update { it.copy(error = err) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            Log.d(TAG, "Updating pet name: $name for petId: $petId")
            val request = UpdatePetRequest(name = name)
            
            when (val result = petRepository.updatePetProfile(petId, request)) {
                is AppResult.Success -> {
                    Log.d(TAG, "Pet name updated successfully")
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess(name)
                }
                is AppResult.Error -> {
                    Log.e(TAG, "Failed to update pet name: ${result.message}")
                    _uiState.update { 
                        it.copy(
                            isSaving = false, 
                            error = result.message ?: "이름 업데이트 실패"
                        ) 
                    }
                }
                is AppResult.Exception -> {
                    Log.e(TAG, "Exception updating pet name", result.throwable)
                    _uiState.update { 
                        it.copy(
                            isSaving = false, 
                            error = "네트워크 오류가 발생했습니다"
                        ) 
                    }
                }
            }
        }
    }

    private fun validate(input: String): String? {
        val t = input.trim()
        if (t.isEmpty()) return "이름을 입력해 주세요."
        if (t.length !in 1..20) return "이름은 1~20자까지 입력할 수 있어요."
        val ok = Regex("^[가-힣a-zA-Z0-9 ]+$").matches(t)
        return if (ok) null else "한글, 영문, 숫자만 사용할 수 있어요."
    }
}
