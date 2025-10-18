package com.example.pet_project_frontend.presentation.petcare.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.example.pet_project_frontend.domain.repository.PetRepository
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.CareRecordUpdateRequest

@HiltViewModel
class PetCareHomeViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val petCareRepository: PetCareRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // 반려동물 정보 가져오기
                when (val petResult = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        val pet = petResult.data
                        _uiState.value = _uiState.value.copy(
                            petName = pet.name,
                            petImageUrl = pet.nosePrintUrl
                        )
                        
                        // 반려동물 ID를 사용해서 케어 설정과 오늘 기록 로딩
                        val petId = pet.id
                        loadCareSettings(petId)
                        loadCareDataForDate(_uiState.value.selectedDate, petId)
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "반려동물 정보를 불러올 수 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "데이터를 불러오는 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        
        // 새로운 날짜의 케어 데이터 로딩
        viewModelScope.launch {
            try {
                when (val petResult = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        val petId = petResult.data.id
                        loadCareDataForDate(date, petId)
                    }
                    else -> {
                        // 기본 petId 사용 - 실제로는 등록된 펫이 있어야 함
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "반려동물 정보를 찾을 수 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "선택된 날짜의 데이터를 불러올 수 없습니다."
                )
            }
        }
    }

    fun changeMonth(monthOffset: Int) {
        val currentDate = _uiState.value.selectedDate
        val newDate = currentDate.plusMonths(monthOffset.toLong())
        _uiState.value = _uiState.value.copy(selectedDate = newDate)
    }

    /**
     * 특정 날짜의 케어 데이터 로딩 (summary api 호출로 설정값과 기록 한 번에 조회])
     */
    /**
     * 특정 날짜의 케어 데이터 로딩 (summary api 호출로 설정값과 기록 한 번에 조회)
     */
    private fun loadCareDataForDate(date: LocalDate, petId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true) // 데이터 로딩 시작
            try {
                val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                android.util.Log.d("PetCareViewModel", "Loading care data for date: $dateString, petId: $petId")

                // 변경된 getDailySummary API 호출
                when (val result = petCareRepository.getDailySummary(petId, dateString)) {
                    is AppResult.Success -> {
                        val summaryData = result.data
                        // 1. meta 객체에서 직접 데이터 추출 (실제 기록 값)
                        val meta = summaryData.meta

                        // --- ▼▼▼ 핵심 수정 부분 ▼▼▼ ---
                        val feedCount: Int = meta.mealCount ?: 0// meal_count -> mealCount 로 수정
                        val activityMinutes: Int = meta.activityMinutes ?: 0 // activity_minutes -> activityMinutes 로 수정
                        // --- ▲▲▲ 수정 완료 ▲▲▲ ---

                        val currentWeight = meta.weight?.toFloat()
                        val latestPoopRecord = meta.stool
                        val latestVomitRecord = meta.vomit

                        // 2. goal_progress 객체에서 목표 관련 데이터 추출
                        val goalProgress = summaryData.goalProgress

                        // --- ▼▼▼ 추가 수정 권장 (목표값도 camelCase로) ▼▼▼ ---
                        val targetFeed = goalProgress?.achievements?.meal?.goal
                        val targetActivity = goalProgress?.achievements?.activity?.goal
                        val targetWeightValue = goalProgress?.achievements?.weight?.goal
                        // --- ▲▲▲ 수정 완료 ▲▲▲ ---
                        // 3. 몸무게 텍스트 포맷 (예: "45.0kg")
                        val weightText = currentWeight?.let {
                            String.format("%.1fkg", it)
                        }

                        _uiState.value = _uiState.value.copy(
                            // meta에서 가져온 실제 기록 값 업데이트
                            currentFeedCount = feedCount,
                            currentActivityMinutes = activityMinutes,
                            currentWeight = currentWeight,
                            weightText = weightText,
                            todayLatestPoopRecord = latestPoopRecord,
                            todayLatestVomitRecord = latestVomitRecord,

                            // goal_progress에서 가져온 목표 값 업데이트
                            targetFeedCount = targetFeed,
                            targetDailyActivityMinutes = targetActivity,
                            targetWeight = targetWeightValue,

                            isLoading = false,
                            errorMessage = null // 성공 시 에러 메시지 초기화
                        )
                        android.util.Log.d("PetCareViewModel", "UI State updated - Activity: $activityMinutes, Feed: $feedCount")
                        // --- ▲▲▲ 수정 완료 ▲▲▲ ---
                    }
                    is AppResult.Error -> {
                        // API 호출은 성공했으나, 서버에서 에러를 반환한 경우 (예: 기록이 아예 없는 날)
                        _uiState.value = _uiState.value.copy(
                            currentFeedCount = 0,
                            currentActivityMinutes = 0,
                            currentWeight = null,
                            weightText = null,
                            todayLatestPoopRecord = null,
                            todayLatestVomitRecord = null,
                            // 목표는 유지될 수 있으므로 초기화하지 않음
                            isLoading = false,
                            errorMessage = null
                        )
                        android.util.Log.w("PetCareViewModel", "Error from getDailySummary: ${result.message}")
                    }
                    is AppResult.Exception -> {
                        // 네트워크 오류 등 API 호출 자체를 실패한 경우
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "케어 데이터를 불러오는 중 오류가 발생했습니다: ${result.throwable.message}"
                        )
                        android.util.Log.e("PetCareViewModel", "Exception from getDailySummary", result.throwable)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "케어 데이터를 불러오는 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }

    /**
     * 케어 설정 로딩
     */
    private fun loadCareSettings(petId: String) {
        viewModelScope.launch {
            try {
                // petId 유효성 검사 추가
                if (petId.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "반려동물 ID가 유효하지 않습니다."
                    )
                    return@launch
                }
                
                // 실제 API 호출로 케어 설정 가져오기
                when (val result = petCareRepository.getPetCareSettings(petId)) {
                    is AppResult.Success -> {
                        val settings = result.data
                        _uiState.value = _uiState.value.copy(
                            targetFeedCount = settings.targetDailyMealCount,
                            targetDailyActivitySessions = settings.targetDailyActivitySessions,  // 1일 목표 활동 횟수
                            activitySessionMinutes = settings.activitySessionMinutes,  // 1회 활동 시간
                            targetDailyActivityMinutes = settings.targetDailyActivityMinutes,  // 1일 총 목표 활동 시간 (서버 계산)
                            targetWeight = settings.targetWeight.toFloat()
                        )
                    }
                    is AppResult.Error -> {
                        // 설정이 없는 경우 기본값 유지 (null로 목표없음 상태)
                        _uiState.value = _uiState.value.copy(
                            targetFeedCount = null,
                            targetDailyActivitySessions = null,
                            activitySessionMinutes = null,
                            targetDailyActivityMinutes = null,
                            targetWeight = null
                        )
                    }
                    is AppResult.Exception -> {
                        // 네트워크 오류 등의 경우 기본값 유지
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "케어 설정을 불러올 수 없습니다: ${result.throwable.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "케어 설정을 불러오는 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }
    /**
     * 현재 데이터를 다시 로드 (설정 변경 후 호출)
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                when (val petResult = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        val petId = petResult.data.id
                        loadCareSettings(petId)
                        loadCareDataForDate(_uiState.value.selectedDate, petId)
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "반려동물 정보를 찾을 수 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "데이터 새로고침 중 오류가 발생했습니다."
                )
            }
        }
    }
    /**
     * 에러 메시지 초기화
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * 사료 기록 추가
     */
    fun addFeedRecord() {
        viewModelScope.launch {
            try {
                when (val petResult = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        val petId = petResult.data.id
                        val timestamp = System.currentTimeMillis()
                        
                        when (val result = petCareRepository.createCareRecord(
                            petId = petId,
                            recordType = "meal_count",
                            timestamp = timestamp,
                            data = 1, // 1회 급여
                            memo = null
                        )) {
                            is AppResult.Success -> {
                                // 성공 시 데이터 다시 로드
                                loadCareDataForDate(_uiState.value.selectedDate, petId)
                            }
                            is AppResult.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    errorMessage = "사료 기록 추가에 실패했습니다: ${result.message}"
                                )
                            }
                            is AppResult.Exception -> {
                                _uiState.value = _uiState.value.copy(
                                    errorMessage = "사료 기록 추가 중 오류가 발생했습니다."
                                )
                            }
                        }
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "반려동물 정보를 찾을 수 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "사료 기록 추가 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 사료 기록 제거 (가장 최근 기록 삭제)
     */
    fun removeFeedRecord() {
        viewModelScope.launch {
            try {
                when (val petResult = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        val petId = petResult.data.id
                        val dateString = _uiState.value.selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                        // 오늘의 사료 기록 조회
                        when (val recordsResult = petCareRepository.getDailyRecords(petId, dateString)) {
                            is AppResult.Success -> {
                                val feedRecords = recordsResult.data.records.filter { it.recordType == "meal_count" }
                                if (feedRecords.isNotEmpty()) {
                                    // 가장 최근 기록 삭제
                                    val latestRecord = feedRecords.maxByOrNull { it.timestamp }
                                    latestRecord?.let { record ->
                                        when (petCareRepository.deleteCareRecord(petId, record.logId)) {
                                            is AppResult.Success -> {
                                                loadCareDataForDate(_uiState.value.selectedDate, petId)
                                            }
                                            else -> {
                                                _uiState.value = _uiState.value.copy(
                                                    errorMessage = "사료 기록 삭제에 실패했습니다."
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                _uiState.value = _uiState.value.copy(
                                    errorMessage = "사료 기록을 찾을 수 없습니다."
                                )
                            }
                        }
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "반려동물 정보를 찾을 수 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "사료 기록 삭제 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 활동 기록 추가
     */
    fun addActivityRecord(minutes: Int = 10) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                android.util.Log.d("PetCareViewModel", "Adding activity record: $minutes minutes")
                
                when (val petResult = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        val petId = petResult.data.id
                        val timestamp = System.currentTimeMillis()
                        
                        // activity 타입은 data에 직접 숫자(분)를 넣음
                        when (val result = petCareRepository.createCareRecord(
                            petId = petId,
                            recordType = "activity",
                            timestamp = timestamp,
                            data = minutes,  // Map이 아닌 직접 숫자로 전달
                            memo = null
                        )) {
                            is AppResult.Success -> {
                                android.util.Log.d("PetCareViewModel", "Activity record created successfully")
                                // 성공 시 데이터 다시 로드
                                loadCareDataForDate(_uiState.value.selectedDate, petId)
                            }
                            is AppResult.Error -> {
                                android.util.Log.e("PetCareViewModel", "Failed to create activity record: ${result.message}")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "활동 기록 추가에 실패했습니다: ${result.message}"
                                )
                            }
                            is AppResult.Exception -> {
                                android.util.Log.e("PetCareViewModel", "Exception creating activity record", result.throwable)
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "활동 기록 추가 중 오류가 발생했습니다."
                                )
                            }
                        }
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "반려동물 정보를 찾을 수 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PetCareViewModel", "Exception in addActivityRecord", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "활동 기록 추가 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 활동 기록 제거 (지정된 시간만큼 최근 기록에서 차감)
     */
    fun removeActivityRecord(minutes: Int = 10) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                when (val petResult = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        val petId = petResult.data.id
                        val dateString = _uiState.value.selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                        // 오늘의 활동 기록 조회
                        when (val recordsResult = petCareRepository.getDailyRecords(petId, dateString)) {
                            is AppResult.Success -> {
                                val activityRecords = recordsResult.data.records.filter { it.recordType == "activity" }
                                if (activityRecords.isNotEmpty()) {
                                    // 가장 최근 기록 차감 또는 삭제
                                    val latestRecord = activityRecords.maxByOrNull { it.timestamp }
                                    latestRecord?.let { record ->
                                        try {
                                            // activity 타입은 data가 직접 숫자
                                            val currentMinutes = when (val data = record.data) {
                                                is Number -> data.toInt()
                                                is Map<*, *> -> {
                                                    // 하위 호환성: Map 형식도 지원
                                                    val dataMap = data as? Map<String, Any>
                                                    (dataMap?.get("minutes") as? Number)?.toInt() ?: 0
                                                }
                                                else -> 0
                                            }

                                            android.util.Log.d("PetCareViewModel", "Removing $minutes from current $currentMinutes")

                                            if (currentMinutes > minutes) {
                                                // 시간 차감 - data에 직접 숫자 전달
                                                val updateData = currentMinutes - minutes
                                                val updateRequest = CareRecordUpdateRequest(data = updateData, notes = record.notes)
                                                when (petCareRepository.updateCareRecord(petId, record.logId, updateRequest)) {
                                                    is AppResult.Success -> {
                                                        android.util.Log.d("PetCareViewModel", "Activity record updated successfully")
                                                        loadCareDataForDate(_uiState.value.selectedDate, petId)
                                                    }
                                                    else -> {
                                                        _uiState.value = _uiState.value.copy(
                                                            isLoading = false,
                                                            errorMessage = "활동 기록 수정에 실패했습니다."
                                                        )
                                                    }
                                                }
                                            } else {
                                                // 기록 삭제
                                                when (petCareRepository.deleteCareRecord(petId, record.logId)) {
                                                    is AppResult.Success -> {
                                                        android.util.Log.d("PetCareViewModel", "Activity record deleted successfully")
                                                        loadCareDataForDate(_uiState.value.selectedDate, petId)
                                                    }
                                                    else -> {
                                                        _uiState.value = _uiState.value.copy(
                                                            isLoading = false,
                                                            errorMessage = "활동 기록 삭제에 실패했습니다."
                                                        )
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("PetCareViewModel", "Error processing activity record", e)
                                            _uiState.value = _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = "활동 기록 처리 중 오류가 발생했습니다."
                                            )
                                        }
                                    }
                                } else {
                                    // 삭제할 기록이 없음
                                    _uiState.value = _uiState.value.copy(isLoading = false)
                                }
                            }
                            else -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "활동 기록을 찾을 수 없습니다."
                                )
                            }
                        }
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "반려동물 정보를 찾을 수 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "활동 기록 제거 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val petName: String = "",
        val selectedPetId: String = "",
        val petImageUrl: String? = null,
        val selectedDate: LocalDate = LocalDate.now(),
        
        // 케어 설정 (목표값들)
        val targetFeedCount: Int? = null,
        val targetDailyActivitySessions: Int? = null,  // 1일 목표 활동 횟수 (예: 3회)
        val activitySessionMinutes: Int? = null,  // 1회 활동 시간 (예: 30분)
        val targetDailyActivityMinutes: Int? = null,  // 1일 총 목표 활동 시간 (서버 계산, 예: 90분)
        val targetWeight: Float? = null,
        
        // 현재 기록된 데이터
        val currentFeedCount: Int = 0,
        val currentActivityMinutes: Int = 0,
        val currentWeight: Float? = null,
        val weightText: String? = null,  // 몸무게 텍스트 (예: "50kg")
        val poopRecords: List<String> = emptyList(),
        val vomitRecords: List<String> = emptyList(),
        
        // 오늘의 최신 기록 (홈 화면 카드 표시용)
        val todayLatestPoopRecord: String? = null,  // 대변 상세 정보 (예: "초록색, 점액 섞임")
        val todayLatestVomitRecord: String? = null  // 구토 상세 정보 (예: "노란색")
    )
}