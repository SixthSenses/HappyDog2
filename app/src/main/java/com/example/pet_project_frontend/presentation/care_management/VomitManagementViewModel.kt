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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 구토 관리 화면 ViewModel
 */
@HiltViewModel
class VomitManagementViewModel @Inject constructor(
    private val petCareRepository: PetCareRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    data class VomitRecord(
        val logId: String,
        val data: String,
        val timestamp: Long,
        val formattedTime: String
    )

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val records: List<VomitRecord> = emptyList(),
        val selectedDate: LocalDate = LocalDate.now()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    // petId 캐싱 - N+1 query 문제 해결
    private var cachedPetId: String? = null

    init {
        loadRecords()
    }
    
    private suspend fun ensurePetIdLoaded(): String? {
        // 이미 캐시되어 있으면 API 호출 없이 반환
        if (cachedPetId != null) {
            android.util.Log.d("VomitManagementVM", "✅ Using cached Pet ID: ${cachedPetId}")
            return cachedPetId
        }
        
        // 캐시가 없는 경우에만 API 호출
        return try {
            when (val petResult = petRepository.getMyPetProfile()) {
                is AppResult.Success -> {
                    cachedPetId = petResult.data.id
                    android.util.Log.d("VomitManagementVM", "✅ Pet ID loaded from API: ${cachedPetId}")
                    cachedPetId
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = "반려동물 정보를 찾을 수 없습니다.")
                    null
                }
                is AppResult.Exception -> {
                    _uiState.value = _uiState.value.copy(error = "반려동물 정보를 불러오는 중 오류가 발생했습니다.")
                    null
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = "반려동물 정보를 불러오는 중 오류가 발생했습니다.")
            null
        }
    }

    fun loadRecords(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedDate = date)
            
            try {
                // 캐시된 petId 사용
                val petId = ensurePetIdLoaded() ?: return@launch
                        
                        // 오늘 날짜의 기록만 가져오기
                        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        
                        val result = petCareRepository.getDailyRecords(petId, dateString)
                        val records = if (result is AppResult.Success) {
                            result.data.records
                                .filter { it.recordType == "vomit" }
                                .map { record ->
                                    val dataString = when (val data = record.data) {
                                        is String -> data
                                        is Map<*, *> -> data.toString()
                                        else -> "기록됨"
                                    }
                                    
                                    val instant = Instant.ofEpochMilli(record.timestamp)
                                    val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
                                    val formattedTime = dateTime.format(
                                        DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
                                    )

                                    VomitRecord(
                                        logId = record.logId,
                                        data = dataString,
                                        timestamp = record.timestamp,
                                        formattedTime = formattedTime
                                    )
                                }
                                .sortedByDescending { it.timestamp }
                        } else {
                            emptyList()
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            records = records
                        )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "기록을 불러오는 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun deleteRecord(logId: String) {
        viewModelScope.launch {
            try {
                // 캐시된 petId 사용
                val petId = ensurePetIdLoaded() ?: return@launch
                        
                        when (petCareRepository.deleteCareRecord(petId, logId)) {
                            is AppResult.Success -> {
                                _uiState.value = _uiState.value.copy(
                                    records = _uiState.value.records.filter { it.logId != logId }
                                )
                            }
                            is AppResult.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    error = "기록 삭제에 실패했습니다."
                                )
                            }
                            is AppResult.Exception -> {
                                _uiState.value = _uiState.value.copy(
                                    error = "기록 삭제 중 오류가 발생했습니다."
                                )
                            }
                        }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "기록 삭제 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}