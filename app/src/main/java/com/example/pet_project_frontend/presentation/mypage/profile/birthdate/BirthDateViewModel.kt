// 변경의도: 저장 시 초기값을 SavedStateHandle에서 받아 화면에 반영하고, MyPage 갱신 신호를 보낼 수 있도록 임시 저장 흐름을 정리한다.
package com.example.pet_project_frontend.presentation.mypage.profile.birth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BirthEditUiState(
    val text: String = "",
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
        val initial = savedStateHandle.get<String>("initialBirth") ?: ""
        _uiState.update { it.copy(text = initial, error = null) }
    }

    fun onTextChange(new: String) {
        _uiState.update { it.copy(text = new, error = null) }
    }

    fun onClear() {
        _uiState.update { it.copy(text = "", error = null) }
    }

    fun onSave(onSuccess: (String, Boolean) -> Unit) {
        val input = _uiState.value.text.trim()
        val validationMessage = validateBirth(input)
        if (validationMessage != null) {
            _uiState.update { it.copy(error = validationMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            // TODO 서버 연동 시 치환: 생년월일 수정 API 연동 후 실제 응답 값으로 대체
            delay(150)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess(input, false)
        }
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

        if (year !in 1900..2100) return "연도 범위를 확인해 주세요"
        if (month !in 1..12) return "월은 1~12 범위여야 해요"

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
