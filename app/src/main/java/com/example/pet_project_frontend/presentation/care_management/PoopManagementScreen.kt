package com.example.pet_project_frontend.presentation.care_management

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.navigation.Screen
import com.example.pet_project_frontend.core.theme.MyPageColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoopManagementScreen(
    navController: NavController,
    selectedDate: String? = null,
    viewModel: PoopManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val today = LocalDate.now()
    val parsedDate = remember(selectedDate) {
        if (selectedDate != null) {
            try {
                LocalDate.parse(selectedDate)
            } catch (e: Exception) {
                today // 파싱 실패 시 오늘 날짜로 대체
            }
        } else {
            today // null이면 오늘 날짜
        }
    }

    val isToday = parsedDate.isEqual(today)
    val formattedDateText = remember(parsedDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", java.util.Locale.KOREA)
        parsedDate.format(formatter)
    }

    LaunchedEffect(parsedDate) {
        viewModel.loadRecords(parsedDate)
    }

    LaunchedEffect(navController) {
        val currentBackStackEntry = navController.currentBackStackEntry
        currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refresh")
            ?.observeForever { refresh ->
                if (refresh == true) {
                    viewModel.loadRecords(parsedDate)
                    currentBackStackEntry.savedStateHandle.set("refresh", false)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
        ) {
            // 상단 제목
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "대변",
                        fontSize = 16.sp,
                        color = MyPageColors.Blue500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 메인 텍스트
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = if (isToday) "오늘 현재까지" else formattedDateText+"까지",
                        fontSize = 23.sp,
                        color = MyPageColors.Grey900,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.records.isEmpty()) {
                        Text(
                            text = "대변 기록을 남기지 않았어요",
                            fontSize = 23.sp,
                            color = MyPageColors.Grey900,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 32.sp
                        )
                    } else {
                        Text(
                            text = buildAnnotatedString {
                                append("대변 기록이 ")
                                withStyle(style = SpanStyle(color = MyPageColors.Blue500)) {
                                    append("${uiState.records.size}건")
                                }
                                append(" 있어요")
                            },
                            fontSize = 23.sp,
                            color = MyPageColors.Grey900, // 기본 텍스트 색상
                            fontWeight = FontWeight.Medium,
                            lineHeight = 32.sp
                        )
                    }
                }
            }
            // --- ▼▼▼ 여기가 수정된 부분입니다 ▼▼▼ ---
            // Column 밖으로 모든 item을 꺼냈습니다.
            item { Spacer(modifier = Modifier.height(40.dp)) }

            // 기록하기 버튼
            item {
                Button(
                    onClick = {
                        navController.navigate(Screen.PoopRecord.route)
                    },
                    enabled = isToday, // isToday 값에 따라 활성/비활성 결정
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyPageColors.Blue500,
                        disabledContainerColor = MyPageColors.Blue200 // 비활성화 색상 지정
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "기록하기",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 구분선
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(17.dp)
                        .background(MyPageColors.Grey100)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 기록 목록 카드
            if (uiState.records.isNotEmpty()) {
                items(uiState.records, key = { it.logId }) { record ->
                    RecordCard(
                        data = record.data,
                        time = record.formattedTime,
                        onDeleteClick = { viewModel.deleteRecord(record.logId) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                // 기록이 없을 때
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp, bottom = 150.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dog_log),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "아직 남긴 기록이 없어요",
                            fontSize = 17.sp,
                            color = MyPageColors.Grey800,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
            // --- ▲▲▲ 여기가 수정된 부분입니다 ▲▲▲ ---
        }
    }
}

/**
 * 기록 카드 컴포넌트
 * 크기: 370x68
 * 배경색: Grey100
 */
@Composable
private fun RecordCard(
    data: String,
    time: String,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MyPageColors.Grey100
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = data,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MyPageColors.Grey800
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = time,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MyPageColors.Grey600
                )
            }

            Button(
                onClick = onDeleteClick,
                modifier = Modifier
                    .width(50.dp)
                    .height(35.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MyPageColors.Red500
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "삭제",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
