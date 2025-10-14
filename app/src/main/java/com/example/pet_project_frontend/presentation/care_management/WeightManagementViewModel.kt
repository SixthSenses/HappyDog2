package com.example.pet_project_frontend.presentation.care_management

import android.util.Log
// import androidx.compose.animation.core.copy // 1. 불필요한 import 삭제
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.PetCareSettingsRequest
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async // 'async'를 직접 import 하도록 명시
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 몸무게 기록 ViewModel
 * - 목표 체중 설정 (Settings API)
 * - 특정 날짜 체중 기록 (Records API)
 */
@HiltViewModel
class WeightManagementViewModel @Inject constructor(
    private val petCareRepository: PetCareRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val isSaveSuccess: Boolean = false,
        val error: String? = null,
        val targetWeight: Float? = null,
        val todayWeight: Float? = null,
        val selectedDate: LocalDate = LocalDate.now(),
        val selectedDateWeight: Float? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var petId: String? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val petProfileResult = petRepository.getMyPetProfile()
            if (petProfileResult !is AppResult.Success) {
                _uiState.update { it.copy(isLoading = false, error = "반려동물 정보를 찾을 수 없습니다.") }
                return@launch
            }
            petId = petProfileResult.data.id

            // 2. 'kotlinx.coroutines.async'를 'async'로 수정
            val targetWeightDeferred = async { getTargetWeight(petId!!) }
            val todayWeightDeferred = async { getWeightForDate(petId!!, LocalDate.now()) }

            val results = awaitAll(targetWeightDeferred, todayWeightDeferred)

            val targetWeight = results[0] as? Float
            val todayWeight = results[1] as? Float

            _uiState.update {
                it.copy(
                    isLoading = false,
                    targetWeight = targetWeight,
                    todayWeight = todayWeight,
                    selectedDateWeight = todayWeight
                )
            }
            Log.d("WeightVM", "Initial Load Complete: Target=$targetWeight, Today=$todayWeight")
        }
    }

    fun onDateSelected(date: LocalDate) {
        viewModelScope.launch {
            petId ?: return@launch // petId가 초기화되지 않은 경우를 대비한 방어 코드
            _uiState.update { it.copy(isLoading = true, selectedDate = date) }
            val weight = getWeightForDate(petId!!, date)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedDateWeight = weight
                )
            }
            Log.d("WeightVM", "Date Selected: $date, Weight=$weight")
        }
    }

    private suspend fun getTargetWeight(petId: String): Float? {
        return when (val result = petCareRepository.getPetCareSettings(petId)) {
            is AppResult.Success -> result.data.targetWeight?.toFloat()
            else -> null
        }
    }

    private suspend fun getWeightForDate(petId: String, date: LocalDate): Float? {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return when (val result = petCareRepository.getDailyRecords(petId, dateString)) {
            is AppResult.Success -> {
                result.data.records
                    .filter { it.recordType == "weight" }
                    .maxByOrNull { it.timestamp }
                    ?.let { (it.data as? Number)?.toFloat() }
            }
            else -> null
        }
    }

    fun saveWeightRecord(weight: Float, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            petId ?: run {
                _uiState.update { it.copy(isLoading = false, error = "펫 정보가 없습니다.") }
                return@launch
            }

            val now = LocalTime.now()
            val timestamp = date.atTime(now).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            when (val result = petCareRepository.createCareRecord(
                petId = petId!!,
                recordType = "weight",
                timestamp = timestamp,
                data = weight.toDouble(),
                memo = null
            )) {
                is AppResult.Success -> {
                    // 저장 성공 후, 최신 데이터를 다시 불러와 UI를 갱신합니다.
                    loadInitialData()
                    _uiState.update { it.copy(isSaveSuccess = true) }
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message ?: "기록에 실패했습니다.") }
                is AppResult.Exception -> _uiState.update { it.copy(isLoading = false, error = "기록 중 오류가 발생했습니다.") }
            }
        }
    }

    // 함수의 이름을 'saveTargetWeight'로 변경하여 역할의 명확성을 높입니다.
    fun saveTargetWeight(weight: Float) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            petId ?: run {
                _uiState.update { it.copy(isLoading = false, error = "펫 정보가 없습니다.") }
                return@launch
            }

            val settingsRequest = PetCareSettingsRequest(targetWeight = weight.toDouble())

            when (val result = petCareRepository.updatePetCareSettings(petId!!, settingsRequest)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSaveSuccess = true,
                            targetWeight = weight
                        )
                    }
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is AppResult.Exception -> _uiState.update { it.copy(isLoading = false, error = "목표 체중 설정에 실패했습니다.") }
            }
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(isSaveSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
