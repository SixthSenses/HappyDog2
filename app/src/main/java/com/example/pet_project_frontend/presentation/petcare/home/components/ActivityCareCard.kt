package com.example.pet_project_frontend.presentation.petcare.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors

/**
 * 활동 케어 카드 컴포넌트
 * Single Responsibility: 활동 관련 케어 기록 카드 UI만 담당
 */
@Composable
fun ActivityCareCard(
    currentMinutes: Int = 45,
    targetMinutes: Int = 60,
    onDetailClick: () -> Unit = {},
    onPlusClick: () -> Unit = {},
    onMinusClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(0.9f),
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
                    painter = painterResource(id = R.drawable.care_activity_icon),
                    contentDescription = "활동",
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "활동",
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
            
            // 중앙 왼쪽 정렬: 활동 정보와 프로그래스바
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .padding(end = 16.dp)
            ) {
                // 활동 시간을 한 줄로 표시
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${currentMinutes}분",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MyPageColors.Green500
                    )
                    Text(
                        text = "/${targetMinutes}분",
                        fontSize = 15.sp,
                        color = MyPageColors.Grey500
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 프로그래스바와 +/- 버튼을 한 줄로 표시
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(35.dp)
                ) {
                    // 프로그래스바
                    CircularProgressIndicator(
                        progress = { (currentMinutes.toFloat() / targetMinutes.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.size(72.dp),
                        color = MyPageColors.Green,
                        strokeWidth = 12.dp,
                        strokeCap = StrokeCap.Round
                    )
                    
                    // +/- 버튼 (세로 배치, 총 높이 72dp)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // + 버튼
                        Image(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "추가",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onPlusClick() }
                        )
                        
                        // - 버튼
                        Image(
                            painter = painterResource(id = R.drawable.minus),
                            contentDescription = "제거",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onMinusClick() }
                        )
                    }
                }
            }

        }
    }
}