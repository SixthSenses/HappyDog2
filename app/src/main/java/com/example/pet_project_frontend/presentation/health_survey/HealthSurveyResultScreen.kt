package com.example.pet_project_frontend.presentation.health_survey

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors

/**
 * 설문지 결과 화면
 * 반려견의 건강 상태 결과 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthSurveyResultScreen(
    yesCount: Int,
    petName: String,
    onFinish: () -> Unit = {},
    onRetry: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // yesCount에 따른 상태 결정
    val (statusIcon, statusText, statusDescription, backgroundBrush) = when (yesCount) {
        in 0..1 -> {
            val brush = Brush.verticalGradient(
                colors = listOf(
                    MyPageColors.Blue100,
                    Color.White
                )
            )
            Tuple4(
                R.drawable.mood, // mood.png
                "건강한 상태",
                "전반적으로 정상 범위에 있으며\n건강을 잘 유지하고 있어요.\n\n다만 정기적인 건강검진과 예방접종,\n꾸준한 생활 관리는 꼭 이어가주세요.",
                brush
            )
        }
        in 2..3 -> {
            val brush = Brush.verticalGradient(
                colors = listOf(
                    MyPageColors.Orange100,
                    Color.White
                )
            )
            Tuple4(
                R.drawable.sentiment_neutral, // sentiment_neutral.png
                "주의가 필요한 상태",
                "일부 수치가 경계 수준에 있어 관리가\n필요해요.\n\n큰 문제는 아니지만 생활습관(식사, 산책 등)\n개선이나 정기적인 검진을 권장해요.\n\n문제가 계속된다면 동물병원에서\n전문적인 진료를 받아보세요.",
                brush
            )
        }
        else -> {
            val brush = Brush.verticalGradient(
                colors = listOf(
                    MyPageColors.Red100,
                    Color.White
                )
            )
            Tuple4(
                R.drawable.sick, // sick.png
                "관리가 시급한 상태",
                "건강에 위험 신호가 있어\n전문적인 진료가 필요해요.\n\n빠른 시일 내에 동물병원에서\n전문적인 진료를 받아보세요.",
                brush
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // 설문 아이콘
            Image(
                painter = painterResource(id = R.drawable.health_survey_icon),
                contentDescription = "설문 아이콘",
                modifier = Modifier.size(width = 53.dp, height = 65.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 타이틀 텍스트
            Text(
                text = "${petName}의 건강 상태는",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MyPageColors.Grey700,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 상태 박스 (둥근 원통형)
            Box(
                modifier = Modifier
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(50.dp) // 완전히 둥근 모서리
                    )
                    .height(60.dp)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 상태 아이콘
                    Image(
                        painter = painterResource(id = statusIcon),
                        contentDescription = "상태 아이콘",
                        modifier = Modifier.size(28.dp)
                    )
                    
                    // 상태 텍스트
                    Text(
                        text = statusText,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = MyPageColors.Grey800
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 상태 세부 설명
            Text(
                text = statusDescription,
                fontSize = 18.sp,
                color = MyPageColors.Grey800,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 확인 버튼
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MyPageColors.Blue500
                )
            ) {
                Text(
                    text = "확인",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 다시하기 텍스트 버튼
            TextButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "다시하기",
                    fontSize = 14.sp,
                    color = MyPageColors.Grey600
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Tuple4 data class 정의
private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)