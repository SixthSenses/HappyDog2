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
    // 사료 관련
    currentFeedCount: Int = 0,
    targetFeedCount: Int? = null,
    onFeedDetailClick: () -> Unit = {},
    onFeedPlusClick: () -> Unit = {},
    onFeedMinusClick: () -> Unit = {},
    // 활동 관련
    currentActivityMinutes: Int = 0,
    targetActivityMinutes: Int? = null,
    activitySessionMinutes: Int = 30, // 1회 활동 시간
    onActivityDetailClick: () -> Unit = {},
    onActivityPlusClick: () -> Unit = {},
    onActivityMinusClick: () -> Unit = {},
    // 몸무게 관련
    currentWeight: Float? = null,
    currentWeightText: String? = null,  // 몸무게 텍스트 (예: "50kg")
    targetWeight: Float? = null,
    onWeightClick: () -> Unit = {},
    // 대변 관련
    poopRecords: List<String> = emptyList(),
    latestPoopRecord: String? = null,  // 대변 상세 정보 (예: "초록색, 점액 섞임")
    onPoopClick: () -> Unit = {},
    // 구토 관련
    vomitRecords: List<String> = emptyList(),
    latestVomitRecord: String? = null,  // 구토 상세 정보 (예: "노란색")
    onVomitClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    enableButtons: Boolean = true // 버튼 활성화/비활성화 제어
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
                currentFeedCount = currentFeedCount,
                targetFeedCount = targetFeedCount,
                onDetailClick = onFeedDetailClick,
                onPlusClick = onFeedPlusClick,
                onMinusClick = onFeedMinusClick,
                modifier = Modifier.weight(1f),
                enableButtons = enableButtons
            )
            
            ActivityCareCard(
                currentActivityMinutes = currentActivityMinutes,
                targetActivityMinutes = targetActivityMinutes,
                activitySessionMinutes = activitySessionMinutes,
                onDetailClick = onActivityDetailClick,
                onPlusClick = onActivityPlusClick,
                onMinusClick = onActivityMinusClick,
                modifier = Modifier.weight(1f),
                enableButtons = enableButtons
            )
        }
        
        // 두 번째 행: 몸무게, 대변
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeightCareCard(
                currentWeight = currentWeight,
                currentWeightText = currentWeightText,  // 몸무게 텍스트 전달
                targetWeight = targetWeight,
                onDetailClick = onWeightClick,
                modifier = Modifier.weight(1f)
            )
            
            PoopCareCard(
                records = poopRecords,
                latestRecord = latestPoopRecord,  // 상세 정보 전달
                onDetailClick = onPoopClick,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 세 번째 행: 구토 (하나만)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            VomitCareCard(
                records = vomitRecords,
                latestRecord = latestVomitRecord,  // 상세 정보 전달
                onDetailClick = onVomitClick,
                modifier = Modifier.weight(0.48f) // 다른 카드들과 같은 크기
            )
            
            // 빈 공간을 위한 Spacer
            Spacer(modifier = Modifier.weight(0.52f))
        }
    }
}