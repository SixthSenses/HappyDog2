package com.example.pet_project_frontend.presentation.petcare.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 케어 항목 그리드 컨테이너 컴포넌트
 * Single Responsibility: 개별 케어 카드들의 그리드 레이아웃 배치만 담당
 */
@Composable
fun CareItemsGrid(
    onFeedClick: () -> Unit = {},
    onActivityClick: () -> Unit = {},
    onWeightClick: () -> Unit = {},
    onPoopClick: () -> Unit = {},
    onVomitClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 첫 번째 행: 사료, 활동
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeedCareCard(
                onClick = onFeedClick,
                modifier = Modifier.weight(1f)
            )
            
            ActivityCareCard(
                onClick = onActivityClick,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 두 번째 행: 몸무게, 대변
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeightCareCard(
                onClick = onWeightClick,
                modifier = Modifier.weight(1f)
            )
            
            PoopCareCard(
                onClick = onPoopClick,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 세 번째 행: 구토 (하나만)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            VomitCareCard(
                onClick = onVomitClick,
                modifier = Modifier.weight(0.48f) // 다른 카드들과 같은 크기
            )
            
            // 빈 공간을 위한 Spacer
            Spacer(modifier = Modifier.weight(0.52f))
        }
    }
}