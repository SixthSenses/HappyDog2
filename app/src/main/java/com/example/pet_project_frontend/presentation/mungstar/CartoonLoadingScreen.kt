package com.example.pet_project_frontend.presentation.mungstar

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.PretendardFont
import kotlinx.coroutines.delay

@Composable
fun CartoonLoadingScreen(
    navController: NavController,
    viewModel: CartoonLoadingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 로딩 애니메이션을 위한 상태 (0, 1, 2 순환)
    var activeLoadingDot by remember { mutableStateOf(0) }

    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) {
            while (true) {
                delay(300) // 각 점 사이 300ms 딜레이
                activeLoadingDot = (activeLoadingDot + 1) % 3
            }
        }
    }
    
    // 완료 시 토스트 표시 및 피드로 이동
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            Toast.makeText(context, "만화가 등록되었어요.", Toast.LENGTH_SHORT).show()
            navController.navigate("community") {
                popUpTo("cartoon_making") { inclusive = true }
            }
        }
    }
    
    // 취소 시 이전 화면으로
    LaunchedEffect(uiState.isCancelled) {
        if (uiState.isCancelled) {
            navController.popBackStack()
        }
    }
    
    // 에러 발생 시 토스트 표시
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_SHORT).show()
            delay(2000)
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바 (412X64)
        Box(
            modifier = Modifier
                .width(412.dp)
                .height(64.dp)
                .background(Color.White)
                .align(Alignment.TopCenter)
        ) {
            // back.png (40X40, 왼쪽에서 8픽셀) - 클릭 시 작업 취소
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "뒤로가기 및 작업 취소",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 8.dp)
                    .clickable {
                        viewModel.cancelJob()
                        navController.popBackStack()
                    }
            )
        }

        // 중앙 로딩 영역
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 로딩 점 3개 (12X12, 10픽셀 간격)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 46.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = if (activeLoadingDot == index) 
                                    Color(0xFF8B95A1) 
                                else 
                                    Color(0, 29, 58, (0.18f * 255).toInt()),
                                shape = CircleShape
                            )
                    )
                }
            }

            // 작성한 이야기가\n만화로 그려지고 있어요 (28픽셀, weight 600, #191F28)
            Text(
                text = "작성한 이야기가\n만화로 그려지고 있어요",
                fontFamily = PretendardFont,
                fontWeight = FontWeight(600),
                fontSize = 28.sp,
                color = Color(0xFF191F28),
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
        }
    }
}
