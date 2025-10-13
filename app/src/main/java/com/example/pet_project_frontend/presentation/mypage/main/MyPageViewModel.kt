package com.example.pet_project_frontend.presentation.mypage.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.model.User
import com.example.pet_project_frontend.domain.repository.PetRepository
import com.example.pet_project_frontend.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    /**
     * 사용자와 펫 정보 로드
     * OpenAPI spec에 따라:
     * 1. GET /api/users/me - 사용자 기본 정보
     * 2. GET /api/pets/profile - 내 펫 프로필 (profile_image_url 포함)
     */
    fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 사용자 정보는 필요 없을 수 있음 (email 등은 MyPage에서 표시 안함)
            // Pet 정보만 로드
            when (val petResult = petRepository.getMyPetProfile()) {
                is AppResult.Success -> {
                    val pet = petResult.data
                    updateStateFromPet(pet)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = petResult.message ?: "Failed to load pet information."
                    )
                }

                is AppResult.Exception -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = petResult.throwable.message ?: "An unexpected error occurred while loading pet information."
                    )
                }
            }
        }
    }
    
    /**
     * Pet 정보를 기반으로 UI 상태 업데이트
     */
    private fun updateStateFromPet(pet: Pet) {
        val ageYears = Period.between(pet.birthDate, LocalDate.now()).years
        val ageText = when {
            ageYears <= 0 -> "Less than 1 year"
            ageYears == 1 -> "1 year"
            else -> "$ageYears years"
        }

        val genderText = when (pet.gender) {
            Gender.MALE -> "수컷"
            Gender.FEMALE -> "암컷"
            Gender.UNKNOWN -> "미상"
        }

        val birthDateText = pet.birthDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

        _uiState.update {
            it.copy(
                petId = pet.id,
                petName = pet.name,
                breed = pet.breed,
                age = ageText,
                birthDate = birthDateText,
                gender = genderText,
                profileImageUrl = pet.profileImageUrl, // Pet에서 가져옴
                isLoading = false,
                error = null
            )
        }
    }



    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun updateProfileImage(uri: String) {
        _uiState.update { it.copy(profileImageUrl = uri) }
    }

    fun updatePetName(newName: String) {
        _uiState.update { it.copy(petName = newName.trim()) }
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
                period.years > 0 -> "${period.years} years"
                period.months > 0 -> "${period.months} months"
                period.days > 0 -> "${period.days} days"
                else -> "0 days"
            }
        } catch (_: DateTimeParseException) {
            ""
        }
    }

    private val birthFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-M-d", Locale.KOREA)
}

data class MyPageUiState(
    val petId: String? = null,
    val petName: String = "",
    val breed: String = "",
    val age: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val profileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
