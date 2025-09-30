// app/src/main/java/com/example/pet_project_frontend/MainViewModel.kt

package com.example.pet_project_frontend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.repository.UserRepository
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val petRepository: PetRepository
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

    // 단일 펫 정책 기반: 서버의 내 펫 프로필 조회 가능 여부로 판정
    val hasPet: StateFlow<Boolean> = petRepository.hasPet()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
}