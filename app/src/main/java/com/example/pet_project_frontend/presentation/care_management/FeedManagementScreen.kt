package com.example.pet_project_frontend.presentation.care_management

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.navigation.Screen
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.presentation.petcare.home.PetCareHomeViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedManagementScreen(
    navController: NavController,
    selectedDate: String? = null,
    summaryViewModel: FeedManagementViewModel = hiltViewModel(),
    homeViewModel: PetCareHomeViewModel = hiltViewModel()
) {
    // 파라미터로 받은 날짜 파싱
    val initialDate = remember(selectedDate) {
        try {
            if (selectedDate != null) LocalDate.parse(selectedDate) else LocalDate.now()
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    var selectedMonth by remember(selectedDate) {
        mutableStateOf(YearMonth.from(initialDate))
    }
    var currentSelectedDate by remember(selectedDate) {
        mutableStateOf(initialDate)
    }
    val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)

    val summaryUiState by summaryViewModel.uiState.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val isToday = currentSelectedDate == LocalDate.now()
    val feedCount = summaryUiState.currentFeedCount
    val targetCount = summaryUiState.goalFeedCount
    val progressPercentage = summaryUiState.achievementPercentage


    // 선택된 날짜 데이터 로드
    LaunchedEffect(currentSelectedDate) {
        android.util.Log.d("FeedManagement", "Loading data for date: $currentSelectedDate")
        summaryViewModel.loadDataForDate(currentSelectedDate)
    }

    // +/- 버튼 클릭 후 데이터 다시 로드
    LaunchedEffect(homeUiState.currentFeedCount) {
        android.util.Log.d("FeedManagement", "Feed record changed, reloading summary")
        summaryViewModel.loadDataForDate(currentSelectedDate)
    }


    // 월 변경 시 달성 날짜 로드
    LaunchedEffect(selectedMonth) {
        android.util.Log.d("FeedManagement", "Month changed - Loading achieved dates for month: $selectedMonth")
        summaryViewModel.loadAchievedDatesForMonth(selectedMonth)
    }

    // API에서 가져온 실제 '목표 달성' 날짜 목록
    val achievedDates = summaryUiState.achievedDates

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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "사료",
                        fontSize = 16.sp,
                        color = MyPageColors.Blue500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isToday) "오늘 현재까지" else "${currentSelectedDate.monthValue}월 ${currentSelectedDate.dayOfMonth}일에",
                        fontSize = 23.sp,
                        color = MyPageColors.Grey900,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "목표를 ${progressPercentage.toInt()}% 달성했어요",
                        fontSize = 23.sp,
                        color = MyPageColors.Grey900,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressChart(
                        progress = progressPercentage / 100f,
                        size = 180.dp
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (isToday && feedCount > 0) {
                                summaryViewModel.removeFeedRecord()
                            }
                        },
                        enabled = isToday && feedCount > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isToday) MyPageColors.Grey100 else MyPageColors.Grey200,
                            disabledContainerColor = MyPageColors.Grey200
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = "-",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday && feedCount > 0) MyPageColors.Grey700 else MyPageColors.Grey500
                        )
                    }
                    Button(
                        onClick = {
                            if (isToday) {
                                summaryViewModel.addFeedRecord()
                            }
                        },
                        enabled = isToday,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isToday) MyPageColors.Blue500 else MyPageColors.Blue200,
                            disabledContainerColor = MyPageColors.Blue200
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = "+",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(
                            color = MyPageColors.Grey100,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "목표",
                            fontSize = 18.sp,
                            color = MyPageColors.Grey800,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1일 목표 섭취 횟수",
                                fontSize = 18.sp,
                                color = MyPageColors.Grey700
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${targetCount}회",
                                fontSize = 18.sp,
                                color = MyPageColors.Blue500,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { navController.navigate(Screen.FeedRecord.route) }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow),
                                    contentDescription = "설정하기",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(17.dp)
                        .background(MyPageColors.Grey100)
                )
            }

            item {
                // 캘린더 영역
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    // 월 네비게이션
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.calendar_arrow_left),
                                contentDescription = "이전 달",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = selectedMonth.format(monthFormatter),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MyPageColors.Grey800,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        IconButton(onClick = { selectedMonth = selectedMonth.plusMonths(1) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.calendar_arrow_right),
                                contentDescription = "다음 달",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "월간 분석",
                        fontSize = 16.sp,
                        color = MyPageColors.Grey500,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 월간 달성 횟수  표시
                    Text(
                        text = summaryUiState.monthlySummaryText,
                        fontSize = 23.sp,
                        color = MyPageColors.Grey900,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    )


                    Spacer(modifier = Modifier.height(8.dp))

                    // 월간 메시지 표시
                    Text(
                        text = summaryUiState.monthlyMessage,
                        fontSize = 23.sp,
                        color = MyPageColors.Grey900,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    )

                    // 디버그: 현재 상태 확인
                    LaunchedEffect(summaryUiState.monthlyMessage, summaryUiState.achievedDates) {
                        android.util.Log.d("FeedManagement", "UI Update - Monthly Message: ${summaryUiState.monthlyMessage}")
                        android.util.Log.d("FeedManagement", "UI Update - Achieved Dates: ${summaryUiState.achievedDates}")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    CalendarGrid(
                        selectedMonth = selectedMonth,
                        selectedDate = currentSelectedDate,
                        recordedDates = achievedDates,
                    )
                }
            }
        }
    }
}

