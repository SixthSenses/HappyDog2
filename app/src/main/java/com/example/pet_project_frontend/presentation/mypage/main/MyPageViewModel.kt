package com.example.pet_project_frontend.presentation.mypage.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.core.common.AppResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Period
import com.example.pet_project_frontend.domain.repository.PetRepository
import com.example.pet_project_frontend.domain.repository.UserRepository
import com.example.pet_project_frontend.data.remote.upload.FileUploadManager
import com.example.pet_project_frontend.data.remote.upload.UploadType
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
    private val fileUploadManager: FileUploadManager
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
                val birthDateText = pet.birthDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                _uiState.update { 
                                    it.copy(
                                        petId = pet.id,
                                        petName = pet.name,
                                        breed = pet.breed,
                    age = ageText,
                    birthDate = birthDateText,
                    gender = genderText,
                                        profileImageUrl = pet.profileImageUrl,
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
    
    fun deleteProfileImage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            
            val petId = _uiState.value.petId
            if (petId.isNullOrBlank()) {
                _uiState.update { it.copy(isUploading = false, error = "펫 정보를 찾을 수 없습니다") }
                return@launch
            }
            
            android.util.Log.d("MyPageViewModel", "Deleting profile image for petId: $petId")
            
            // profile_image_url을 null로 설정하여 삭제
            // Gson은 기본적으로 null 필드를 생략하므로, 빈 문자열 사용
            val updateRequest = com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest(
                profileImageUrl = ""
            )
            
            when (val res = petRepository.updatePetProfile(petId, updateRequest)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(profileImageUrl = null, isUploading = false, error = null) }
                }
                is AppResult.Error -> {
                    val validation = res.validation
                    val errorMsg = when {
                        res.code == 403 -> "프로필 사진을 삭제할 권한이 없습니다"
                        res.code == 404 -> "반려동물 정보를 찾을 수 없습니다"
                        validation != null -> validation.generalMessage ?: "입력값이 올바르지 않습니다"
                        else -> res.message ?: "프로필 사진 삭제에 실패했습니다"
                    }
                    _uiState.update { it.copy(isUploading = false, error = errorMsg) }
                }
                is AppResult.Exception -> {
                    val errorMsg = when {
                        res.throwable.message?.contains("403") == true -> "프로필 사진을 삭제할 권한이 없습니다"
                        res.throwable.message?.contains("network") == true -> "네트워크 연결을 확인해주세요"
                        else -> "프로필 사진 삭제 중 오류가 발생했습니다"
                    }
                    _uiState.update { it.copy(isUploading = false, error = errorMsg) }
                }
            }
        }
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
                            val validation = res.validation
                            val errorMsg = res.message ?: validation?.generalMessage ?: "프로필 갱신 실패"
                            _uiState.update { it.copy(isUploading = false, error = errorMsg) }
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
    
    fun updateName(newName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val petId = _uiState.value.petId
            if (petId.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false, error = "펫 정보를 찾을 수 없습니다") }
                return@launch
            }
            
            val updateRequest = com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest(
                name = newName
            )
            
            when (val res = petRepository.updatePetProfile(petId, updateRequest)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(petName = newName, isLoading = false) }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = res.message ?: "이름 수정 실패") }
                }
                is AppResult.Exception -> {
                    _uiState.update { it.copy(isLoading = false, error = res.throwable.message ?: "이름 수정 오류") }
                }
            }
        }
    }
    
    fun updateBirthDate(birthDate: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val petId = _uiState.value.petId
            if (petId.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false, error = "펫 정보를 찾을 수 없습니다") }
                return@launch
            }
            
            android.util.Log.d("MyPageViewModel", "Updating birthdate: petId=$petId, birthdate=$birthDate")
            
            val updateRequest = com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest(
                birthdate = birthDate  // yyyy-MM-dd 형식 문자열
            )
            
            when (val res = petRepository.updatePetProfile(petId, updateRequest)) {
                is AppResult.Success -> {
                    // 나이 재계산
                    val pet = res.data
                    val ageYears = Period.between(pet.birthDate, LocalDate.now()).years
                    val ageText = when {
                        ageYears == 0 -> "1살 미만"
                        ageYears == 1 -> "1살"
                        else -> "${ageYears}살"
                    }
                    val birthDateText = pet.birthDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                    _uiState.update { it.copy(birthDate = birthDateText, age = ageText, isLoading = false) }
                }
                is AppResult.Error -> {
                    val validation = res.validation
                    val errorMsg = when {
                        res.code == 500 -> "생년월일 수정 중 서버 오류가 발생했습니다\n잠시 후 다시 시도해주세요"
                        validation != null -> validation.generalMessage ?: "날짜 형식이 올바르지 않습니다"
                        else -> res.message ?: "생년월일 수정에 실패했습니다"
                    }
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
                is AppResult.Exception -> {
                    val errorMsg = if (res.throwable.message?.contains("500") == true) {
                        "생년월일 수정 중 서버 오류가 발생했습니다\n잠시 후 다시 시도해주세요"
                    } else {
                        "생년월일 수정 중 오류가 발생했습니다"
                    }
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
            }
        }
    }
    
    fun updateGender(gender: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val petId = _uiState.value.petId
            if (petId.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false, error = "펫 정보를 찾을 수 없습니다") }
                return@launch
            }
            
            // "수컷", "암컷" → "MALE", "FEMALE" 변환
            val genderEnum = when (gender) {
                "수컷" -> "MALE"
                "암컷" -> "FEMALE"
                else -> return@launch
            }
            
            val updateRequest = com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest(
                gender = genderEnum
            )
            
            when (val res = petRepository.updatePetProfile(petId, updateRequest)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(gender = gender, isLoading = false) }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = res.message ?: "성별 수정 실패") }
                }
                is AppResult.Exception -> {
                    _uiState.update { it.copy(isLoading = false, error = res.throwable.message ?: "성별 수정 오류") }
                }
            }
        }
    }
    
    fun updateBreed(breed: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val petId = _uiState.value.petId
            if (petId.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false, error = "펫 정보를 찾을 수 없습니다") }
                return@launch
            }
            
            val updateRequest = com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest(
                breed = breed
            )
            
            when (val res = petRepository.updatePetProfile(petId, updateRequest)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(breed = breed, isLoading = false) }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = res.message ?: "견종 수정 실패") }
                }
                is AppResult.Exception -> {
                    _uiState.update { it.copy(isLoading = false, error = res.throwable.message ?: "견종 수정 오류") }
                }
            }
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
    val error: String? = null
)