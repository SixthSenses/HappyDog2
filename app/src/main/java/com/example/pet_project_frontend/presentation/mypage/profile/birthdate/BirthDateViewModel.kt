package com.example.pet_project_frontend.presentation.mypage.profile.birth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class BirthEditUiState(
    val text: String = "",      // YYYY/MM/DD
    val error: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class BirthEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BirthEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // 네비게이션에서 이전 설정값을 전달했다면 반영 (없으면 빈값)
        val initial = savedStateHandle.get<String>("initialBirth") ?: ""
        _uiState.update { it.copy(text = initial, error = null) }
    }

    fun onTextChange(new: String) {
        _uiState.update { it.copy(text = new, error = null) }
    }

    fun onClear() {
        _uiState.update { it.copy(text = "", error = null) }
    }

    fun onSave(onSuccess: () -> Unit) {
        // TODO: 저장 로직(Repository) 연결
        onSuccess()
    }
}
