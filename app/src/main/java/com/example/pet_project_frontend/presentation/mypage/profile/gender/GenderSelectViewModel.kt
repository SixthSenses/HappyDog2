// 변경의도: SavedStateHandle 초기값을 활용하고 저장 시 MyPage가 즉시 반영되도록 임시 콜백 구조를 확장한다.
package com.example.pet_project_frontend.presentation.mypage.profile.gender

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class GenderUi { MALE, FEMALE }

data class GenderUiState(
    val selected: GenderUi? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GenderSelectViewModel @Inject constructor(
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

    fun onSave(onSuccess: (GenderUi, Boolean) -> Unit) = viewModelScope.launch {
        val selected = _ui.value.selected ?: run {
            _ui.value = _ui.value.copy(error = "성별을 선택해 주세요")
            return@launch
        }

        _ui.value = _ui.value.copy(isSaving = true, error = null)

        // TODO 서버 연동 시 치환: 성별 수정 API 연동 후 응답 기반으로 처리
        runCatching { selected }
            .onSuccess { resolved ->
                _ui.value = _ui.value.copy(isSaving = false)
                onSuccess(resolved, false)
            }
            .onFailure {
                _ui.value = _ui.value.copy(isSaving = false, error = "요청 처리에 실패했어요")
            }
    }
}
