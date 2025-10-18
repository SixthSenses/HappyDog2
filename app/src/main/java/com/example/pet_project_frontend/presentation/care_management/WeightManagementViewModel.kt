package com.example.pet_project_frontend.presentation.care_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.PetCareSettingsRequest
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async // 'async'를 직접 import 하도록 명시
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class WeightManagementViewModel @Inject constructor(
    private val petCareRepository: PetCareRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    data class MonthlyWeightData(
        val month: String,
        val weight: Float
    )

    data class UiState(
        val isLoading: Boolean = false, // false로 시작 (필요할 때만 true)
        val isSaveSuccess: Boolean = false,
        val error: String? = null,
        val selectedDate: LocalDate = LocalDate.now(),
        val selectedDateWeight: Float? = null,
        val targetWeight: Float? = null,
        val weightDiff: Float? = null,
        val todayWeight: Float? = null,
        val monthlyWeights: List<MonthlyWeightData> = emptyList(),
        val monthlyAnalysisText: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var petId: String? = null

    init {
        loadPetId()
        loadMonthlyAnalysis()
    }

    private fun loadPetId() {
        viewModelScope.launch {
            if (petId != null) return@launch
            when (val petResult = petRepository.getMyPetProfile()) {
                is AppResult.Success -> {
                    petId = petResult.data.id
                    android.util.Log.d("WeightManagementVM", "Loaded petId: $petId")
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false, error = "반려동물 정보를 찾을 수 없습니다.") }
                }
            }
        }
    }



    private fun loadMonthlyAnalysis() {
        viewModelScope.launch {
            if (petId == null) {
                var retryCount = 0
                while (petId == null && retryCount < 10) {
                    kotlinx.coroutines.delay(100)
                    retryCount++
                }
            }
            
            petId?.let { id ->
                android.util.Log.d("WeightManagementVM", "Starting monthly analysis load for petId: $id")
                when (val result = petCareRepository.getWeightMonthlyAnalysis(id)) {
                    is AppResult.Success -> {
                        val data = result.data
                        android.util.Log.d("WeightManagementVM", "Monthly analysis API success: ${data.monthlyData.size} months")
                        
                        // API에서 받은 월간 데이터를 ViewModel 데이터로 변환
                        val monthlyData = data.monthlyData.map { item ->
                            MonthlyWeightData(
                                month = item.label, // "5월", "6월", ... 또는 "10월" (현재 월)
                                weight = item.averageWeight?.toFloat() ?: 0f
                            )
                        }
                        
                        // API에서 제공하는 분석 텍스트 사용
                        val analysisText = data.analysis.title
                        
                        _uiState.update { 
                            it.copy(
                                monthlyWeights = monthlyData,
                                monthlyAnalysisText = analysisText
                            ) 
                        }
                        android.util.Log.d("WeightManagementVM", "Monthly analysis loaded: title='$analysisText'")
                    }
                    is AppResult.Error -> {
                        android.util.Log.e("WeightManagementVM", "Monthly analysis API error: ${result.message}")
                        // 에러 시 기본 텍스트 설정
                        _uiState.update { 
                            it.copy(
                                monthlyWeights = emptyList(),
                                monthlyAnalysisText = "6개월 동안 매월 한 번 이상\n기록하면 분석을 볼 수 있어요"
                            )
                        }
                    }
                    is AppResult.Exception -> {
                        android.util.Log.e("WeightManagementVM", "Monthly analysis exception: ${result.throwable?.message}", result.throwable)
                        // 예외 시 기본 텍스트 설정
                        _uiState.update { 
                            it.copy(
                                monthlyWeights = emptyList(),
                                monthlyAnalysisText = "6개월 동안 매월 한 번 이상\n기록하면 분석을 볼 수 있어요"
                            )
                        }
                    }
                }
            }
        }
    }

    // 더 이상 사용하지 않음 (API에서 분석 텍스트 제공)
    @Deprecated("API에서 분석 텍스트를 제공합니다")
    private fun generateAnalysisText(sixMonthsAgo: Float?, currentMonth: Float?): String {
        return when {
            sixMonthsAgo == null || currentMonth == null -> "6개월간 몸무게 데이터가 부족해요"
            currentMonth > sixMonthsAgo -> "6개월 전보다\n몸무게가 늘었어요"
            currentMonth < sixMonthsAgo -> "6개월 전보다\n몸무게가 줄었어요"
            else -> "6개월간\n몸무게가 비슷해요"
        }
    }

    fun loadDataForDate(selectedDate: LocalDate) {
        viewModelScope.launch {
            if (petId == null) {
                loadPetId()
                var retryCount = 0
                while (petId == null && retryCount < 10) {
                    kotlinx.coroutines.delay(100)
                    retryCount++
                }
                if (petId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "반려동물 정보를 찾을 수 없습니다.") }
                    return@launch
                }
            }
            
            _uiState.update { it.copy(isLoading = true, selectedDate = selectedDate, error = null) }
            
            android.util.Log.d("WeightManagementVM", "Loading data for date: $selectedDate (petId: $petId)")
            
            try {
                if (selectedDate == LocalDate.now()) {
                    android.util.Log.d("WeightManagementVM", "Loading today's data")
                    when (val result = petCareRepository.getDailySummary(petId!!, selectedDate.toString())) {
                        is AppResult.Success -> {
                            val data = result.data
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    todayWeight = data.meta.weight,
                                    selectedDateWeight = data.meta.weight,
                                    targetWeight = data.goalProgress?.achievements?.weight?.goal,
                                    weightDiff = data.goalProgress?.achievements?.weight?.diff
                                )
                            }
                        }
                        else -> _uiState.update { it.copy(isLoading = false, error = "데이터 로드에 실패했습니다.") }
                    }
                } else {
                    android.util.Log.d("WeightManagementVM", "Loading selected date: $selectedDate and today's data")
                    coroutineScope {
                        val todayAsync = async { petCareRepository.getDailySummary(petId!!, LocalDate.now().toString()) }
                        val selectedAsync = async { petCareRepository.getDailySummary(petId!!, selectedDate.toString()) }
                        
                        val todayResult = todayAsync.await()
                        val selectedResult = selectedAsync.await()
                        
                        if (todayResult is AppResult.Success && selectedResult is AppResult.Success) {
                            val todayData = todayResult.data
                            val selectedData = selectedResult.data
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    todayWeight = todayData.meta.weight,
                                    selectedDateWeight = selectedData.meta.weight,
                                    targetWeight = selectedData.goalProgress?.achievements?.weight?.goal,
                                    weightDiff = selectedData.goalProgress?.achievements?.weight?.diff
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, error = "데이터 로드에 실패했습니다.") }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("WeightManagementVM", "Exception loading data", e)
                _uiState.update { it.copy(isLoading = false, error = "데이터 로드 중 오류가 발생했습니다.") }
            }
        }
    }

    fun saveWeightRecord(weight: Float, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            petId ?: run {
                _uiState.update { it.copy(isLoading = false, error = "펫 정보가 없습니다.") }
                return@launch
            }
            val timestamp = date.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            android.util.Log.d("WeightManagementVM", "Saving weight record: $weight for date: $date")
            
            when (val result = petCareRepository.createCareRecord(petId!!, "weight", timestamp, weight.toDouble(), null)) {
                is AppResult.Success -> {
                    android.util.Log.d("WeightManagementVM", "Weight record saved successfully")
                    // 즉시 success 상태 반영
                    _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
                    // 백그라운드에서 데이터 재로드
                    loadDataForDate(date)
                    loadMonthlyAnalysis()
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message ?: "기록에 실패했습니다.") }
                is AppResult.Exception -> _uiState.update { it.copy(isLoading = false, error = "기록 중 오류가 발생했습니다.") }
            }
        }
    }

    fun saveTargetWeight(weight: Float) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            petId ?: run {
                _uiState.update { it.copy(isLoading = false, error = "펫 정보가 없습니다.") }
                return@launch
            }
            
            val settingsRequest = PetCareSettingsRequest(targetWeight = weight.toDouble())
            android.util.Log.d("WeightManagementVM", "Saving target weight: $weight for petId: $petId")
            
            when (val result = petCareRepository.updatePetCareSettings(petId!!, settingsRequest)) {
                is AppResult.Success -> {
                    android.util.Log.d("WeightManagementVM", "Target weight saved successfully")
                    // 목표값 즉시 반영하고 isSaveSuccess 설정
                    _uiState.update { 
                        it.copy(
                            targetWeight = weight,
                            isLoading = false,
                            isSaveSuccess = true
                        ) 
                    }
                    // 백그라운드에서 전체 데이터 재로드 (UI 블로킹 없음)
                    loadDataForDate(uiState.value.selectedDate)
                }
                is AppResult.Error -> {
                    android.util.Log.e("WeightManagementVM", "Failed to save target weight: ${result.message}")
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is AppResult.Exception -> {
                    android.util.Log.e("WeightManagementVM", "Exception saving target weight", result.throwable)
                    _uiState.update { it.copy(isLoading = false, error = "목표 체중 설정에 실패했습니다.") }
                }
            }
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(isSaveSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

} // <-- [수정됨] 클래스를 닫는 중괄호는 파일의 맨 마지막에 위치해야 합니다.
