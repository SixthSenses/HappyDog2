package com.example.pet_project_frontend.presentation.mypage.settings.notification

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class NotificationUiState(
    val weeklyReport: Boolean = true,
    val like: Boolean = false,
    val comment: Boolean = false
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState = _uiState.asStateFlow()

    fun setWeeklyReport(v: Boolean) = _uiState.update { it.copy(weeklyReport = v) }
    fun setLike(v: Boolean) = _uiState.update { it.copy(like = v) }
    fun setComment(v: Boolean) = _uiState.update { it.copy(comment = v) }
}
