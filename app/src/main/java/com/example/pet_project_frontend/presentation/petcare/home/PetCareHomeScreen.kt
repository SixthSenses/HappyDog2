package com.example.pet_project_frontend.presentation.petcare.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.presentation.petcare.home.components.*
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
    onPoopClick: () -> Unit = {},
    onVomitClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
                    onMonthChanged = viewModel::changeMonth
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
                    onFeedClick = onFeedClick,
                    onActivityClick = onActivityClick,
                    onWeightClick = onWeightClick,
                    onPoopClick = onPoopClick,
                    onVomitClick = onVomitClick
                )
            }
            
            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}