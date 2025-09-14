package com.example.pet_project_frontend.presentation.mypage.profile.gender

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI 전용 성별 타입 (도메인 분리)
enum class GenderUi { MALE, FEMALE }

data class GenderUiState(
    val selected: GenderUi? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GenderSelectViewModel @Inject constructor(
    // 실제 저장 연동: 필요 시 주입
    // private val updateGender: UpdateGenderUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _ui = MutableStateFlow(GenderUiState())
    val uiState = _ui.asStateFlow()

    init {
        // 초기값 쿼리 예: ?initial=male|female
        val init = savedStateHandle.get<String>("initial")?.lowercase()
        val initial = when (init) {
            "male" -> GenderUi.MALE
            "female" -> GenderUi.FEMALE
            else -> null
        }
        _ui.value = _ui.value.copy(selected = initial)
    }

    fun onSelect(g: GenderUi) {
        _ui.value = _ui.value.copy(selected = g, error = null)
    }

    fun onSave(onSuccess: () -> Unit) = viewModelScope.launch {
        val selected = _ui.value.selected ?: run {
            _ui.value = _ui.value.copy(error = "성별을 선택해 주세요.")
            return@launch
        }

        _ui.value = _ui.value.copy(isSaving = true, error = null)

        // 실제 저장 연동이 아직이면 스텁 처리
        runCatching {
            // domain 모델이 있다면 이렇게 매핑
            // val domain = when (selected) {
            //     GenderUi.MALE -> Gender.MALE
            //     GenderUi.FEMALE -> Gender.FEMALE
            // }
            // updateGender(domain)
        }.onSuccess {
            onSuccess()
        }.onFailure {
            _ui.value = _ui.value.copy(isSaving = false, error = "저장에 실패했어요.")
        }
    }
}
