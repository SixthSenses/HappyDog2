package com.example.pet_project_frontend.presentation.mypage.profile.name

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NameEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

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

    fun onSave(onSuccess: () -> Unit) {
        val name = _uiState.value.text.trim()
        val err = validate(name)
        if (err != null) {
            _uiState.update { it.copy(error = err) }
            return
        }
        // TODO: Repository 연동(Flask/Firebase) 예정. 현재는 UX용 로딩만.
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            delay(300)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }

    private fun validate(input: String): String? {
        val t = input.trim()
        if (t.isEmpty()) return "이름을 입력해 주세요."
        if (t.length !in 1..12) return "이름은 1~12자까지 입력할 수 있어요."
        val ok = Regex("^[가-힣a-zA-Z0-9 ]+$").matches(t)
        return if (ok) null else "한글, 영문, 숫자만 사용할 수 있어요."
    }
}
