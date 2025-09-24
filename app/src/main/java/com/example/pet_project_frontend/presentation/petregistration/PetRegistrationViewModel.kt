package com.example.pet_project_frontend.presentation.petregistration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.data.remote.dto.response.BreedResponse
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.ValidationError
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.presentation.model.PetUiState
import com.example.pet_project_frontend.domain.usecase.breed.SearchBreedsUseCase
import com.example.pet_project_frontend.domain.usecase.pet.RegisterPetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import java.time.Period

/**
 * 반려동물 등록 화면의 상태와 상호작용을 관리하는 ViewModel입니다.
 *
 * 주요 의존성으로는 다음이 포함됩니다.
 * - [RegisterPetUseCase]: 서버에 반려동물 등록을 수행합니다.
 * - [SearchBreedsUseCase]: 품종 검색 쿼리를 기반으로 품종 목록을 조회합니다.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
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
    
    // Weight is not part of PetRegistrationSchema; removed from registration form
    
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
    
    /**
     * 초기화 블록으로, 품종 검색어의 변화를 감지하여 자동으로 검색을 수행합니다.
     *
     * @return Unit
     */
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
    
    /**
     * 반려동물 이름을 업데이트합니다. 입력 시 기존 오류 메시지를 초기화합니다.
     *
     * @param name 반려동물 이름
     * @return Unit
     */
    fun updatePetName(name: String) {
        _petName.value = name
        clearError()
    }
    
    /**
     * 선택된 성별을 업데이트합니다.
     *
     * @param gender 선택할 성별
     * @return Unit
     */
    fun updateGender(gender: Gender) {
        _selectedGender.value = gender
    }
    
    /**
     * 선택된 품종을 설정하고 품종 선택 다이얼로그를 닫습니다.
     *
     * @param breed 선택된 품종 응답
     * @return Unit
     */
    fun selectBreed(breed: BreedResponse) {
        _selectedBreed.value = breed
        _showBreedDialog.value = false
        clearError()
    }
    
    /**
     * 생년월일을 업데이트합니다. 입력 시 기존 오류 메시지를 초기화합니다.
     *
     * @param date 선택한 생년월일
     * @return Unit
     */
    fun updateBirthDate(date: LocalDate) {
        _birthDate.value = date
        clearError()
    }
    
    /**
     * 체중 입력값을 업데이트합니다. 숫자와 소수점만 허용하며 유효할 때만 반영합니다.
     *
     * @param weight 체중 문자열(예: "4.2")
     * @return Unit
     */
    // No-op: weight removed
    
    /**
     * 털색 정보를 업데이트합니다.
     *
     * @param color 털색(빈 문자열 허용)
     * @return Unit
     */
    fun updateFurColor(color: String) {
        _furColor.value = color
    }
    
    /**
     * 건강 이슈를 추가합니다. 공백이 아니고 중복이 아닐 때만 추가됩니다.
     *
     * @param concern 추가할 건강 이슈 문구
     * @return Unit
     */
    fun addHealthConcern(concern: String) {
        if (concern.isNotBlank() && !_healthConcerns.value.contains(concern)) {
            _healthConcerns.value = _healthConcerns.value + concern
        }
    }
    
    /**
     * 건강 이슈를 제거합니다.
     *
     * @param concern 제거할 건강 이슈 문구
     * @return Unit
     */
    fun removeHealthConcern(concern: String) {
        _healthConcerns.value = _healthConcerns.value - concern
    }
    
    /**
     * 품종 검색어를 업데이트합니다. 검색어 변경은 자동 검색을 트리거합니다.
     *
     * @param query 품종 검색어
     * @return Unit
     */
    fun updateBreedSearchQuery(query: String) {
        _breedSearchQuery.value = query
    }
    
    /**
     * 품종 선택 다이얼로그를 표시합니다. 전체 목록을 보이도록 검색어를 초기화합니다.
     *
     * @return Unit
     */
    fun showBreedDialog() {
        _showBreedDialog.value = true
        // 다이얼로그 열 때 전체 목록 로드
        _breedSearchQuery.value = ""
    }
    
    /**
     * 품종 선택 다이얼로그를 숨깁니다.
     *
     * @return Unit
     */
    fun hideBreedDialog() {
        _showBreedDialog.value = false
    }
    
    /**
     * 입력값을 검증한 뒤, 반려동물 등록 유스케이스를 호출하여 서버에 등록합니다.
     * 처리 결과에 따라 UI 상태([uiState])를 갱신합니다.
     *
     * @return Unit
     */
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
                // weight removed from API schema
                furColor = _furColor.value.takeIf { it.isNotBlank() },
                healthConcerns = _healthConcerns.value
            )
            
            when (result) {
                is AppResult.Success -> {
                    val petUi = result.data.toUiState()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        registeredPet = petUi
                    )
                }
                is AppResult.Error -> {
                    // 서버 검증 오류 매핑: 필드명 기준으로 로컬 필드에 에러 전달
                    val fieldErrors = result.validation?.fields.orEmpty()
                    val fieldErrorMessage = when {
                        fieldErrors.containsKey("name") -> fieldErrors["name"]
                        fieldErrors.containsKey("breed") -> fieldErrors["breed"]
                        fieldErrors.containsKey("birth_date") -> fieldErrors["birth_date"]
                        fieldErrors.containsKey("weight") -> fieldErrors["weight"]
                        else -> null
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = fieldErrorMessage ?: result.message ?: result.validation?.generalMessage
                    )
                }
                is AppResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "네트워크 오류가 발생했습니다. 다시 시도해주세요."
                    )
                }
            }
        }
    }

    /**
     * 도메인 모델 Pet을 화면 표시용 [PetUiState]로 변환합니다.
     *
     * @receiver 변환 대상 도메인 모델 Pet
     * @return 변환된 [PetUiState]
     */
    private fun com.example.pet_project_frontend.domain.model.Pet.toUiState(): PetUiState {
        val years = Period.between(birthDate, LocalDate.now()).years
        val ageText = when {
            years <= 0 -> "1살 미만"
            years == 1 -> "1살"
            else -> "${years}살"
        }
        val genderText = when (gender) {
            com.example.pet_project_frontend.domain.model.Gender.MALE -> "수컷"
            com.example.pet_project_frontend.domain.model.Gender.FEMALE -> "암컷"
            com.example.pet_project_frontend.domain.model.Gender.UNKNOWN -> "미상"
        }
        return PetUiState(
            id = id,
            name = name,
            breed = breed,
            ageText = ageText,
            birthDateText = birthDate.toString(),
            genderText = genderText,
            weightText = null,
            furColorText = null,
            profileImageUrl = null
        )
    }
    
    /**
     * 현재 입력값을 검증하고 오류 메시지를 반환합니다.
     *
     * @return 유효하지 않은 경우 오류 메시지, 유효한 경우 null
     */
    private fun validateInput(): String? {
        return when {
            _petName.value.isBlank() -> "반려동물 이름을 입력해주세요"
            _petName.value.length > 20 -> "이름은 20자 이내로 입력해주세요"
            _selectedBreed.value == null -> "품종을 선택해주세요"
            _birthDate.value == null -> "생년월일을 선택해주세요"
            _birthDate.value!!.isAfter(LocalDate.now()) -> "올바른 생년월일을 선택해주세요"
            else -> null
        }
    }
    
    /**
     * UI 상태의 오류 메시지를 초기화합니다.
     *
     * @return Unit
     */
    private fun clearError() {
        if (_uiState.value.error != null) {
            _uiState.value = _uiState.value.copy(error = null)
        }
    }
}

// UI State
/**
 * 반려동물 등록 화면의 UI 상태를 나타내는 데이터 클래스입니다.
 *
 * @property isLoading 등록 요청 진행 여부
 * @property isSuccess 등록 성공 여부
 * @property error 사용자에게 표시할 오류 메시지(없으면 null)
 * @property registeredPet 등록 완료된 반려동물의 UI 상태(없으면 null)
 */
data class PetRegistrationUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val registeredPet: PetUiState? = null
)