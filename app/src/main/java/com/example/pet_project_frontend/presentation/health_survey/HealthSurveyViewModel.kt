package com.example.pet_project_frontend.presentation.health_survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 건강 설문지 ViewModel
 * 설문지 진행 상태 및 답변 관리
 */
@HiltViewModel
class HealthSurveyViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {
    
    // 설문지 질문들
    private val questions = listOf(
        "최근 식욕이 줄었거나\n체중 변화가 있었다",
        "구토나 설사가 반복되거나\n배변 상태가 평소와 다르다",
        "기침, 재채기 또는\n호흡 곤란 같은 증상이 있다",
        "걸을 때 다리를 절거나\n계단을 오르내리기 힘들어한다",
        "피부를 자주 긁거나\n탈모·발진 같은 증상이 있다",
        "입 냄새가 심하거나 치석, 잇몸\n출혈 같은 구강 문제가 있다",
        "예방접종과 구충(내·외부 기생충\n예방)을 정기적으로 한다"
    )
    
    private val _uiState = MutableStateFlow(HealthSurveyUiState())
    val uiState: StateFlow<HealthSurveyUiState> = _uiState.asStateFlow()
    
    private val _petName = MutableStateFlow("반려견")
    val petName: StateFlow<String> = _petName.asStateFlow()
    
    init {
        loadPetName()
    }
    
    fun getCurrentQuestion(): String {
        val currentIndex = _uiState.value.currentQuestionIndex
        return if (currentIndex < questions.size) {
            questions[currentIndex]
        } else {
            ""
        }
    }
    
    fun getTotalQuestions(): Int = questions.size
    
    fun getCurrentQuestionNumber(): Int = _uiState.value.currentQuestionIndex + 1
    
    fun answerYes() {
        val currentState = _uiState.value
        val newYesCount = currentState.yesCount + 1
        val newQuestionIndex = currentState.currentQuestionIndex + 1
        
        _uiState.value = currentState.copy(
            yesCount = newYesCount,
            currentQuestionIndex = newQuestionIndex,
            isCompleted = newQuestionIndex >= questions.size
        )
    }
    
    fun answerNo() {
        val currentState = _uiState.value
        val newQuestionIndex = currentState.currentQuestionIndex + 1
        
        _uiState.value = currentState.copy(
            currentQuestionIndex = newQuestionIndex,
            isCompleted = newQuestionIndex >= questions.size
        )
    }
    
    fun resetSurvey() {
        _uiState.value = HealthSurveyUiState()
    }
    
    fun startSurvey() {
        _uiState.value = _uiState.value.copy(isStarted = true)
    }
    
    /**
     * 이전 질문으로 돌아가기
     */
    fun goToPreviousQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex > 0) {
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex - 1,
                yesCount = if (currentState.currentQuestionIndex > 0) maxOf(0, currentState.yesCount - 1) else currentState.yesCount,
                isCompleted = false
            )
        } else {
            // 첫 번째 질문에서 뒤로가기를 누르면 설문 시작 전으로 돌아감
            _uiState.value = HealthSurveyUiState()
        }
    }
    
    /**
     * 반려견 이름 로드
     */
    private fun loadPetName() {
        viewModelScope.launch {
            when (val result = petRepository.getPetProfileForPetCare()) {
                is AppResult.Success -> {
                    _petName.value = result.data.name
                }
                else -> {
                    _petName.value = "반려견"
                }
            }
        }
    }
    
    /**
     * 현재 반려견 이름 가져오기
     */
    fun getCurrentPetName(): String {
        return _petName.value
    }
    
    /**
     * 결과 화면에서 뒤로가기 - 마지막 질문으로 돌아가기
     */
    fun goBackFromResult() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            currentQuestionIndex = questions.size - 1, // 마지막 질문으로
            isCompleted = false
        )
    }
}

/**
 * 건강 설문지 UI 상태
 */
data class HealthSurveyUiState(
    val isStarted: Boolean = false,
    val currentQuestionIndex: Int = 0,
    val yesCount: Int = 0,
    val isCompleted: Boolean = false
)