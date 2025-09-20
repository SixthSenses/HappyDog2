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
            .aspectRatio(1f) // 정사각형에 가까운 네모 박스
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
            // 왼쪽 위: 제목
            Text(
                text = "견종\n가이드북",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MyPageColors.Orange800,
                modifier = Modifier.align(Alignment.TopStart)
            )
            
            // 오른쪽 아래: 이미지
            Image(
                painter = painterResource(id = R.drawable.breed_guide_icon),
                contentDescription = "견종 가이드북",
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomEnd),
                contentScale = ContentScale.Fit
            )
        }
    }
}