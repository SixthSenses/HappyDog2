package com.example.pet_project_frontend.presentation.petregistration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.data.remote.dto.response.BreedResponse
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.usecase.breed.SearchBreedsUseCase
import com.example.pet_project_frontend.domain.usecase.pet.RegisterPetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PetRegistrationViewModel @Inject constructor(
    private val registerPetUseCase: RegisterPetUseCase,
    private val searchBreedsUseCase: SearchBreedsUseCase
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(PetRegistrationUiState())
    val uiState: StateFlow<PetRegistrationUiState> = _uiState.asStateFlow()
    
    // Form fields
    private val _petName = MutableStateFlow("")
    val petName: StateFlow<String> = _petName.asStateFlow()
    
    private val _selectedGender = MutableStateFlow(Gender.MALE)
    val selectedGender: StateFlow<Gender> = _selectedGender.asStateFlow()
    
    private val _selectedBreed = MutableStateFlow<BreedResponse?>(null)
    val selectedBreed: StateFlow<BreedResponse?> = _selectedBreed.asStateFlow()
    
    private val _birthDate = MutableStateFlow<LocalDate?>(null)
    val birthDate: StateFlow<LocalDate?> = _birthDate.asStateFlow()
    
    private val _weight = MutableStateFlow("")
    val weight: StateFlow<String> = _weight.asStateFlow()
    
    private val _furColor = MutableStateFlow("")
    val furColor: StateFlow<String> = _furColor.asStateFlow()
    
    private val _healthConcerns = MutableStateFlow<List<String>>(emptyList())
    val healthConcerns: StateFlow<List<String>> = _healthConcerns.asStateFlow()
    
    // Breed search
    private val _breedSearchQuery = MutableStateFlow("")
    val breedSearchQuery: StateFlow<String> = _breedSearchQuery.asStateFlow()
    
    private val _breedSearchResults = MutableStateFlow<List<BreedResponse>>(emptyList())
    val breedSearchResults: StateFlow<List<BreedResponse>> = _breedSearchResults.asStateFlow()
    
    private val _showBreedDialog = MutableStateFlow(false)
    val showBreedDialog: StateFlow<Boolean> = _showBreedDialog.asStateFlow()
    
    init {
        // 품종 검색 자동 실행
        viewModelScope.launch {
            _breedSearchQuery
                .debounce(300) // 300ms 딜레이
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    searchBreedsUseCase(query)
                }
                .collect { breeds ->
                    _breedSearchResults.value = breeds
                }
        }
    }
    
    fun updatePetName(name: String) {
        _petName.value = name
        clearError()
    }
    
    fun updateGender(gender: Gender) {
        _selectedGender.value = gender
    }
    
    fun selectBreed(breed: BreedResponse) {
        _selectedBreed.value = breed
        _showBreedDialog.value = false
        clearError()
    }
    
    fun updateBirthDate(date: LocalDate) {
        _birthDate.value = date
        clearError()
    }
    
    fun updateWeight(weight: String) {
        // 숫자와 소수점만 허용
        if (weight.isEmpty() || weight.matches(Regex("^\\d*\\.?\\d*$"))) {
            _weight.value = weight
            clearError()
        }
    }
    
    fun updateFurColor(color: String) {
        _furColor.value = color
    }
    
    fun addHealthConcern(concern: String) {
        if (concern.isNotBlank() && !_healthConcerns.value.contains(concern)) {
            _healthConcerns.value = _healthConcerns.value + concern
        }
    }
    
    fun removeHealthConcern(concern: String) {
        _healthConcerns.value = _healthConcerns.value - concern
    }
    
    fun updateBreedSearchQuery(query: String) {
        _breedSearchQuery.value = query
    }
    
    fun showBreedDialog() {
        _showBreedDialog.value = true
        // 다이얼로그 열 때 전체 목록 로드
        _breedSearchQuery.value = ""
    }
    
    fun hideBreedDialog() {
        _showBreedDialog.value = false
    }
    
    fun registerPet() {
        viewModelScope.launch {
            // 유효성 검사
            val validationError = validateInput()
            if (validationError != null) {
                _uiState.value = _uiState.value.copy(
                    error = validationError,
                    isLoading = false
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = registerPetUseCase(
                name = _petName.value,
                gender = _selectedGender.value,
                breed = _selectedBreed.value!!.breedName,
                birthDate = _birthDate.value!!,
                currentWeight = _weight.value.toFloat(),
                furColor = _furColor.value.takeIf { it.isNotBlank() },
                healthConcerns = _healthConcerns.value
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        registeredPet = result.data
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is NetworkResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "네트워크 오류가 발생했습니다. 다시 시도해주세요."
                    )
                }
            }
        }
    }
    
    private fun validateInput(): String? {
        return when {
            _petName.value.isBlank() -> "반려동물 이름을 입력해주세요"
            _petName.value.length > 20 -> "이름은 20자 이내로 입력해주세요"
            _selectedBreed.value == null -> "품종을 선택해주세요"
            _birthDate.value == null -> "생년월일을 선택해주세요"
            _birthDate.value!!.isAfter(LocalDate.now()) -> "올바른 생년월일을 선택해주세요"
            _weight.value.isBlank() -> "체중을 입력해주세요"
            _weight.value.toFloatOrNull() == null -> "올바른 체중을 입력해주세요"
            _weight.value.toFloat() < 0.1 || _weight.value.toFloat() > 200 -> "체중은 0.1kg ~ 200kg 사이로 입력해주세요"
            else -> null
        }
    }
    
    private fun clearError() {
        if (_uiState.value.error != null) {
            _uiState.value = _uiState.value.copy(error = null)
        }
    }
}

// UI State
data class PetRegistrationUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val registeredPet: Pet? = null
)