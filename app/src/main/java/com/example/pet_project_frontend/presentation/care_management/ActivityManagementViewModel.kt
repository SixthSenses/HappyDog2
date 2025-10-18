package com.example.pet_project_frontend.presentation.care_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 활동 관리 화면 ViewModel
 * Summary API를 사용하여 선택한 날짜의 활동 기록 및 달성률 표시
 */
@HiltViewModel
class ActivityManagementViewModel @Inject constructor(
    private val petCareRepository: PetCareRepository,
    private val petRepository: PetRepository
) : ViewModel() {

        data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedDate: LocalDate = LocalDate.now(),

        // '선택된 날짜'의 실제 활동 횟수
        //val selectedDateActivityCount: Int = 0,
        // '선택된 날짜'의 목표 활동 횟수
        val goalActivityCount: Int = 0,
        // '선택된 날짜'의 1회 활동 시간(분)
        val selectedDateSessionMinutes: Int = 0,

            // '선택된 날짜'의 실시간 활동 시간(막대바 위 텍스트)
        val selectedDateActivityLiveMinutes: Int = 0,
        val selectedDateAchievementPercentage: Float = 0f,  // 달성률
        // '이전 기록'(선택된 날짜 - 1일)의 실제 활동 횟수
        //val previousDayActivityCount: Int = 0,
        // '이전 기록'(선택된 날짜 - 1일)의 1회 활동 시간(분)
        //val previousDaySessionMinutes: Int = 0,
            // 선택한 날 전날의 활동 시간(막대바 위 텍스트)
        val previousDateActivityMinutes: Int = 0,
        val previousDateAchivementPercentage: Float = 0f, // 선택한 날 전날의 달성률
        //val isAchieved: Boolean = false,  // 목표 달성 여부
        
        // 캘린더 표시용 달성 날짜 목록
        val achievedDates: Set<LocalDate> = emptySet(),
        val monthlySummaryText: String = "",
        
        // 월간 분석 메시지
        val monthlyMessage: String = "이번 달 목표를 0일 달성했어요"
    )


    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var currentPetId: String? = null

    init {
        loadPetAndData()
    }

    private fun loadPetAndData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                when (val petResult = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> {
                        currentPetId = petResult.data.id
                        loadDataForDate(LocalDate.now())
                        loadAchievedDatesForMonth(YearMonth.now())
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "반려동물 정보를 찾을 수 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "데이터를 불러오는 중 오류가 발생했습니다."
                )
            }
        }
    }

    /**
     * 특정 날짜의 활동 데이터 로드 (Summary API 사용)
     */
    fun loadDataForDate(date: LocalDate) {
        val petId = currentPetId ?: return

        viewModelScope.launch {
            // 날짜 변경 시 이전 데이터 초기화 (중요!)
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null, 
                selectedDate = date,
                selectedDateActivityLiveMinutes = 0,
                goalActivityCount = 0,
                previousDateActivityMinutes = 0,
                previousDateAchivementPercentage = 0f,
                selectedDateAchievementPercentage = 0f
            )

            try {
                // 1. 선택된 날짜(date)의 데이터 요청
                val currentDateResult = petCareRepository.getDailySummary(petId, date.format(DateTimeFormatter.ISO_LOCAL_DATE))

                // 2. 그 전날(date - 1일)의 데이터 요청
                val previousDateResult = petCareRepository.getDailySummary(petId, date.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE))

                // 두 요청이 모두 성공했을 때만 UI 업데이트
                if (currentDateResult is AppResult.Success && previousDateResult is AppResult.Success) {
                    val currentSummary = currentDateResult.data
                    val previousSummary = previousDateResult.data

                    // --- 선택된 날짜의 데이터 추출 ---
                    //val currentActualCount = (currentSummary.recordCounts["activity"] as? Number)?.toInt() ?: 0
                    val currentGoalSessions = currentSummary.goalProgress?.achievements?.activity?.detail?.sessions ?: 0
                    val currentSessionMinutes = currentSummary.goalProgress?.achievements?.activity?.detail?.minutesPerSession ?: 0
                    val selectedDateActivityLiveMinutes = currentSummary.goalProgress?.achievements?.activity?.actual ?: 0
                    //선택한 날짜의 달성률
                    val selectedDatePercentage = currentSummary.goalProgress?.achievements?.activity?.percentage ?: 0f
                    // --- 그 전날의 데이터 추출 ---
                    val previousDateActivityMinutes = previousSummary.goalProgress?.achievements?.activity?.actual ?: 0f
                    // 선택한 날짜 전날의 달성률
                    val previousDatePercentage = previousSummary.goalProgress?.achievements?.activity?.percentage ?: 0f
                    // 이전 날의 세션 시간은 목표 설정에서 가져옴 (목표가 없으면 0)
                    //val previousSessionMinutes = previousSummary.goalProgress?.achievements?.activity?.detail?.minutesPerSession ?: 0



                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        //selectedDateActivityCount = currentActualCount,
                        goalActivityCount = currentGoalSessions,
                        selectedDateSessionMinutes = currentSessionMinutes,
                        selectedDateActivityLiveMinutes = (selectedDateActivityLiveMinutes as? Number)?.toInt() ?: 0,
                        previousDateActivityMinutes = (previousDateActivityMinutes as? Number)?.toInt() ?: 0,
                        //previousDaySessionMinutes = previousSessionMinutes,
                        previousDateAchivementPercentage = previousDatePercentage,
                        selectedDateAchievementPercentage = selectedDatePercentage,
                        //isAchieved = currentGoalSessions > 0 && currentActualCount >= currentGoalSessions
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "데이터를 가져오는 데 실패했습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "데이터 로딩 중 오류가 발생했습니다.")
            }
        }
    }

    /**
     * 활동 기록 추가 -> 홈화면 + 버튼 뷰모델 사용
     */
    fun addActivityRecord(sessionMinutes: Int) {
        val petId = currentPetId ?: return

        viewModelScope.launch {
            try {
                val timestamp = System.currentTimeMillis()
                
                when (petCareRepository.createCareRecord(
                    petId = petId,
                    recordType = "activity",
                    timestamp = timestamp,
                    data = sessionMinutes,  // 숫자로 전송
                    memo = null
                )) {
                    is AppResult.Success -> {
                        // 성공 후 현재 선택된 날짜의 데이터 다시 로드
                        loadDataForDate(_uiState.value.selectedDate)
                        loadAchievedDatesForMonth(YearMonth.from(_uiState.value.selectedDate))
                    }
                    is AppResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = "활동 기록 추가에 실패했습니다."
                        )
                    }
                    is AppResult.Exception -> {
                        _uiState.value = _uiState.value.copy(
                            error = "활동 기록 추가 중 오류가 발생했습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "활동 기록 추가 중 오류가 발생했습니다."
                )
            }
        }
    }

    /**
     * 활동 기록 삭제 -> 홈화면 - 버튼 뷰모델 사용
     */
    fun removeActivityRecord(sessionMinutes: Int) {
        val petId = currentPetId ?: return

        viewModelScope.launch {
            try {
                val dateString = _uiState.value.selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                
                // 현재 날짜의 모든 기록 가져오기
                when (val result = petCareRepository.getDailyRecords(petId, dateString)) {
                    is AppResult.Success -> {
                        // activity 타입 기록 중 가장 최근 것 찾기
                        val latestActivityRecord = result.data.records
                            .filter { it.recordType == "activity" }
                            .maxByOrNull { it.timestamp }
                        
                        if (latestActivityRecord != null) {
                            // 기록 삭제
                            when (petCareRepository.deleteCareRecord(petId, latestActivityRecord.logId)) {
                                is AppResult.Success -> {
                                    // 성공 후 데이터 다시 로드
                                    loadDataForDate(_uiState.value.selectedDate)
                                    loadAchievedDatesForMonth(YearMonth.from(_uiState.value.selectedDate))
                                }
                                else -> {
                                    _uiState.value = _uiState.value.copy(
                                        error = "활동 기록 삭제에 실패했습니다."
                                    )
                                }
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(
                                error = "삭제할 기록이 없습니다."
                            )
                        }
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            error = "기록을 불러올 수 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "활동 기록 삭제 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun refresh() {
        loadDataForDate(_uiState.value.selectedDate)
    }
    
    /**
     * 특정 월의 목표 달성 날짜들을 로드
     * @param yearMonth 조회할 연월 (예: 2025-10)
     */
    fun loadAchievedDatesForMonth(yearMonth: java.time.YearMonth) {
        val petId = currentPetId ?: return
        viewModelScope.launch {
            try {
                val startDate = yearMonth.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                android.util.Log.d("ActivityManagementVM", "Loading achieved dates for period: $startDate ~ $endDate")

                when (val result = petCareRepository.getRangeSummary(petId, startDate, endDate)) {
                    is AppResult.Success -> {
                        val summary = result.data
                        android.util.Log.d("ActivityManagementVM", "Range summary response: goal_tracking=${summary.goalTracking}")

                        // --- ▼▼▼ [수정 1] 'meal' -> 'activity'로 변경 ▼▼▼ ---
                        val daysAchievedData = summary.goalTracking?.get("days_achieved") as? Map<*, *>
                        val activityDaysAchieved = when (val activityValue = daysAchievedData?.get("activity")) {
                            is Number -> activityValue.toInt()
                            is String -> activityValue.toIntOrNull() ?: 0
                            else -> 0
                        }

                        android.util.Log.d("ActivityManagementVM", "Activity days achieved: $activityDaysAchieved")

                        // --- ▼▼▼ [수정 2] 'meal' -> 'activity'로 변경 ▼▼▼ ---
                        val achievementDatesMap = summary.goalTracking?.get("achievement_dates") as? Map<*, *>
                        val activityDateStrings = (achievementDatesMap?.get("activity") as? List<*>)
                            ?.mapNotNull { it as? String }
                            ?: emptyList()

                        android.util.Log.d("ActivityManagementVM", "Activity achievement dates from server: $activityDateStrings")

                        val achievedDates = activityDateStrings.mapNotNull { dateStr ->
                            try {
                                LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            } catch (e: Exception) {
                                android.util.Log.e("ActivityManagementVM", "Failed to parse date: $dateStr", e)
                                null
                            }
                        }.toSet()
                        

                        // 1. 월별 요약 텍스트 생성 (활동 기준)
                        val summaryText = "${yearMonth.monthValue}월에는 목표를 ${activityDaysAchieved}번 채웠네요"

                        // 2. 월별 분석 메시지 생성
                        val today = LocalDate.now()
                        val currentMonth = YearMonth.from(today)
                        val isCurrentMonth = yearMonth == currentMonth
                        val isPastMonth = yearMonth.isBefore(currentMonth)

                        val applicableDays = if (isCurrentMonth) today.dayOfMonth else yearMonth.lengthOfMonth()
                        val achievementRate = if (applicableDays > 0) (activityDaysAchieved.toDouble() / applicableDays) * 100 else 0.0

                        val analysisMessage = when {
                            isCurrentMonth -> {
                                if (activityDaysAchieved == 0) "아직 목표를 채우지 못했어요"
                                else if (achievementRate >= 50) "이 기세를 계속 이어가요!"
                                else "조금만 더 힘내세요!"
                            }
                            isPastMonth -> {
                                if (activityDaysAchieved == 0) "다음에 더 노력해봐요"
                                else if (achievementRate >= 50) "정말 대단해요!"
                                else "다음에 더 노력해봐요" // mealDaysAchieved가 0이 아닌 과거 월은 이 메시지가 나옴
                            }
                            else -> "기록을 시작해보세요!"
                        }
                        android.util.Log.d("FeedManagementVM", "Achieved dates from records: $achievedDates")
                        android.util.Log.d("FeedManagementVM", "Summary Text: $summaryText")
                        android.util.Log.d("FeedManagementVM", "Analysis Message: $analysisMessage")

                        _uiState.value = _uiState.value.copy(
                            achievedDates = achievedDates,
                            monthlySummaryText = summaryText,
                            monthlyMessage = analysisMessage,
                        )
                    }
                    is AppResult.Error -> {
                        android.util.Log.e("ActivityManagementVM", "Failed to load achieved dates: ${result.message}")
                    }
                    is AppResult.Exception -> {
                        android.util.Log.e("ActivityManagementVM", "Exception loading achieved dates", result.throwable)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ActivityManagementVM", "Error loading achieved dates", e)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
