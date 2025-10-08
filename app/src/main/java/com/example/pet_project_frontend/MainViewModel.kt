// app/src/main/java/com/example/pet_project_frontend/MainViewModel.kt

package com.example.pet_project_frontend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.model.PetStatus
import com.example.pet_project_frontend.domain.repository.UserRepository
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
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
            started = SharingStarted.Eagerly, // 즉시 시작하여 로그인 상태 변화 감지
            initialValue = false // 앱 시작 시 초기값은 로그아웃 상태
        )

    /**
     * 반려동물 상태 (Loading/HasPet/NoPet)
     * 로그인 상태가 변경되면 자동으로 서버에서 펫 상태를 확인합니다.
     */
    val petStatus: StateFlow<PetStatus> = isLoggedIn
        .flatMapLatest { loggedIn ->
            if (loggedIn) {
                android.util.Log.d("MainViewModel", "User logged in, refreshing pet status from server")
                // 🔥 핵심: 로그인 상태일 때 자동으로 서버에서 펫 상태 확인
                viewModelScope.launch {
                    petRepository.refreshPetStatusManually()
                }
                petRepository.getPetStatus()
            } else {
                android.util.Log.d("MainViewModel", "User not logged in, setting status to NoPet")
                flowOf(PetStatus.NoPet)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // 앱 시작 시 즉시 확인
            initialValue = PetStatus.Loading
        )
    
    /**
     * 로그인 후 펫 상태를 서버에서 새로고침
     * LoginScreen에서 로그인 성공 후 호출
     * suspend 함수로 변경하여 완료를 기다릴 수 있도록 함
     */
    suspend fun refreshPetStatus() {
        android.util.Log.d("MainViewModel", "Manually refreshing pet status after login")
        petRepository.refreshPetStatusManually()
        android.util.Log.d("MainViewModel", "Pet status refresh completed")
    }
}