package com.example.pet_project_frontend.presentation.care_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.response.DailySummaryResponse
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 사료 관리 화면 ViewModel
 * Summary API를 사용하여 선택한 날짜의 사료 기록 및 달성률 표시
 */
@HiltViewModel
class FeedManagementViewModel @Inject constructor(
    private val petCareRepository: PetCareRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedDate: LocalDate = LocalDate.now(),
        val currentFeedCount: Int = 0,  // 실제 급여 횟수
        val goalFeedCount: Int = 0,     // 목표 급여 횟수
        val achievementPercentage: Float = 0f,  // 달성률
        val isAchieved: Boolean = false,  // 목표 달성 여부

        // 캘린더 표시용 달성 날짜 목록
        val achievedDates: Set<LocalDate> = emptySet(),
        val monthlySummaryText: String = "",
        val monthlyMessage: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var currentPetId: String? = null

    init {
        loadPetId()
    }

    /**
     * 펫 ID만 로드 (날짜별 데이터는 화면의 LaunchedEffect에서 로드)
     */
    private fun loadPetId() {
        if (currentPetId != null) return // 이미 로드되어 있으면 스킵
        
        viewModelScope.launch {
            try {
                when (val petResult = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        currentPetId = petResult.data.id
                        android.util.Log.d("FeedManagementVM", "✅ Pet ID loaded: ${currentPetId}")
                    }
                    is AppResult.Error -> {
                        _uiState.update { it.copy(error = "반려동물 정보를 찾을 수 없습니다.") }
                    }
                    is AppResult.Exception -> {
                        _uiState.update { it.copy(error = "반려동물 정보를 불러오는 중 오류가 발생했습니다.") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "반려동물 정보를 불러오는 중 오류가 발생했습니다.") }
            }
        }
    }

    /**
     * 펫 ID를 동기적으로 로드 (suspend 함수)
     */
    private suspend fun ensurePetIdLoaded(): String? {
        if (currentPetId != null) return currentPetId
        
        return try {
            when (val petResult = petRepository.getMyPetProfile()) {
                is AppResult.Success -> {
                    currentPetId = petResult.data.id
                    android.util.Log.d("FeedManagementVM", "✅ Pet ID loaded: ${currentPetId}")
                    currentPetId
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(error = "반려동물 정보를 찾을 수 없습니다.") }
                    null
                }
                is AppResult.Exception -> {
                    _uiState.update { it.copy(error = "반려동물 정보를 불러오는 중 오류가 발생했습니다.") }
                    null
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "반려동물 정보를 불러오는 중 오류가 발생했습니다.") }
            null
        }
    }

    /**
     * 특정 날짜의 사료 데이터 로드 (Summary API 사용)
     */
    fun loadDataForDate(date: LocalDate) {
        viewModelScope.launch {
            // petId가 없으면 먼저 로드
            val petId = ensurePetIdLoaded() ?: return@launch

            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    error = null, 
                    selectedDate = date
                ) 
            }

            try {
                val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                when (val result = petCareRepository.getDailySummary(petId, dateString)) {
                    is AppResult.Success -> {
                        val summary = result.data as? DailySummaryResponse
                        if (summary?.goalProgress?.achievements?.meal == null) {
                            _uiState.update { it.copy(isLoading = false, error = "요약 데이터의 형식이 올바르지 않습니다.") }
                            return@launch
                        }

                        val mealAchievement = summary.goalProgress.achievements.meal

                        val actualCount = (mealAchievement.actual as? Number)?.toInt() ?: 0
                        val goalCount = (mealAchievement.goal as? Number)?.toInt() ?: 0
                        val percentage = mealAchievement.percentage ?: 0f
                        val achieved = mealAchievement.achieved ?: false

                        android.util.Log.d("FeedManagementVM", "✅ Loaded data for date=$date: actual=$actualCount, goal=$goalCount, percentage=$percentage")

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentFeedCount = actualCount,
                                goalFeedCount = goalCount,
                                achievementPercentage = percentage,
                                isAchieved = achieved
                            )
                        }

                        // 월간 데이터도 선택된 날짜 기준으로 로드
                        loadAchievedDatesForMonth(YearMonth.from(date))
                    }
                    is AppResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                    is AppResult.Exception -> {
                        _uiState.update { it.copy(isLoading = false, error = "데이터를 불러오는 중 오류가 발생했습니다.") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "데이터를 불러오는 중 오류가 발생했습니다.") }
            }
        }
    }

    fun addFeedRecord() {
        val petId = currentPetId ?: return

        viewModelScope.launch {
            try {
                val timestamp = System.currentTimeMillis()

                when (petCareRepository.createCareRecord(
                    petId = petId,
                    recordType = "meal_count",
                    timestamp = timestamp,
                    data = 1,
                    memo = null

                )) {
                    is AppResult.Success -> {
                        loadDataForDate(_uiState.value.selectedDate)
                        loadAchievedDatesForMonth(YearMonth.from(_uiState.value.selectedDate))
                    }
                    is AppResult.Error -> {
                        _uiState.update { it.copy(error = "사료 기록 추가에 실패했습니다.") }
                    }
                    is AppResult.Exception -> {
                        _uiState.update { it.copy(error = "사료 기록 추가 중 오류가 발생했습니다.") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "사료 기록 추가 중 오류가 발생했습니다.") }
            }
        }
    }

    /**
     * 사료 급여 기록 삭제 (가장 최근 기록 1개)
     */
    fun removeFeedRecord() {
        val petId = currentPetId ?: return

        viewModelScope.launch {
            try {
                val dateString = _uiState.value.selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                when (val result = petCareRepository.getDailyRecords(petId, dateString)) {
                    is AppResult.Success -> {
                        val latestMealRecord = result.data.records
                            .filter { it.recordType == "meal_count" }
                            .maxByOrNull { it.timestamp }

                        if (latestMealRecord != null) {
                            when (petCareRepository.deleteCareRecord(petId, latestMealRecord.logId)) {
                                is AppResult.Success -> {
                                    loadDataForDate(_uiState.value.selectedDate)
                                    loadAchievedDatesForMonth(YearMonth.from(_uiState.value.selectedDate))
                                }
                                else -> {
                                    _uiState.update { it.copy(error = "사료 기록 삭제에 실패했습니다.") }
                                }
                            }
                        } else {
                            _uiState.update { it.copy(error = "삭제할 기록이 없습니다.") }
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(error = "기록을 불러올 수 없습니다.") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "사료 기록 삭제 중 오류가 발생했습니다.") }
            }
        }
    }

    fun refresh() {
        loadDataForDate(_uiState.value.selectedDate)
    }


    /**
     * 특정 월의 목표 달성 날짜들과 메시지를 로드
     * @param yearMonth 조회할 연월 (예: 2025-10)
     */
    fun loadAchievedDatesForMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            val petId = ensurePetIdLoaded() ?: return@launch
            try {
                val startDate = yearMonth.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                android.util.Log.d("FeedManagementVM", "Loading achieved dates for period: $startDate ~ $endDate")

                when (val result = petCareRepository.getRangeSummary(petId, startDate, endDate)) {
                    is AppResult.Success -> {
                        val summary = result.data
                        android.util.Log.d("FeedManagementVM", "Range summary: goal_tracking=${summary.goalTracking}")

                        val daysAchievedData = summary.goalTracking?.get("days_achieved") as? Map<*, *>
                        val mealDaysAchieved = when (val mealValue = daysAchievedData?.get("meal")) {
                            is Number -> mealValue.toInt()
                            is String -> mealValue.toIntOrNull() ?: 0
                            else -> 0
                        }

                        android.util.Log.d("FeedManagementVM", "Meal days achieved: $mealDaysAchieved")

                        val achievementDatesMap = summary.goalTracking?.get("achievement_dates") as? Map<*, *>

                        val mealDateStrings = (achievementDatesMap?.get("meal") as? List<*>)
                            ?.mapNotNull { it as? String }
                            ?: emptyList()

                        android.util.Log.d("FeedManagementVM", "Meal achievement dates from server: $mealDateStrings")


                        val achievedDates = mealDateStrings.mapNotNull { dateStr ->
                            try {
                                LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            } catch (e: Exception) {
                                android.util.Log.e("FeedManagementVM", "Failed to parse date: $dateStr", e)
                                null
                            }
                        }.toSet()

                        val summaryText = "${yearMonth.monthValue}월에는 목표를 ${mealDaysAchieved}번 채웠네요"
                        val today = LocalDate.now()
                        val currentMonth = YearMonth.from(today)
                        val isCurrentMonth = yearMonth == currentMonth
                        val isPastMonth = yearMonth.isBefore(currentMonth)
                        val applicableDays = if (isCurrentMonth) today.dayOfMonth else yearMonth.lengthOfMonth()
                        val achievementRate = if (applicableDays > 0) (mealDaysAchieved.toDouble() / applicableDays) * 100 else 0.0
                        val analysisMessage = when {
                            isCurrentMonth -> {
                                if (mealDaysAchieved == 0) "아직 목표를 채우지 못했어요"
                                else if (achievementRate >= 50) "이 기세를 계속 이어가요!"
                                else "조금만 더 힘내세요!"
                            }
                            isPastMonth -> {
                                if (mealDaysAchieved == 0) "다음에 더 노력해봐요"
                                else if (achievementRate >= 50) "정말 대단해요!"
                                else "다음에 더 노력해봐요" // mealDaysAchieved가 0이 아닌 과거 월은 이 메시지가 나옴
                            }
                            else -> "기록을 시작해보세요!"
                        }
                        android.util.Log.d("FeedManagementVM", "Achieved dates from records: $achievedDates")
                        android.util.Log.d("FeedManagementVM", "Summary Text: $summaryText")
                        android.util.Log.d("FeedManagementVM", "Analysis Message: $analysisMessage")

                        _uiState.update {
                            it.copy(
                                achievedDates = achievedDates,
                                monthlySummaryText = summaryText,
                                monthlyMessage = analysisMessage
                            )
                        }
                    }
                    is AppResult.Error -> {
                        android.util.Log.e("FeedManagementVM", "Failed to load achieved dates: ${result.message}")
                    }
                    is AppResult.Exception -> {
                        android.util.Log.e("FeedManagementVM", "Exception loading achieved dates", result.throwable)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("FeedManagementVM", "Error loading achieved dates", e)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
