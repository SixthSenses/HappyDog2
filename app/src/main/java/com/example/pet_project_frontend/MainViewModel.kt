// app/src/main/java/com/example/pet_project_frontend/MainViewModel.kt

package com.example.pet_project_frontend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.repository.UserRepository
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // 토큰이 존재하는지 여부를 확인하는 StateFlow
    // userRepository.getAccessToken() Flow를 관찰하여 토큰이 있으면(null이나 blank가 아니면) true를 방출합니다.
    val isLoggedIn: StateFlow<Boolean> = userRepository.getAccessToken()
        .map { token ->
            !token.isNullOrBlank()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // 5초간 화면이 꺼져있으면 데이터 수집 중단
            initialValue = false // 앱 시작 시 초기값은 로그아웃 상태
        )

    init {
        // Restore selected_pet_id from server if missing locally once logged in
        viewModelScope.launch {
            var restored = false
            isLoggedIn.collect { loggedIn ->
                if (!restored && loggedIn) {
                    val local = tokenManager.getSelectedPetId()
                    if (local.isNullOrBlank()) {
                        userRepository.getSelectedPet()
                    }
                    restored = true
                }
            }
        }
    }
}