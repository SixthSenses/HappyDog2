package com.example.pet_project_frontend.presentation.eye_health

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.domain.model.EyeAnalysisHistoryItem
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyeHealthHistoryScreen(
    onBackClick: () -> Unit,
    onImageClick: (String) -> Unit = {},
    viewModel: EyeHealthHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 에러 처리
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "안구 검사 결과 내역", 
                color = MyPageColors.Grey900, 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            when {
                uiState.isLoading -> {
                    // 로딩 상태
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.historyItems.isEmpty() -> {
                    // 검사 기록이 없을 때
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.eye_disease_image),
                            contentDescription = "검사 기록 없음",
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "아직 검사 기록이 없어요",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MyPageColors.Grey700
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "첫 번째 안구 검사를 시작해보세요",
                            fontSize = 14.sp,
                            color = MyPageColors.Grey600
                        )
                    }
                }
                
                else -> {
                    // 검사 기록 리스트
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.historyItems) { historyItem ->
                            EyeHealthHistoryItem(
                                historyItem = historyItem,
                                onImageClick = onImageClick
                            )
                        }
                        
                        // 더 많은 데이터가 있으면 로드 버튼 표시
                        if (uiState.hasMore) {
                            item {
                                Button(
                                    onClick = { viewModel.loadMoreHistory() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("더 보기")
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EyeHealthHistoryItem(
    historyItem: EyeAnalysisHistoryItem,
    onImageClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘 (정상/질환으로 2가지 경우만 표시)
            Image(
                painter = painterResource(
                    id = if (historyItem.isNormal) {
                        R.drawable.eye_check // 정상인 경우
                    } else {
                        R.drawable.x // 질환이 있는 경우
                    }
                ),
                contentDescription = "검사 결과 아이콘",
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 텍스트 정보 (3줄)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 질환명 또는 결과 (가장 높은 확률의 질환)
                val primaryDisease = if (historyItem.isNormal) {
                    "정상"
                } else {
                    historyItem.predictions.maxByOrNull { it.probability }?.diseaseName ?: historyItem.diseaseName
                }
                
                Text(
                    text = primaryDisease,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey800
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // 확률 (가장 높은 확률 표시)
                val primaryProbability = if (historyItem.isNormal) {
                    "정상"
                } else {
                    "${historyItem.probabilityPercent}%"
                }
                
                Text(
                    text = primaryProbability,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MyPageColors.Grey600
                )
                Spacer(modifier = Modifier.height(2.dp))
                
                // 날짜 (LocalDateTime을 포맷팅)
                val formattedDate = historyItem.createdAt.format(
                    DateTimeFormatter.ofPattern("yyyy.MM.dd")
                )
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    color = MyPageColors.Grey600
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 사진 버튼 (이미지가 있을 때만 표시)
            if (!historyItem.imageUrl.isNullOrBlank()) {
                Button(
                    onClick = { 
                        onImageClick(historyItem.imageUrl!!)
                    },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyPageColors.Grey100
                    )
                ) {
                    Text(
                        text = "사진",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MyPageColors.Grey700
                    )
                }
            }
        }
    }
}