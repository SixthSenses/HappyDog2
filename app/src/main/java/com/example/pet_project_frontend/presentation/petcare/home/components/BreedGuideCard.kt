package com.example.pet_project_frontend.presentation.petcare.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors

/**
 * 견종 가이드북 카드 컴포넌트
 * Single Responsibility: 견종 가이드북 카드 UI만 담당
 */
@Composable
fun BreedGuideCard(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(0.9f) // 사료 박스와 동일한 비율 (세로로 살짝 긴 박스)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MyPageColors.Yellow100
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 왼쪽 위: 제목들
            Column(
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "견종",
                    fontSize = 14.sp, // 크기 줄임 (16sp -> 14sp)
                    fontWeight = FontWeight.ExtraBold, // 두께 굵게 (Bold -> ExtraBold)
                    color = MyPageColors.Orange800
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "가이드북",
                    fontSize = 14.sp, // 크기 줄임 (16sp -> 14sp)
                    fontWeight = FontWeight.ExtraBold, // 두께 굵게 (Bold -> ExtraBold)
                    color = MyPageColors.Orange800
                )
            }
            
            // 오른쪽 아래: 이미지
            Image(
                painter = painterResource(id = R.drawable.breed_guide_icon),
                contentDescription = "견종 가이드북",
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.BottomEnd),
                contentScale = ContentScale.Fit
            )
        }
    }
}