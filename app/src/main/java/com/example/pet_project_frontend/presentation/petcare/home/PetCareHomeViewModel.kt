package com.example.pet_project_frontend.presentation.petcare.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PetCareHomeUiState(
    val isLoading: Boolean = false,
    val petName: String = "반려견 이름",
    val petImageUrl: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: LocalDate = LocalDate.now().withDayOfMonth(1),
    val hasNotification: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PetCareHomeViewModel @Inject constructor(
    // TODO: Repository 의존성 추가
    // private val petRepository: PetRepository,
    // private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetCareHomeUiState())
    val uiState: StateFlow<PetCareHomeUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * 초기 데이터 로딩
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // TODO: 실제 데이터 로딩 구현
                // val petInfo = petRepository.getCurrentPet()
                // val notifications = notificationRepository.getUnreadCount()
                
                // 임시 데이터
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    petName = "멍멍이", // TODO: 실제 펫 이름으로 교체
                    petImageUrl = null, // TODO: 실제 펫 이미지 URL로 교체
                    hasNotification = true // TODO: 실제 알림 상태로 교체
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "데이터를 불러오는 중 오류가 발생했습니다."
                )
            }
        }
    }

    /**
     * 날짜 선택
     */
    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        
        // TODO: 선택된 날짜의 케어 데이터 로딩
        loadCareDataForDate(date)
    }

    /**
     * 월 변경
     */
    fun changeMonth(month: LocalDate) {
        _uiState.value = _uiState.value.copy(currentMonth = month)
        
        // TODO: 해당 월의 케어 데이터 로딩
        loadCareDataForMonth(month)
    }

    /**
     * 특정 날짜의 케어 데이터 로딩
     */
    private fun loadCareDataForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                // TODO: 특정 날짜의 케어 기록 조회
                // val careRecords = petCareRepository.getCareRecordsByDate(date)
                
                // 로직 구현 예정
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "케어 데이터를 불러오는 중 오류가 발생했습니다."
                )
            }
        }
    }

    /**
     * 특정 월의 케어 데이터 로딩
     */
    private fun loadCareDataForMonth(month: LocalDate) {
        viewModelScope.launch {
            try {
                // TODO: 특정 월의 케어 기록 조회
                // val monthlyData = petCareRepository.getCareRecordsByMonth(month)
                
                // 로직 구현 예정
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "월별 데이터를 불러오는 중 오류가 발생했습니다."
                )
            }
        }
    }

    /**
     * 에러 메시지 클리어
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * 데이터 새로고침
     */
    fun refresh() {
        loadInitialData()
    }
}