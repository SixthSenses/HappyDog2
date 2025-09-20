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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 왼쪽: 텍스트 내용 (기존과 동일)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "한눈에 보는 눈 건강",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI 안구 검사",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // "탭" 표시를 위한 작은 칩
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "TAP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // 오른쪽: 아이콘 이미지 (기존과 동일한 위치)
            Image(
                painter = painterResource(id = R.drawable.eye_check_icon), // 기존 아이콘
                contentDescription = "AI 안구 검사",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}