package com.example.pet_project_frontend.presentation.care_record

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
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * 대변 기록 ViewModel
 */
@HiltViewModel
class PoopRecordViewModel @Inject constructor(
    private val petCareRepository: PetCareRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val isSaveSuccess: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun savePoopRecord(quality: String, color: String, selectedDate: String, memo: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // 현재 페트 ID 가져오기
                val petProfile = petRepository.getMyPetProfile()
                when (petProfile) {
                    is AppResult.Success -> {
                        val petId = petProfile.data.id
                        
                        // 대변 기록 생성 - 간단 형식 (문자열로 전송)
                        val data = if (color.isNotEmpty()) {
                            "$quality, $color"
                        } else {
                            quality // color가 비어있으면(즉, "대변 안 봄"이면) shape만 사용
                        }
                        
                        // 선택된 날짜를 timestamp로 변환
                        val timestamp = convertDateStringToTimestamp(selectedDate)
                        
                        val result = petCareRepository.createCareRecord(
                            petId = petId,
                            recordType = "stool",
                            timestamp = timestamp,
                            data = data,
                            memo = memo
                        )
                        when (result) {
                            is AppResult.Success -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    isSaveSuccess = true
                                )
                            }
                            is AppResult.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                            is AppResult.Exception -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "대변 기록 저장에 실패했습니다."
                                )
                            }
                        }
                    }
                    is AppResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "반려동물 정보를 찾을 수 없습니다."
                        )
                    }
                    is AppResult.Exception -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "반려동물 정보를 가져오는데 실패했습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "대변 기록 저장에 실패했습니다."
                )
            }
        }
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSaveSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 날짜 문자열("yyyy년 M월 d일")을 현재 시간의 timestamp(ms)로 변환
     * 선택된 날짜 + 현재 시간(시:분:초)을 조합하여 정확한 시간 저장
     */
    private fun convertDateStringToTimestamp(dateString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
            val selectedDate = format.parse(dateString)
            
            if (selectedDate != null) {
                val calendar = Calendar.getInstance()
                val now = Calendar.getInstance()
                
                calendar.time = selectedDate
                // 선택된 날짜에 현재 시간(시:분:초)를 설정
                calendar.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                calendar.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                calendar.set(Calendar.SECOND, now.get(Calendar.SECOND))
                calendar.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND))
                
                calendar.timeInMillis
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            // 파싱 실패 시 현재 시간 사용
            System.currentTimeMillis()
        }
    }
}