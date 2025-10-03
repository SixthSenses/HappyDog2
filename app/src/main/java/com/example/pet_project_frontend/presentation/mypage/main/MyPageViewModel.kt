package com.example.pet_project_frontend.presentation.mypage.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.repository.PetRepository
import com.example.pet_project_frontend.domain.repository.UserRepository
import com.example.pet_project_frontend.data.remote.upload.FileUploadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val userRepository: UserRepository,
    private val fileUploadManager: FileUploadManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    // 생성자 초기화: 사용자 및 펫 정보를 로드하여 uiState를 업데이트함
    init {
        loadUserData()
        // 임시 더미 데이터 (네트워크 호출 전 빠른 UI 출력을 위한 초기값)
        _uiState.value = MyPageUiState(
            petName = "해피",
            breed = "비글",
            age = "2살",
            birthDate = "2023.08.01",
            gender = "수컷"
        )
    }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 사용자 정보 로드
                val userResult = userRepository.getUserInfo()
                when (userResult) {
                    is AppResult.Success -> {
                        val user = userResult.data
                        // 단일 펫 정책: 서버에서 내 반려동물 프로필 조회
                        val petResult = petRepository.getMyPetProfile()
                        when (petResult) {
                            is AppResult.Success -> {
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
                                val birthDateText = pet.birthDate.format(
                                    DateTimeFormatter.ofPattern("yyyy.MM.dd")
                                )
                                _uiState.update {
                                    it.copy(
                                        petName = pet.name,
                                        breed = pet.breed,
                                        age = ageText,
                                        birthDate = birthDateText,
                                        gender = genderText,
                                        profileImageUrl = user.profileImageUrl,
                                        isLoading = false,
                                        error = null
                                    )
                                }
                            }
                            is AppResult.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = petResult.message ?: "반려동물 정보를 불러오는데 실패했습니다."
                                    )
                                }
                            }
                            is AppResult.Exception -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "반려동물 정보를 불러오는데 실패했습니다: ${petResult.throwable.message}"
                                    )
                                }
                            }
                        }
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "사용자 정보를 불러오는데 실패했습니다: ${userResult.message}"
                            )
                        }
                    }
                    is AppResult.Exception -> {
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
    
    // 에러 메시지 제거 (화면 노출 후, 재시도 가능하게 하기 위함)
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // 프로필 이미지 업데이트 (파일 업로드 후 URL 업데이트)
    fun updateProfileImage(uri: String) {
        _uiState.update { it.copy(profileImageUrl = uri) }
    }

    fun updatePetName(newName: String) {
        val normalized = newName.trim()
        _uiState.update { it.copy(petName = normalized) }
    }

    fun updateBirthDate(newBirth: String) {
        val normalized = newBirth.trim()
        val ageLabel = calculateAgeLabel(normalized)
        _uiState.update { it.copy(birthDate = normalized, age = ageLabel) }
    }

    fun updateGender(newGender: String) {
        _uiState.update { it.copy(gender = newGender.trim()) }
    }

    fun updateBreed(newBreed: String) {
        _uiState.update { it.copy(breed = newBreed.trim()) }
    }

    private fun calculateAgeLabel(birth: String): String {
        if (birth.isBlank()) return ""
        val sanitized = birth.replace(".", "-").replace("/", "-")
        return try {
            val birthDate = LocalDate.parse(sanitized, birthFormatter)
            val today = LocalDate.now()
            if (birthDate.isAfter(today)) return ""
            val period = Period.between(birthDate, today)
            when {
                period.years > 0 -> "${period.years}살"
                period.months > 0 -> "${period.months}개월"
                period.days > 0 -> "${period.days}일"
                else -> "0일"
            }
        } catch (error: DateTimeParseException) {
            ""
        }
    }

    private val birthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d", Locale.KOREA)
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