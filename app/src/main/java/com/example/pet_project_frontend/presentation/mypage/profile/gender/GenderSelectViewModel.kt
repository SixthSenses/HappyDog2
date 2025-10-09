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

// UI에서 사용할 성별 타입
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

    fun onSave(onSuccess: (GenderUi) -> Unit) = viewModelScope.launch {
        val selected = _ui.value.selected ?: run {
            _ui.value = _ui.value.copy(error = "성별을 선택해 주세요")
            return@launch
        }

        _ui.value = _ui.value.copy(isSaving = true, error = null)

        // TODO: 서버 연동 시 실제 API 호출로 대체
        runCatching { selected }
            .onSuccess { resolved ->
                _ui.value = _ui.value.copy(isSaving = false)
                onSuccess(resolved)
            }
            .onFailure {
                _ui.value = _ui.value.copy(isSaving = false, error = "요청 처리에 실패했어요")
            }
    }
}
