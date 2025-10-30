package com.example.pet_project_frontend.presentation.mungstar

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    
    // 중단 확인 다이얼로그 표시 상태
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) {
            while (true) {
                delay(300) // 각 점 사이 300ms 딜레이
                activeLoadingDot = (activeLoadingDot + 1) % 3
            }
        }
    }
    
    // 완료 시 피드로 이동 (토스트는 멍스타그램에서 표시)
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            // 멍스타그램으로 이동하기 전에 플래그 설정
            navController.navigate("community") {
                popUpTo("cartoon_making") { inclusive = true }
            }
            // 네비게이션 후 플래그 설정
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("cartoon_created", true)
        }
    }
    
    // 취소 시 이전 화면으로
    LaunchedEffect(uiState.isCancelled) {
        if (uiState.isCancelled) {
            navController.popBackStack()
        }
    }
    
    // 에러 발생 시 토스트 표시 후 이전 화면으로 (자동으로 돌아감)
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
            delay(3000) // 3초 후 자동으로 뒤로가기
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바 (디바이스 너비 × 64픽셀)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White)
                .align(Alignment.TopCenter)
        ) {
            // back.png (24X24, 왼쪽에서 16픽셀) - 클릭 시 중단 확인 다이얼로그 표시
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "뒤로가기 및 작업 취소",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 16.dp)
                    .clickable {
                        showCancelDialog = true
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
        
        // 중단 확인 다이얼로그
        if (showCancelDialog) {
            Dialog(
                onDismissRequest = { showCancelDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showCancelDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    // 다이얼로그 콘텐츠 (336×144, 둥근 모서리 26)
                    Column(
                        modifier = Modifier
                            .width(336.dp)
                            .height(144.dp)
                            .background(
                                color = Color(0xFFFFFFFF),
                                shape = RoundedCornerShape(26.dp)
                            )
                            .clickable(enabled = false) { } // 내부 클릭은 무시
                    ) {
                        // 제목 텍스트 (좌측 17px, 상단 17px)
                        Text(
                            text = "만화 생성을 중단할까요?",
                            fontFamily = PretendardFont,
                            fontWeight = FontWeight(600),
                            fontSize = 21.sp,
                            color = Color(0xFF333D4B),
                            modifier = Modifier.padding(start = 17.dp, top = 17.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(21.dp))
                        
                        // 버튼 영역 (좌측 17px)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 17.dp, end = 17.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 닫기 버튼 (146×58, 모서리 14, #F3F4F6)
                            Box(
                                modifier = Modifier
                                    .width(146.dp)
                                    .height(58.dp)
                                    .background(
                                        color = Color(0xFFF3F4F6),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { showCancelDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "닫기",
                                    fontFamily = PretendardFont,
                                    fontWeight = FontWeight(600),
                                    fontSize = 18.sp,
                                    color = Color(0xFF4E5968)
                                )
                            }
                            
                            // 중단하기 버튼 (146×58, 모서리 14, #EC4453)
                            Box(
                                modifier = Modifier
                                    .width(146.dp)
                                    .height(58.dp)
                                    .background(
                                        color = Color(0xFFEC4453),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        showCancelDialog = false
                                        viewModel.cancelJob()
                                        navController.popBackStack()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "중단하기",
                                    fontFamily = PretendardFont,
                                    fontWeight = FontWeight(600),
                                    fontSize = 18.sp,
                                    color = Color(0xFFFFFFFF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
