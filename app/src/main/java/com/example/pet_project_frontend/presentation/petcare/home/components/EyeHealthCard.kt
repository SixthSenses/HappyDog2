package com.example.pet_project_frontend.presentation.petcare.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors

@Composable
fun EyeHealthCard(
    onEyeCheckClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEyeCheckClick() }
    ) {
        // 배경 이미지 (별도의 배경 이미지 리소스)
        Image(
            painter = painterResource(id = R.drawable.eye_health_background), // 배경 이미지
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 기존 콘텐츠 레이아웃
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 왼쪽: 텍스트 내용 (가운데 정렬)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp)
            ) {
                Text(
                    text = "한눈에 보는 눈 건강",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MyPageColors.GreyOpacity600.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "AI 안구 검사",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey900
                )
            }
            
            // 오른쪽: 아이콘 이미지 (오른쪽으로 이동, 크기 증가, 아래쪽 정렬)
            Image(
                painter = painterResource(id = R.drawable.eye_check_icon),
                contentDescription = "AI 안구 검사",
                modifier = Modifier
                    .size(250.dp) // 크기 더 증가 (220dp -> 250dp)
                    .align(Alignment.BottomEnd) // 오른쪽 아래 정렬
                    .offset(x = 20.dp, y = 10.dp) // 오른쪽으로 더 이동, 아래로 살짝 이동
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}