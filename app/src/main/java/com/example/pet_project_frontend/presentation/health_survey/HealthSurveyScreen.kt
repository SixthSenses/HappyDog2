package com.example.pet_project_frontend.presentation.health_survey

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 건강 설문지 메인 화면
 * 소개 -> 질문 -> 결과 화면 간의 네비게이션 관리
 */
@Composable
fun HealthSurveyScreen(
    onBackClick: () -> Unit = {},
    onFinish: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HealthSurveyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when {
        !uiState.isStarted -> {
            // 소개 화면
            HealthSurveyIntroScreen(
                onStartSurvey = {
                    viewModel.startSurvey()
                },
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
        
        !uiState.isCompleted -> {
            // 질문 화면
            HealthSurveyQuestionScreen(
                question = viewModel.getCurrentQuestion(),
                questionNumber = viewModel.getCurrentQuestionNumber(),
                totalQuestions = viewModel.getTotalQuestions(),
                petName = viewModel.getCurrentPetName(), // 현재 반려견 이름 사용
                onAnswerYes = {
                    viewModel.answerYes()
                },
                onAnswerNo = {
                    viewModel.answerNo()
                },
                onBackClick = {
                    viewModel.goToPreviousQuestion()
                },
                modifier = modifier
            )
        }
        
        else -> {
            // 결과 화면
            HealthSurveyResultScreen(
                yesCount = uiState.yesCount,
                petName = viewModel.getCurrentPetName(), // 실제 반려견 이름 전달
                onFinish = {
                    viewModel.resetSurvey()
                    onFinish()
                },
                onRetry = {
                    // 다시하기 - 설문 시작 화면으로 이동
                    viewModel.resetSurvey()
                },
                onBackClick = {
                    // 뒤로가기 - 마지막 질문으로 돌아가기
                    viewModel.goBackFromResult()
                },
                modifier = modifier
            )
        }
    }
}