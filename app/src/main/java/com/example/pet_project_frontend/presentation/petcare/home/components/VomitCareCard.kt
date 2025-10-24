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
 * 구토 케어 카드 컴포넌트
 * Single Responsibility: 구토 관련 케어 기록 카드 UI만 담당
 */
@Composable
fun VomitCareCard(
    latestRecord: String? = null,  // 구토 상세 정보 (예: "노란색")
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
                    painter = painterResource(id = R.drawable.care_vomit_icon), 
                    contentDescription = "구토",
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "구토",
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
            
            // 하단: 구토 기록 표시
            if (latestRecord == null) {
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
                // 구토 상세 정보 표시 (예: "노란색")
                Text(
                    text = latestRecord,
                    fontSize = 20.sp,  // 읽기 좋은 크기
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey800,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 8.dp),
                    maxLines = 1
                )
            }
        }
    }
}