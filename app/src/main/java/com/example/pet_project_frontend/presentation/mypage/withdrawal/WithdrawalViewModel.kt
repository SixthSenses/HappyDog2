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
                    val message = result.message ?: "탈퇴 요청이 실패했어요"
                    _ui.update { it.copy(isProcessing = false, errorMessage = message) }
                }
                is AppResult.Exception -> {
                    val message = result.throwable.message ?: "알 수 없는 오류가 발생했어요"
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
