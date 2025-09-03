package com.example.pet_project_frontend.presentation.mypage.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.model.Gender
import java.time.LocalDate
import java.time.Period
import com.example.pet_project_frontend.domain.repository.PetRepository
import com.example.pet_project_frontend.data.local.preferences.TokenManager
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
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
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
                        // 저장된 pet_id 조회
                        val petId = tokenManager.getSelectedPetId()
                        if (petId.isNullOrBlank()) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "등록된 반려동물이 없습니다. 먼저 반려동물을 등록해주세요."
                                )
                            }
                            return@launch
                        }
                        // 반려동물 정보 로드
                        val petResult = petRepository.getPetProfile(petId)
                        when (petResult) {
                            is com.example.pet_project_frontend.data.remote.result.NetworkResult.Success -> {
                                val pet = petResult.data
                                val ageYears = Period.between(pet.birthDate, LocalDate.now()).years
                val ageText = when {
                                    ageYears == 0 -> "1살 미만"
                                    ageYears == 1 -> "1살"
                                    else -> "${ageYears}살"
                                }
                val genderText = when (pet.gender) {
                                    Gender.MALE -> "수컷"
                                    Gender.FEMALE -> "암컷"
                                    Gender.UNKNOWN -> "미상"
                                }
                                _uiState.update { 
                                    it.copy(
                                        petName = pet.name,
                                        breed = pet.breed,
                    age = ageText,
                                        birthDate = pet.birthDate.toString(),
                    gender = genderText,
                                        profileImageUrl = user.profileImageUrl,
                                        isLoading = false,
                                        error = null
                                    )
                                }
                            }
                            is com.example.pet_project_frontend.data.remote.result.NetworkResult.Error -> {
                                // 권한 문제나 유효하지 않은 petId면 로컬 pet_id를 제거하고 안내
                                if (petResult.code == 403 || petResult.code == 404) {
                                    tokenManager.clearSelectedPetId()
                                }
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