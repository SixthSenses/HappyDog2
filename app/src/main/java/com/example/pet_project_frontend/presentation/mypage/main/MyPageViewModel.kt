package com.example.pet_project_frontend.presentation.mypage.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.upload.FileUploadManager
import com.example.pet_project_frontend.data.remote.upload.UploadType
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.domain.repository.PetRepository
import com.example.pet_project_frontend.domain.repository.UserRepository
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
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val userRepository: UserRepository,
    private val fileUploadManager: FileUploadManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    private val birthFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

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
                    is AppResult.Success -> {
                        // 단일 펫 정책: 서버에서 내 반려동물 프로필 조회
                        val petResult = petRepository.getMyPetProfile()
                        when (petResult) {
                            is AppResult.Success -> {
                                val pet = petResult.data
                                val ageText = calculateAgeLabel(pet.birthDate)
                                val genderText = when (pet.gender) {
                                    Gender.MALE -> "수컷"
                                    Gender.FEMALE -> "암컷"
                                    Gender.UNKNOWN -> "미상"
                                }
                                val birthDateText = pet.birthDate.format(birthFormatter)
                                _uiState.update {
                                    it.copy(
                                        petId = pet.id,
                                        petName = pet.name,
                                        breed = pet.breed,
                                        age = ageText,
                                        birthDate = birthDateText,
                                        gender = genderText,
                                        profileImageUrl = pet.profileImageUrl,
                                        isPetVerified = pet.isVerified,
                                        hasRegisteredNosePrint = !pet.nosePrintUrl.isNullOrBlank(),
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

    fun uploadAndApplyProfileImage(localFilePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }

            val petId = _uiState.value.petId
            if (petId.isNullOrBlank()) {
                _uiState.update { it.copy(isUploading = false, error = "펫 정보를 찾을 수 없습니다") }
                return@launch
            }

            when (val upload = fileUploadManager.uploadFile(java.io.File(localFilePath), UploadType.PET_PROFILE)) {
                is AppResult.Success -> {
                    val filePath = upload.data // backend file_path
                    val updateRequest = com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest(
                        profileImageUrl = filePath
                    )
                    when (val res = petRepository.updatePetProfile(petId, updateRequest)) {
                        is AppResult.Success -> {
                            _uiState.update { it.copy(profileImageUrl = res.data.profileImageUrl, isUploading = false) }
                        }

                        is AppResult.Error -> {
                            _uiState.update { it.copy(isUploading = false, error = res.message ?: res.validation?.generalMessage ?: "프로필 갱신 실패") }
                        }

                        is AppResult.Exception -> {
                            _uiState.update { it.copy(isUploading = false, error = res.throwable.message ?: "프로필 갱신 오류") }
                        }
                    }
                }

                is AppResult.Error -> {
                    _uiState.update { it.copy(isUploading = false, error = upload.message ?: "업로드 실패") }
                }

                is AppResult.Exception -> {
                    _uiState.update { it.copy(isUploading = false, error = upload.throwable.message ?: "업로드 오류") }
                }
            }
        }
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
}

data class MyPageUiState(
    val petId: String? = null,
    val petName: String = "",
    val breed: String = "",
    val age: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val profileImageUrl: String? = null,
    val uploadedImageUrls: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val isPetVerified: Boolean = false,
    val hasRegisteredNosePrint: Boolean = false,
    val error: String? = null
)
