package com.example.pet_project_frontend.presentation.mypage.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    fun updateProfileImage(uri: String) {
        _uiState.update { it.copy(profileImageUrl = uri) }
    }

    init {
        // 임시 데이터
        _uiState.value = MyPageUiState(
            petName = "이름",
            breed = "견종",
            age = "나이",
            birthDate = "YYYY.MM.DD",
            gender = "성별"
        )
    }
}

data class MyPageUiState(
    val petName: String = "",
    val breed: String = "",
    val age: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val profileImageUrl: String? = null,
    val isLoading: Boolean = false
)