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
    
    // petId 캐싱 - N+1 query 문제 해결
    private var cachedPetId: String? = null
    
    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // 반려동물 정보 가져오기 및 petId 캐싱 (최초 1회만)
                when (val petResult = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        val pet = petResult.data
                        cachedPetId = pet.id // 캐싱!
                        
                        _uiState.value = _uiState.value.copy(
                            petName = pet.name,
                            petImageUrl = pet.profileImageUrl // 마이페이지와 동일한 프로필 이미지 사용
                        )
                        
                        // 캐시된 ID 사용해서 케어 설정과 오늘 기록 로딩
                        loadCareDataForDate(_uiState.value.selectedDate, cachedPetId!!)
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
        
        // 캐시된 petId 사용 (API 호출 불필요)
        cachedPetId?.let { petId ->
            loadCareDataForDate(date, petId)
        } ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "반려동물 정보가 없습니다. 새로고침해주세요."
            )
        }
    }

    fun changeMonth(monthOffset: Int) {
        val currentDate = _uiState.value.selectedDate
        val newDate = currentDate.plusMonths(monthOffset.toLong())
        _uiState.value = _uiState.value.copy(selectedDate = newDate)
    }

    /**
     * 특정 날짜의 케어 데이터 로딩 (summary api 호출로 설정값과 기록 한 번에 조회)
     */
    private fun loadCareDataForDate(date: LocalDate, petId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                android.util.Log.d("PetCareViewModel", "Loading care data for date: $dateString, petId: $petId")

                when (val result = petCareRepository.getDailySummary(petId, dateString)) {
                    is AppResult.Success -> {
                        val summaryData = result.data
                        val meta = summaryData.meta

                        val feedCount: Int = meta.mealCount ?: 0// meal_count -> mealCount 로 수정
                        val activityMinutes: Int = meta.activityMinutes ?: 0 // activity_minutes -> activityMinutes 로 수정

                        val currentWeight = meta.weight?.toFloat()
                        val latestPoopRecord = meta.stool
                        val latestVomitRecord = meta.vomit
                        val goalProgress = summaryData.goalProgress
                        val targetFeed = goalProgress?.achievements?.meal?.goal
                        val targetActivity = goalProgress?.achievements?.activity?.goal
                        val targetWeightValue = goalProgress?.achievements?.weight?.goal
                        val weightText = currentWeight?.let {
                            String.format("%.1fkg", it)
                        }

                        _uiState.value = _uiState.value.copy(
                            currentFeedCount = feedCount,
                            currentActivityMinutes = activityMinutes,
                            currentWeight = currentWeight,
                            weightText = weightText,
                            todayLatestPoopRecord = latestPoopRecord,
                            todayLatestVomitRecord = latestVomitRecord,
                            targetFeedCount = targetFeed,
                            targetDailyActivityMinutes = targetActivity,
                            targetWeight = targetWeightValue,
                            isLoading = false,
                            errorMessage = null // 성공 시 에러 메시지 초기화
                        )
                        android.util.Log.d("PetCareViewModel", "UI State updated - Activity: $activityMinutes, Feed: $feedCount")
                    }
                    is AppResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            currentFeedCount = 0,
                            currentActivityMinutes = 0,
                            currentWeight = null,
                            weightText = null,
                            todayLatestPoopRecord = null,
                            todayLatestVomitRecord = null,
                            isLoading = false,
                            errorMessage = null
                        )
                        android.util.Log.w("PetCareViewModel", "Error from getDailySummary: ${result.message}")
                    }
                    is AppResult.Exception -> {
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
     * 현재 데이터를 다시 로드 (설정 변경 후 호출)
     */
    fun refresh() {
        // 캐시된 petId 사용 (API 호출 불필요)
        cachedPetId?.let { petId ->
            loadCareDataForDate(_uiState.value.selectedDate, petId)
        } ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "반려동물 정보가 없습니다."
            )
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
                // 캐시된 petId 사용
                val petId = cachedPetId ?: run {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "반려동물 정보를 찾을 수 없습니다."
                    )
                    return@launch
                }
                
                val timestamp = System.currentTimeMillis()
                
                when (val result = petCareRepository.createCareRecord(
                    petId = petId,
                    recordType = "meal_count",
                    timestamp = timestamp,
                    data = 1,
                    memo = null
                )) {
                    is AppResult.Success -> {
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
                // 캐시된 petId 사용
                val petId = cachedPetId ?: run {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "반려동물 정보를 찾을 수 없습니다."
                    )
                    return@launch
                }
                
                val dateString = _uiState.value.selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                when (val recordsResult = petCareRepository.getDailyRecords(petId, dateString)) {
                    is AppResult.Success -> {
                        val feedRecords = recordsResult.data.records.filter { it.recordType == "meal_count" }
                        if (feedRecords.isNotEmpty()) {
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
                
                // 캐시된 petId 사용
                val petId = cachedPetId ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "반려동물 정보를 찾을 수 없습니다."
                    )
                    return@launch
                }
                
                val timestamp = System.currentTimeMillis()

                when (val result = petCareRepository.createCareRecord(
                    petId = petId,
                    recordType = "activity",
                    timestamp = timestamp,
                    data = minutes,
                    memo = null
                )) {
                    is AppResult.Success -> {
                        android.util.Log.d("PetCareViewModel", "Activity record created successfully")
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

                // 캐시된 petId 사용
                val petId = cachedPetId ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "반려동물 정보를 찾을 수 없습니다."
                    )
                    return@launch
                }
                
                val dateString = _uiState.value.selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                        when (val recordsResult = petCareRepository.getDailyRecords(petId, dateString)) {
                            is AppResult.Success -> {
                                val activityRecords = recordsResult.data.records.filter { it.recordType == "activity" }
                                if (activityRecords.isNotEmpty()) {
                                    val latestRecord = activityRecords.maxByOrNull { it.timestamp }
                                    latestRecord?.let { record ->
                                        try {
                                            val currentMinutes = when (val data = record.data) {
                                                is Number -> data.toInt()
                                                is Map<*, *> -> {
                                                    val dataMap = data as? Map<String, Any>
                                                    (dataMap?.get("minutes") as? Number)?.toInt() ?: 0
                                                }
                                                else -> 0
                                            }

                                            android.util.Log.d("PetCareViewModel", "Removing $minutes from current $currentMinutes")

                                            if (currentMinutes > minutes) {
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
        val targetDailyActivitySessions: Int? = null,
        val activitySessionMinutes: Int? = null,
        val targetDailyActivityMinutes: Int? = null,
        val targetWeight: Float? = null,
        
        // 현재 기록된 데이터
        val currentFeedCount: Int = 0,
        val currentActivityMinutes: Int = 0,
        val currentWeight: Float? = null,
        val weightText: String? = null,
        val poopRecords: List<String> = emptyList(),
        val vomitRecords: List<String> = emptyList(),
        
        // 오늘의 최신 기록 (홈 화면 카드 표시용)
        val todayLatestPoopRecord: String? = null,
        val todayLatestVomitRecord: String? = null
    )
}