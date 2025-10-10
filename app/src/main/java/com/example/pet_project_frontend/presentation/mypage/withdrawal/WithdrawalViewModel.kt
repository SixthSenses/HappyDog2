// 변경의도: 탈퇴 요청 성공 시 세션 정리와 화면 전환이 매끄럽게 이뤄지도록 상태/메시지를 정돈한다.
package com.example.pet_project_frontend.presentation.mypage.withdrawal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.usecase.settings.WithdrawAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WithdrawalViewModel @Inject constructor(
    private val withdrawAccountUseCase: WithdrawAccountUseCase
) : ViewModel() {

    data class UiState(
        val showCompleted: Boolean = false,
        val isProcessing: Boolean = false,
        val errorMessage: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    fun onClickWithdraw() {
        if (_ui.value.isProcessing) return
        viewModelScope.launch {
            _ui.update { it.copy(isProcessing = true, errorMessage = null) }
            when (val result = withdrawAccountUseCase()) {
                is AppResult.Success -> {
                    _ui.update { it.copy(isProcessing = false, showCompleted = true) }
                }

                is AppResult.Error -> {
                    val message = result.message ?: "탈퇴 요청에 실패했어요. 다시 시도해 주세요."
                    _ui.update { it.copy(isProcessing = false, errorMessage = message) }
                }

                is AppResult.Exception -> {
                    val message = result.throwable.message ?: "예기치 못한 오류가 발생했어요. 잠시 후 다시 시도해 주세요."
                    _ui.update { it.copy(isProcessing = false, errorMessage = message) }
                }
            }
        }
    }

    fun onDismissCompleted() {
        _ui.update { it.copy(showCompleted = false) }
    }

    fun clearError() {
        _ui.update { it.copy(errorMessage = null) }
    }
}
