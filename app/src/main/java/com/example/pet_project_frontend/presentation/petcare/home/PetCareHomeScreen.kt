package com.example.pet_project_frontend.presentation.petcare.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.presentation.petcare.home.components.*
import com.example.pet_project_frontend.core.theme.MyPageColors
import java.time.format.DateTimeFormatter
import java.time.LocalDate
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetCareHomeScreen(
    viewModel: PetCareHomeViewModel = hiltViewModel(),
    onNotificationClick: () -> Unit = {},
    onHealthSurveyClick: () -> Unit = {},
    onBreedGuideClick: () -> Unit = {},
    onEyeCheckClick: () -> Unit = {},
    onFeedClick: () -> Unit = {},
    onActivityClick: () -> Unit = {},
    onWeightClick: () -> Unit = {},
    onPoopClick: (String) -> Unit = {}, // 날짜 파라미터 추가
    onVomitClick: (String) -> Unit = {}, // 날짜 파라미터 추가
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // 화면 다시 돌아오면 업데이트(관리 페이지에서의 사료, 활동 변경 값)
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            // 화면이 다시 활성화될 때 (예: 다른 화면에서 돌아왔을 때)
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // ViewModel에게 데이터를 새로고침하라고 알립니다.
                viewModel.refresh() // 기존의 refresh 함수를 그대로 사용합니다.
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // 화면이 사라질 때 observer를 정리합니다.
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // 에러 메시지 스낵바 처리
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // TODO: 스낵바나 토스트로 에러 메시지 표시
            viewModel.clearErrorMessage()
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MyPageColors.White // Color.kt에서 정의된 White 사용
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MyPageColors.White) // Color.kt에서 정의된 White 사용
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                // 상단 프로필 헤더
                ProfileHeader(
                    petName = uiState.petName,
                    petImageUrl = uiState.petImageUrl,
                    onNotificationClick = onNotificationClick
                )
            }
            
            item {
                // 기능 카드들 (건강 설문지, 견종 가이드북)
                FeatureCards(
                    onHealthSurveyClick = onHealthSurveyClick,
                    onBreedGuideClick = onBreedGuideClick
                )
            }
            
            item {
                // 캘린더
                CalendarComponent(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = viewModel::selectDate,
                    onMonthChanged = { date -> 
                        // LocalDate를 Int로 변환하여 changeMonth 호출
                        val currentDate = uiState.selectedDate
                        val monthDiff = (date.year - currentDate.year) * 12 + (date.monthValue - currentDate.monthValue)
                        viewModel.changeMonth(monthDiff)
                    }
                )
            }
            
            item {
                // AI 안구 검사 카드
                EyeHealthCard(
                    onEyeCheckClick = onEyeCheckClick
                )
            }
            
            item {
                // 케어 항목 그리드
                CareItemsGrid(
                    // 사료 관련 데이터 (목표값과 현재값 표시하고 버튼 활성화)
                    currentFeedCount = uiState.currentFeedCount,
                    targetFeedCount = uiState.targetFeedCount,
                    onFeedDetailClick = onFeedClick,
                    onFeedPlusClick = { viewModel.addFeedRecord() }, // 실제 API 호출
                    onFeedMinusClick = { viewModel.removeFeedRecord() }, // 실제 API 호출
                    // 활동 관련 데이터 (목표값과 현재값 표시하고 버튼 활성화)
                    currentActivityMinutes = uiState.currentActivityMinutes,
                    targetActivityMinutes = uiState.targetDailyActivityMinutes,  // 1일 총 목표 활동 시간 (서버 계산)
                    activitySessionMinutes = uiState.activitySessionMinutes ?: 30,  // 1회 활동 시간 (버튼 증감값)
                    onActivityDetailClick = onActivityClick,
                    onActivityPlusClick = { viewModel.addActivityRecord(uiState.activitySessionMinutes ?: 30) }, // 1회 활동 시간만큼 증가
                    onActivityMinusClick = { viewModel.removeActivityRecord(uiState.activitySessionMinutes ?: 30) }, // 1회 활동 시간만큼 감소
                    // 몸무게 관련 데이터 (목표값과 현재값 표시)
                    currentWeight = uiState.currentWeight,
                    currentWeightText = uiState.weightText,  // 몸무게 텍스트 (예: "50kg")
                    targetWeight = uiState.targetWeight,
                    onWeightClick = onWeightClick,
                    // 대변 관련 데이터 (기록없음으로 표시)
                    poopRecords = uiState.poopRecords,
                    latestPoopRecord = uiState.todayLatestPoopRecord,  // 상세 정보 (예: "초록색, 점액 섞임")
                    onPoopClick = { onPoopClick(uiState.selectedDate.toString()) }, // 선택된 날짜 전달
                    // 구토 관련 데이터 (기록없음으로 표시)
                    vomitRecords = uiState.vomitRecords,
                    latestVomitRecord = uiState.todayLatestVomitRecord,  // 상세 정보 (예: "노란색")
                    onVomitClick = { onVomitClick(uiState.selectedDate.toString()) }, // 선택된 날짜 전달
                    enableButtons = true // 홈 화면에서도 버튼 활성화
                )
            }
            
            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}