package com.example.pet_project_frontend.presentation.mypage.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.repository.PetRepository
import com.example.pet_project_frontend.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }
    
    fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 사용자 정보 로드
                val userResult = userRepository.getUserInfo()
                when (userResult) {
                    is com.example.pet_project_frontend.data.remote.result.NetworkResult.Success -> {
                        val user = userResult.data
                        // 반려동물 정보 로드 (첫 번째 반려동물이라고 가정)
                        val petResult = petRepository.getPetProfile(user.id)
                        when (petResult) {
                            is com.example.pet_project_frontend.data.remote.result.NetworkResult.Success -> {
                                val pet = petResult.data
                                _uiState.update { 
                                    it.copy(
                                        petName = pet.name,
                                        breed = pet.breed,
                                        age = pet.ageDisplay,
                                        birthDate = pet.birthDate.toString(),
                                        gender = pet.genderDisplay,
                                        profileImageUrl = user.profileImageUrl,
                                        isLoading = false,
                                        error = null
                                    )
                                }
                            }
                            is com.example.pet_project_frontend.data.remote.result.NetworkResult.Error -> {
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        error = "반려동물 정보를 불러오는데 실패했습니다: ${petResult.message}"
                                    )
                                }
                            }
                            is com.example.pet_project_frontend.data.remote.result.NetworkResult.Exception -> {
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        error = "반려동물 정보를 불러오는데 실패했습니다: ${petResult.throwable.message}"
                                    )
                                }
                            }
                        }
                    }
                    is com.example.pet_project_frontend.data.remote.result.NetworkResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "사용자 정보를 불러오는데 실패했습니다: ${userResult.message}"
                            )
                        }
                    }
                    is com.example.pet_project_frontend.data.remote.result.NetworkResult.Exception -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "사용자 정보를 불러오는데 실패했습니다: ${userResult.throwable.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "데이터를 불러오는데 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class MyPageUiState(
    val petName: String = "",
    val breed: String = "",
    val age: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val profileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)