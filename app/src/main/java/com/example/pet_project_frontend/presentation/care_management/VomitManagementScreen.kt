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
fun VomitManagementScreen(
    navController: NavController,
    selectedDate: String? = null,
    viewModel: VomitManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 오늘 날짜 확인
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
    // "YYYY년 M월 d일" 형식으로 날짜 포맷팅
    val formattedDateText = remember(parsedDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", java.util.Locale.KOREA)
        parsedDate.format(formatter)
    }
    // 선택된 날짜로 기록 로드
    // 선택된 날짜로 기록 로드
    LaunchedEffect(parsedDate) {
        viewModel.loadRecords(parsedDate)
    }

    // 화면 진입 시 및 기록 화면에서 돌아올 때마다 최신 데이터 로드
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
                        text = "구토",
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
                        text = if (isToday) "오늘 현재까지" else formattedDateText+"까지", // 날짜 텍스트 변경
                        fontSize = 23.sp,
                        color = MyPageColors.Grey900,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // --- 여기부터 수정된 부분 ---
                    if (uiState.records.isEmpty()) {
                        Text(
                            text = "구토를 하지 않았어요",
                            fontSize = 23.sp,
                            color = MyPageColors.Grey900,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 32.sp
                        )
                    } else {
                        Text(
                            text = buildAnnotatedString {
                                append("구토한 기록이 ")
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
                    // --- 여기까지 수정된 부분 ---
                }
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }

            // 기록하기 버튼
            item {
                Button(
                    onClick = {
                        navController.navigate(Screen.VomitRecord.route)
                    },
                    enabled = isToday,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyPageColors.Blue500,
                        disabledContainerColor = MyPageColors.Blue200
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

            item { Spacer(modifier = Modifier.height(32.dp)) }

            // 추가 안내 박스
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .background(
                            color = MyPageColors.Grey100,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "반려견의 건강이 걱정된다면\n건강 설문지로 빠르게 검사해보세요",
                            fontSize = 16.sp,
                            color = MyPageColors.Grey700,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Image(
                            painter = painterResource(id = R.drawable.health_survey_icon),
                            contentDescription = "건강 설문 아이콘",
                            modifier = Modifier.size(40.dp)
                        )
                    }
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
            // 왼쪽: 데이터와 시간
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

            // 오른쪽: 삭제 버튼
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