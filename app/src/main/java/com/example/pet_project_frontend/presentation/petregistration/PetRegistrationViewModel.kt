package com.example.pet_project_frontend.presentation.petregistration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.ValidationError
import com.example.pet_project_frontend.domain.model.Breed
import com.example.pet_project_frontend.domain.model.Gender
import com.example.pet_project_frontend.presentation.model.PetUiState
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.domain.repository.BreedRepository
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
    private val breedRepository: BreedRepository,
    private val searchBreedsUseCase: SearchBreedsUseCase
) : ViewModel() {

    // UI State (유지)
    private val _uiState = MutableStateFlow(PetRegistrationUiState())
    val uiState: StateFlow<PetRegistrationUiState> = _uiState.asStateFlow()

    // -- Form fields (mutableStateOf 로 변경) --
    var petName by mutableStateOf("")

    var selectedGender by mutableStateOf<Gender?>(null)

    var selectedBreed by mutableStateOf<Breed?>(null)

    var birthDate by mutableStateOf<LocalDate?>(null)

    var weight by mutableStateOf("")

    var furColor by mutableStateOf("")

    var healthConcerns by mutableStateOf<List<String>>(emptyList())

    // Breed search
    var breedSearchQuery by mutableStateOf("")
    var breedSearchResults by mutableStateOf<List<Breed>>(emptyList())
    var showBreedDialog by mutableStateOf(false)

    /**
     * 초기화 블록으로, 품종 검색어의 변화를 감지하여 자동으로 검색을 수행합니다.
     *
     * @return Unit
     */

    fun updatePetName(name: String) {
        petName = name
        clearError()
    }

    fun updateGender(gender: String) {
        val gender = if (gender == "수컷") Gender.MALE else if (gender == "암컷") Gender.FEMALE else Gender.UNKNOWN
        selectedGender = gender
        clearError()
    }

    suspend fun selectBreed(breedName: String) {
        when (val result = breedRepository.searchBreeds(breedName)) {
            is AppResult.Success -> {
                if (result.data.isNotEmpty()) {
                    selectedBreed = result.data[0]
                    clearError()
                }
            }
            is AppResult.Error -> {
                // 에러 처리 필요 시 추가
            }
            is AppResult.Exception -> {
                // 예외 처리 필요 시 추가
            }
        }
    }


    fun updateBirthDate(date: LocalDate?) {
        birthDate = date
        clearError()
    }

    /**
     * 털색 정보를 업데이트합니다.
     *
     * @param color 털색(빈 문자열 허용)
     * @return Unit
     */
    fun updateFurColor(color: String) {
        furColor = color
        clearError()
    }

    /**
     * 건강 이슈를 추가합니다. 공백이 아니고 중복이 아닐 때만 추가됩니다.
     *
     * @param concern 추가할 건강 이슈 문구
     * @return Unit
     */
    fun addHealthConcern(concern: String) {
        if (concern.isNotBlank() && !healthConcerns.contains(concern)) {
            healthConcerns = healthConcerns + concern
        }
    }

    /**
     * 건강 이슈를 제거합니다.
     *
     * @param concern 제거할 건강 이슈 문구
     * @return Unit
     */
    fun removeHealthConcern(concern: String) {
        healthConcerns = healthConcerns - concern
    }

    fun updateHealthConcerns(newConcerns: Set<String>) {
        healthConcerns = newConcerns.toList()
    }

    /**
     * 품종 검색어를 업데이트합니다. 검색어 변경은 자동 검색을 트리거합니다.
     *
     * @param query 품종 검색어
     * @return Unit
     */
    fun updateBreedSearchQuery(query: String) {
        breedSearchQuery = query
    }

    /**
     * 품종 선택 다이얼로그를 표시합니다. 전체 목록을 보이도록 검색어를 초기화합니다.
     *
     * @return Unit
     */
    fun showBreedDialog() {
        showBreedDialog = true
        // 다이얼로그 열 때 전체 목록 로드
        breedSearchQuery = ""
    }

    /**
     * 품종 선택 다이얼로그를 숨깁니다.
     *
     * @return Unit
     */
    fun hideBreedDialog() {
        showBreedDialog = false
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
                name = petName,
                gender = selectedGender!!,
                breed = selectedBreed!!.breedName,
                birthDate = birthDate!!,
                furColor = furColor.takeIf { it.isNotBlank() },
                healthConcerns = healthConcerns
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
            Gender.MALE -> "수컷"
            Gender.FEMALE -> "암컷"
            Gender.UNKNOWN -> "미상"
        }
        return PetUiState(
            id = id,
            name = name,
            breed = breed,
            ageText = ageText,
            birthDateText = birthDate.toString(),
            genderText = genderText,
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
            petName.isBlank() -> "이름을 입력해주세요"
            petName.length > 20 -> "이름은 20자 이내로 입력해주세요"
            selectedGender == null -> "성별을 선택해주세요"
            selectedBreed == null -> "견종을 선택해주세요"
            birthDate == null -> "생년월일을 입력해주세요"
            birthDate!!.isAfter(LocalDate.now()) -> "올바른 생년월일을 입력해주세요"
            furColor.isBlank() -> "털 색상을 선택해주세요"
            healthConcerns.size == 0 -> "건강 관심사를 선택해주세요"
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