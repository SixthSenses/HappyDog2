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
 * 대변 관리 화면 ViewModel
 */
@HiltViewModel
class PoopManagementViewModel @Inject constructor(
    private val petCareRepository: PetCareRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    data class PoopRecord(
        val logId: String,
        val data: String,
        val timestamp: Long,
        val formattedTime: String
    )

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val records: List<PoopRecord> = emptyList(),
        val selectedDate: LocalDate = LocalDate.now()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadRecords()
    }

    fun loadRecords(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedDate = date)
            
            try {
                val petProfile = petRepository.getMyPetProfile()
                when (petProfile) {
                    is AppResult.Success -> {
                        val petId = petProfile.data.id
                        
                        // 오늘 날짜의 기록만 가져오기
                        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        
                        val result = petCareRepository.getDailyRecords(petId, dateString)
                        val records = if (result is AppResult.Success) {
                            result.data.records
                                .filter { it.recordType == "stool" }
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
                                    
                                    // 디버깅용 로그 (실제 배포시 제거)
                                    println("DEBUG - Record: logId=${record.logId}, timestamp=${record.timestamp}, formattedTime=$formattedTime, data=$dataString")
                                    
                                    PoopRecord(
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
                    error = "기록을 불러오는 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun deleteRecord(logId: String) {
        viewModelScope.launch {
            try {
                val petProfile = petRepository.getMyPetProfile()
                when (petProfile) {
                    is AppResult.Success -> {
                        val petId = petProfile.data.id
                        
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
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            error = "반려동물 정보를 찾을 수 없습니다."
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