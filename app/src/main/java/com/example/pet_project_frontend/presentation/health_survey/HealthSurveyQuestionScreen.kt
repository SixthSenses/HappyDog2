package com.example.pet_project_frontend.presentation.health_survey

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors
import androidx.compose.ui.draw.clip
/**
 * 설문지 질문 화면
 * 개별 질문과 예/아니오 답변 버튼
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthSurveyQuestionScreen(
    question: String,
    questionNumber: Int,
    totalQuestions: Int,
    petName: String = "레오", // 반려견 이름 (기본값은 레오)
    questionIconRes: Int = R.drawable.survey_question, // 질문 아이콘
    onAnswerYes: () -> Unit = {},
    onAnswerNo: () -> Unit = {},
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyPageColors.Grey100)
            )
        },
        containerColor = MyPageColors.Grey100
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // 진행률 바
            LinearProgressIndicator(
                progress = { questionNumber.toFloat() / totalQuestions.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(16.dp)),
            trackColor = MyPageColors.Grey200,
            color = MyPageColors.Blue500,
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 상단 타이틀
            Text(
                text = "${petName}의 건강 상태를\n몇가지 질문으로 살펴볼게요",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MyPageColors.Grey700,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(92.dp))
            
            // 중앙 흰색 박스
            Card(
                modifier = Modifier
                    .width(360.dp)
                    .height(260.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 아이콘 (중앙)
                    Image(
                        painter = painterResource(id = questionIconRes),
                        contentDescription = "질문 아이콘",
                        modifier = Modifier.size(30.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // 질문 텍스트 (중앙)
                    Text(
                        text = question,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = MyPageColors.Grey800,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // 답변 버튼들 (가로 배치)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 아니다 버튼 (테두리 없는 일반 버튼)
                        Button(
                            onClick = onAnswerNo,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MyPageColors.Red50
                            )
                        ) {
                            Text(
                                text = "아니다",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MyPageColors.Red700
                            )
                        }
                        
                        // 그렇다 버튼
                        Button(
                            onClick = onAnswerYes,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MyPageColors.Blue50
                            )
                        ) {
                            Text(
                                text = "그렇다",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MyPageColors.Blue500
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}