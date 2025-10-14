package com.example.pet_project_frontend.presentation.care_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
// 1. DailySummaryResponse를 사용하므로 import 문이 필요합니다.
import com.example.pet_project_frontend.data.remote.dto.response.DailySummaryResponse
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // update 확장 함수를 사용하기 위해 추가
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth // YearMonth를 사용하므로 추가
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

        // 2. 주석 처리된 필드들을 모두 활성화합니다.
        val currentFeedCount: Int = 0,  // 실제 급여 횟수
        val goalFeedCount: Int = 0,     // 목표 급여 횟수

        val achievementPercentage: Float = 0f,  // 달성률
        val isAchieved: Boolean = false,  // 목표 달성 여부

        // 캘린더 표시용 달성 날짜 목록
        val achievedDates: Set<LocalDate> = emptySet(),
        val monthlySummaryText: String = "",
        // 3. 컴파일 오류를 막기 위해 기본값을 할당합니다.
        val monthlyMessage: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var currentPetId: String? = null

    init {
        loadPetAndData()
    }

    private fun loadPetAndData() {
        viewModelScope.launch {
            // .value를 직접 수정하는 대신, 스레드에 안전한 .update를 사용합니다.
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                when (val petResult = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        currentPetId = petResult.data.id
                        // petId를 가져온 후 즉시 오늘 날짜의 데이터를 로드합니다.
                        loadDataForDate(LocalDate.now())
                        loadAchievedDatesForMonth(YearMonth.now())
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false, error = "반려동물 정보를 찾을 수 없습니다.") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "데이터를 불러오는 중 오류가 발생했습니다.") }
            }
        }
    }

    /**
     * 특정 날짜의 사료 데이터 로드 (Summary API 사용)
     */
    fun loadDataForDate(date: LocalDate) {
        val petId = currentPetId ?: run {
            loadPetAndData()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedDate = date) }

            try {
                val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                when (val result = petCareRepository.getDailySummary(petId, dateString)) {
                    is AppResult.Success -> {
                        // result.data의 타입을 명시적으로 DailySummaryResponse로 캐스팅합니다.
                        val summary = result.data as? DailySummaryResponse
                        if (summary?.goalProgress?.achievements?.meal == null) {
                            _uiState.update { it.copy(isLoading = false, error = "요약 데이터의 형식이 올바르지 않습니다.") }
                            return@launch
                        }

                        // 4. 서버 응답의 goal_progress 객체를 직접 사용합니다.
                        val mealAchievement = summary.goalProgress.achievements.meal

                        val actualCount = (mealAchievement.actual as? Number)?.toInt() ?: 0
                        val goalCount = (mealAchievement.goal as? Number)?.toInt() ?: 0
                        val percentage = mealAchievement.percentage ?: 0f // <-- 사용자의 요청: 계산 대신 직접 사용
                        val achieved = mealAchievement.achieved ?: false

                        android.util.Log.d("FeedManagementVM", "✅ SERVER DATA - Actual: $actualCount, Goal: $goalCount, Percentage: $percentage")

                        // 5. 모든 주석을 해제하고 UiState를 올바르게 업데이트합니다.
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentFeedCount = actualCount,
                                goalFeedCount = goalCount,
                                achievementPercentage = percentage, // API에서 받은 percentage 사용
                                isAchieved = achieved
                            )
                        }
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
    // ... (addFeedRecord, removeFeedRecord 등 나머지 함수들은 그대로 유지)
    fun addFeedRecord() {
        val petId = currentPetId ?: return

        viewModelScope.launch {
            try {
                val timestamp = System.currentTimeMillis()

                when (petCareRepository.createCareRecord(
                    petId = petId,
                    recordType = "meal",
                    timestamp = timestamp,
                    data = "급여",
                    memo = null
                )) {
                    is AppResult.Success -> {
                        // 성공 후 현재 선택된 날짜의 데이터 다시 로드
                        loadDataForDate(_uiState.value.selectedDate)
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

                // 현재 날짜의 모든 기록 가져오기
                when (val result = petCareRepository.getDailyRecords(petId, dateString)) {
                    is AppResult.Success -> {
                        // meal 타입 기록 중 가장 최근 것 찾기
                        val latestMealRecord = result.data.records
                            .filter { it.recordType == "meal" }
                            .maxByOrNull { it.timestamp }

                        if (latestMealRecord != null) {
                            // 기록 삭제
                            when (petCareRepository.deleteCareRecord(petId, latestMealRecord.logId)) {
                                is AppResult.Success -> {
                                    // 성공 후 데이터 다시 로드
                                    loadDataForDate(_uiState.value.selectedDate)
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

    // ###################### ▼▼▼ 월간 분석 로직 수정 ▼▼▼ ######################
    /**
     * 특정 월의 목표 달성 날짜들과 메시지를 로드
     * @param yearMonth 조회할 연월 (예: 2025-10)
     */
    /**
     * 특정 월의 목표 달성 날짜들과 메시지를 로드
     * @param yearMonth 조회할 연월 (예: 2025-10)
     */
    fun loadAchievedDatesForMonth(yearMonth: YearMonth) {
        val petId = currentPetId ?: return
        viewModelScope.launch {
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

                        val achievedDateStrings = mutableSetOf<String>()
                        summary.recordsByDate.forEach { (dateStr, records) ->
                            if (records is List<*>) {
                                if (records.any { (it as? Map<*, *>)?.get("record_type") == "meal_count" }) {
                                    achievedDateStrings.add(dateStr)
                                }
                            }
                        }

                        val achievedDates = achievedDateStrings.mapNotNull { dateStr ->
                            try { LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
                            catch (e: Exception) {
                                android.util.Log.e("FeedManagementVM", "Failed to parse date: $dateStr", e)
                                null
                            }
                        }.toSet()

                        // --- ▼▼▼ 메시지 생성 로직 수정 ▼▼▼ ---

                        // 1. 월별 요약 텍스트 생성 (수정 없음)
                        val summaryText = "${yearMonth.monthValue}월에는 목표를 ${mealDaysAchieved}번 채웠네요"

                        // 2. 월별 분석 메시지 생성 (로직 순서 변경)
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
                                if (mealDaysAchieved == 0) "아직 목표를 채우지 못했어요"
                                else if (achievementRate >= 50) "정말 대단해요!"
                                else "다음에 더 노력해봐요" // mealDaysAchieved가 0이 아닌 과거 월은 이 메시지가 나옴
                            }
                            else -> "기록을 시작해보세요!"
                        }
                        android.util.Log.d("FeedManagementVM", "Achieved dates from records: $achievedDates")
                        android.util.Log.d("FeedManagementVM", "Summary Text: $summaryText")
                        android.util.Log.d("FeedManagementVM", "Analysis Message: $analysisMessage")

                        // 3. 두 메시지를 모두 UiState에 업데이트
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
