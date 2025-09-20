package com.example.pet_project_frontend.presentation.petcare.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 기능 카드들을 조합하는 컨테이너 컴포넌트
 * Single Responsibility: 개별 기능 카드들의 레이아웃 배치만 담당
 */
@Composable
fun FeatureCards(
    onHealthSurveyClick: () -> Unit = {},
    onBreedGuideClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 건강 설문지 카드
        HealthSurveyCard(
            onClick = onHealthSurveyClick,
            modifier = Modifier.weight(1f)
        )
        
        // 견종 가이드북 카드
        BreedGuideCard(
            onClick = onBreedGuideClick,
            modifier = Modifier.weight(1f)
        )
    }
}