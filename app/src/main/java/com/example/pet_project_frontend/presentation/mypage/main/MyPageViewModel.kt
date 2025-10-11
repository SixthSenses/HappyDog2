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

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val userResult = userRepository.getUserInfo()
            val user = when (userResult) {
                is AppResult.Success -> userResult.data
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = userResult.message ?: "Failed to load user information."
                        )
                    }
                    return@launch
                }

                is AppResult.Exception -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = userResult.throwable.message ?: "An unexpected error occurred while loading user information."
                        )
                    }
                    return@launch
                }
            }

            when (val petResult = petRepository.getMyPetProfile()) {
                is AppResult.Success -> updateState(user, petResult.data)
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        profileImageUrl = user.profileImageUrl,
                        isLoading = false,
                        error = petResult.message ?: "Failed to load pet information."
                    )
                }

                is AppResult.Exception -> _uiState.update {
                    it.copy(
                        profileImageUrl = user.profileImageUrl,
                        isLoading = false,
                        error = petResult.throwable.message ?: "An unexpected error occurred while loading pet information."
                    )
                }
            }
        }
    }

    private fun updateState(user: User, pet: Pet) {
        val ageText = calculateAgeLabel(pet.birthDate)

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
                profileImageUrl = user.profileImageUrl,
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

    private fun calculateAgeLabel(birthDate: LocalDate): String {
        val today = LocalDate.now()
        if (birthDate.isAfter(today)) return ""
        val period = Period.between(birthDate, today)

        val years = period.years
        val months = period.months
        val days = period.days

        return when {
            years > 0 -> "${years}살"
            months > 0 -> "${months}개월"
            days > 0 -> "${days}일"
            else -> "신생아"
        }
    }

    private fun calculateAgeLabel(birth: String): String {
        if (birth.isBlank()) return ""
        val sanitized = birth.replace(".", "-").replace("/", "-")
        return try {
            val birthDate = LocalDate.parse(sanitized, birthFormatter)
            calculateAgeLabel(birthDate)
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
