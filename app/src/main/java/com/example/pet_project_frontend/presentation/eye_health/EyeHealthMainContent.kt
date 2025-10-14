package com.example.pet_project_frontend.presentation.eye_health

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyeHealthMainContent(
    onBackClick: () -> Unit,
    onEyeCheckClick: () -> Unit,
    onCheckHistoryClick: () -> Unit,
    showBottomSheet: Boolean,
    onDismissBottomSheet: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "사진 한 장으로,\n우리 아이 눈 건강을 확인하세요",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MyPageColors.Grey900,
                textAlign = TextAlign.Start,
                lineHeight = 32.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.eye_disease_image),
                contentDescription = "안구 질환 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp) // 이미지 높이 지정
            )

            Spacer(modifier = Modifier.height(32.dp))

            InfoRow(
                iconRes = R.drawable.eye_guide_1,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 14.sp, color = MyPageColors.Grey600, fontWeight = FontWeight.Normal)) {
                        append("안검내반증, 결막염, 백내장, 각막궤양\n")
                    }
                    withStyle(style = SpanStyle(fontSize = 16.sp, color = MyPageColors.Grey700, fontWeight = FontWeight.Bold)) {
                        append("4가지 안질환을 예측해요")
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            InfoRow(
                iconRes = R.drawable.eye_guide_2,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 14.sp, color = MyPageColors.Grey600, fontWeight = FontWeight.Normal)) {
                        append("플래시로 더 정확하게\n")
                    }
                    withStyle(style = SpanStyle(fontSize = 16.sp, color = MyPageColors.Grey700,fontWeight = FontWeight.Bold)) {
                        append("정확도는 환경에 따라 달라질 수 있어요")
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            InfoRow(
                iconRes = R.drawable.eye_guide_3,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 14.sp, color = MyPageColors.Grey600, fontWeight = FontWeight.Normal)) {
                        append("AI 진단은 참고용으로만\n")
                    }
                    withStyle(style = SpanStyle(fontSize = 16.sp, color = MyPageColors.Grey700, fontWeight = FontWeight.Bold)) {
                        append("정확한 진단은 전문 진료로 받으세요")
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onEyeCheckClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MyPageColors.Blue500)
            ) {
                Text("안구 검사하기", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onCheckHistoryClick) {
                Text(
                    text = "검사 기록 확인하기",
                    color = MyPageColors.Grey600,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissBottomSheet,
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp) // 하단 여백 추가
            ) {
                Text(
                    text = "어떤 방식으로 검사할까요?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey900
                )
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BottomSheetItem(
                        iconRes = R.drawable.camera,
                        text = "사진 촬영하기",
                        onClick = onCameraClick
                    )
                    BottomSheetItem(
                        iconRes = R.drawable.gallary,
                        text = "갤러리에서 불러오기",
                        onClick = onGalleryClick
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
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

@Composable
private fun BottomSheetItem(
    iconRes: Int,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MyPageColors.Grey800
        )
    }
}