// CircularProgressChart, CalendarGrid, CalendarDayItem 함수는 수정사항 없음 (기존 코드 유지)

@Composable
private fun CircularProgressChart(
    progress: Float,
    size: Dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()
            val diameter = this.size.minDimension
            val radius = (diameter - strokeWidth) / 2
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter - strokeWidth, diameter - strokeWidth)

            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = Color(0xFF4A90E2),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
        }

    }
}
// in FeedManagementScreen.kt

@Composable
private fun CalendarGrid(
    selectedMonth: YearMonth,
    selectedDate: LocalDate,
    recordedDates: Set<LocalDate>
) {
    val daysInMonth = selectedMonth.lengthOfMonth()
    val firstDayOfMonth = selectedMonth.atDay(1)
    val dayOfWeekOfFirstDay = firstDayOfMonth.dayOfWeek.value
    val emptyDays = (dayOfWeekOfFirstDay - DayOfWeek.MONDAY.value + 7) % 7
    val calendarDays = List(emptyDays) { null } + (1..daysInMonth).map { day -> selectedMonth.atDay(day) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 요일 헤더 (수정 없음)
        Row {
            listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        calendarDays.chunked(7).forEach { week ->
            Row {
                // --- ▼▼▼ 이 부분을 수정합니다 ▼▼▼ ---
                // 항상 7칸을 그리도록 for-loop로 변경
                for (i in 0..6) {
                    // week 리스트에 현재 인덱스(i)에 해당하는 날짜가 있는지 확인
                    if (i < week.size) {
                        val date = week[i]
                        if (date != null) {
                            CalendarDayItem(
                                date = date,
                                isSelected = date == selectedDate,
                                isAchieved = recordedDates.contains(date)
                            )
                        } else {
                            // 날짜가 null인 경우 (달의 시작 부분 빈 칸)
                            Spacer(modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f))
                        }
                    } else {
                        // week의 크기를 벗어나는 경우 (달의 마지막 부분 빈 칸)
                        Spacer(modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f))
                    }
                }
                // --- ▲▲▲ 수정 완료 ▲▲▲ ---
            }
        }
    }
}


@Composable
private fun RowScope.CalendarDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isAchieved: Boolean
) {
    val today = LocalDate.now()
    val isPastOrToday = !date.isAfter(today)
    val textColor = if(isPastOrToday) {
        Color(0xFF757575)
    } else {
            Color(0xFFBDBDBD)
    }

    // --- ▼▼▼ Box의 modifier를 수정합니다 ▼▼▼ ---
    Box(
        // weight(1f)가 부모 Row에 의해 이미 적용되었으므로 여기서는 제거해도 되지만,
        // 명시적으로 남겨두어 각 아이템이 균등한 너비를 갖도록 보장합니다.
        modifier = Modifier
            .weight(1f) // <-- 이 weight가 각 날짜 칸의 너비를 1/7로 고정시킵니다.
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ){

        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
        if (isAchieved) {
            Icon(
                painter = painterResource(id = R.drawable.selected_day),
                contentDescription = "목표 달성",
                modifier = Modifier
                    .size(30.dp), // 아이콘 크기 조절
                tint = Color.Unspecified
            )
        }
    }
    // --- ▲▲▲ 수정 완료 ▲▲▲ ---
}
