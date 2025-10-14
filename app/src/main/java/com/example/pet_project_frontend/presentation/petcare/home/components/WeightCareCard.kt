package com.example.pet_project_frontend.presentation.petcare.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors

/**
 * 몸무게 케어 카드 컴포넌트
 * Single Responsibility: 몸무게 관련 케어 기록 카드 UI만 담당
 */
@Composable
fun WeightCareCard(
    currentWeight: Float? = null,
    currentWeightText: String? = null,  // 몸무게 텍스트 (예: "50kg")
    targetWeight: Float? = null,
    onDetailClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1.2f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MyPageColors.Grey100
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 상단 왼쪽: 아이콘과 텍스트
            Row(
                modifier = Modifier.align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.care_weight_icon),
                    contentDescription = "몸무게",
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "몸무게",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // 상단 오른쪽: 화살표 버튼
            Image(
                painter = painterResource(id = R.drawable.chevron_right),
                contentDescription = "상세보기",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(10.dp)
                    .clickable { onDetailClick() }
            )
            
            // 하단: 오늘 기록된 몸무게 정보 표시
            if (currentWeightText == null) {
                // 오늘 몸무게 기록이 없는 경우
                Text(
                    text = "기록없음",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey500,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 8.dp)
                )
            } else {
                // 오늘 기록된 몸무게 텍스트 표시 (예: "50kg")
                Text(
                    text = currentWeightText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey800,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}