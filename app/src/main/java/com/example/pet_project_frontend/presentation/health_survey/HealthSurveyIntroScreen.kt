package com.example.pet_project_frontend.presentation.health_survey

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors

/**
 * 건강 설문지 소개 화면
 * 설문지 시작 전 안내 및 시작 버튼
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthSurveyIntroScreen(
    onStartSurvey: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "건강 설문지",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MyPageColors.Blue500,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "우리 아이의 건강 상태를\n몇가지 질문을 통해 알아보세요",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MyPageColors.Grey900,
                lineHeight = 32.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Image(
                painter = painterResource(id = R.drawable.survey_main),
                contentDescription = "견종 가이드 메인",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp) // 이미지 높이 지정
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 안내 정보들 (사용자가 직접 수정)
            InfoRow(
                iconRes = R.drawable.survey_guide1, // 임시 아이콘 (사용자가 변경)
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 16.sp, color = MyPageColors.Grey700, fontWeight = FontWeight.Bold)) {
                        append("총 7개의 질문으로 구성되어 있어요")
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            InfoRow(
                iconRes = R.drawable.survey_guide2, // 임시 아이콘 (사용자가 변경)
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 16.sp, color = MyPageColors.Grey700, fontWeight = FontWeight.Bold)) {
                        append("설문에는 3분 정도 걸려요")
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            InfoRow(
                iconRes = R.drawable.survey_guide3, // 임시 아이콘 (사용자가 변경)
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 16.sp, color = MyPageColors.Grey700, fontWeight = FontWeight.Bold)) {
                        append("이 설문은 기본적인 건강 체크용이며\n정밀 진단은 전문의를 통해 받으세요")
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStartSurvey,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MyPageColors.Blue500)
            ) {
                Text("건강 설문지 작성하기", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(iconRes: Int, text: AnnotatedString) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text)
    }
}