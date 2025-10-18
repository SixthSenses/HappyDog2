package com.example.pet_project_frontend.presentation.care_management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.navigation.Screen
import com.example.pet_project_frontend.presentation.petcare.home.PetCareHomeViewModel
import com.example.pet_project_frontend.core.theme.MyPageColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityManagementScreen(
    navController: NavController,
    selectedDate: String? = null,
    summaryViewModel: ActivityManagementViewModel = hiltViewModel(),
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

    //val feedCount = summaryUiState.currentFeedCount
    val targetMinutes = summaryUiState.selectedDateSessionMinutes
    val todayProgressPercentage = summaryUiState.selectedDateAchievementPercentage
    //val previousProgressPercentage = summaryUiState.previousDateActivityMinutes
    val goalActivityCount = summaryUiState.goalActivityCount
    
    // 선택된 날짜 데이터 로드
    LaunchedEffect(currentSelectedDate) {
        android.util.Log.d("ActivityManagement", "Loading data for date: $currentSelectedDate")
        summaryViewModel.loadDataForDate(currentSelectedDate)
    }
    
    // +/- 버튼 클릭 후 데이터 다시 로드
    LaunchedEffect(homeUiState.currentActivityMinutes) {
        android.util.Log.d("ActivityManagement", "Activity record changed, reloading summary")
        summaryViewModel.loadDataForDate(currentSelectedDate)
    }
    
    // 월 변경 시 달성 날짜 로드
    LaunchedEffect(selectedMonth) {
        android.util.Log.d("ActivityManagement", "Month changed - Loading achieved dates for month: $selectedMonth")
        summaryViewModel.loadAchievedDatesForMonth(selectedMonth)
    }

    // 캘린더에 표시할 달성 날짜 (ViewModel에서 가져옴)
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
                        text = "활동",
                        fontSize = 16.sp,
                        color = MyPageColors.Blue500, // 수정
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
                        color = MyPageColors.Grey900, // 수정
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "목표를 ${todayProgressPercentage.toInt()}% 달성했어요",
                        fontSize = 23.sp,
                        color = MyPageColors.Grey900, // 수정
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    val uiState = summaryUiState
                    val selectedDateMinutes = uiState.selectedDateActivityLiveMinutes
                    val previousDayMinutes = uiState.previousDateActivityMinutes
                    //val goalMinutes = uiState.goalActivityCount * uiState.selectedDateSessionMinutes
                    //val maxMinutes = if (goalMinutes > 0) goalMinutes.toFloat() else maxOf(selectedDateMinutes, previousDayMinutes, 60).toFloat()
                    val currentProgress = uiState.selectedDateAchievementPercentage / 100f
                    val previousProgress = uiState.previousDateAchivementPercentage / 100f

                    BarChartView(
                        currentProgress = currentProgress,
                        previousProgress = previousProgress,
                        currentBarText = "${selectedDateMinutes}분",
                        previousBarText = "${previousDayMinutes}분",
                        currentBarLabel = "오늘",
                        previousBarLabel = "어제"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isToday && selectedDateMinutes >= uiState.selectedDateSessionMinutes) {
                                    summaryViewModel.removeActivityRecord(uiState.selectedDateSessionMinutes)
                                }
                            },
                            enabled = isToday && selectedDateMinutes >= uiState.selectedDateSessionMinutes,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isToday) MyPageColors.Grey100 else MyPageColors.Grey200, // 수정
                                disabledContainerColor = MyPageColors.Grey200 // 수정
                            ),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text(
                                text = "-",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday && selectedDateMinutes >= uiState.selectedDateSessionMinutes) MyPageColors.Grey700 else MyPageColors.Grey500 // 수정
                            )
                        }

                        Button(
                            onClick = {
                                if (isToday) {
                                    summaryViewModel.addActivityRecord(uiState.selectedDateSessionMinutes)
                                }
                            },
                            enabled = isToday,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isToday) MyPageColors.Blue500 else MyPageColors.Blue200, // 수정
                                disabledContainerColor = MyPageColors.Blue200 // 수정
                            ),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text(text = "+",
                                 fontSize = 24.sp,
                                 fontWeight = FontWeight.Bold,
                                 color = Color.White
                            )
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MyPageColors.Grey100, // 수정
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "목표",
                            fontSize = 18.sp,
                            color = MyPageColors.Grey800, // 수정
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1회 활동 시간",
                                fontSize = 18.sp,
                                color = MyPageColors.Grey700 // 수정
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${summaryUiState.selectedDateSessionMinutes}분",
                                fontSize = 18.sp,
                                color = MyPageColors.Blue500, // 수정
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { navController.navigate(Screen.ActivityRecord.route) }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow),
                                    contentDescription = "설정하기",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1일 목표 활동 횟수",
                                fontSize = 18.sp,
                                color = MyPageColors.Grey700 // 수정
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${goalActivityCount}회",
                                fontSize = 18.sp,
                                color = MyPageColors.Blue500, // 수정
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { navController.navigate(Screen.ActivityRecord.route) }
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(17.dp)
                        .background(MyPageColors.Grey100) // 수정
                )
            }
            // 캘린더 영역
            item {
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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MyPageColors.Grey800, // 수정
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
                        recordedDates = achievedDates  // API에서 가져온 실제 달성 날짜
                    )
                }
            }
        }
    }
}
/*

@Composable
fun BarChartView(
    currentProgress: Float,
    previousProgress: Float,
    currentBarText: String,
    previousBarText: String,
    currentBarLabel: String,
    previousBarLabel: String
) {
    val graphMaxHeight = 128.dp
    val minGraphHeightForText = 30.dp
    val minBarProgress = 0.02f // 2%

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(graphMaxHeight + minGraphHeightForText)
                .graphicsLayer { clip = false },
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            // '어제' 막대그래프와 텍스트
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(graphMaxHeight)
                        //.graphicsLayer { clip = false }
                ) {
                    val previousProgressToShow = maxOf(previousProgress, minBarProgress)
                    val previousBarHeight = graphMaxHeight * previousProgressToShow.coerceIn(0f, 1f)
                    //val textPaddingBottom = maxOf(graphMaxHeight * previousProgress.coerceIn(0f, 1f), minGraphHeightForText)
                    val textPaddingBottom = previousBarHeight + minGraphHeightForText
                    Text(
                        text = previousBarText,
                        fontSize = 13.sp,
                        color = MyPageColors.Grey500, // 수정
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = textPaddingBottom)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(previousBarHeight)
                            .background(
                                MyPageColors.Grey200, // 수정
                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
                            .align(Alignment.BottomCenter)
                    )
                }
            }

            // '오늘' 막대그래프와 텍스트 (동일한 로직 적용)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(graphMaxHeight)
                        //.graphicsLayer { clip = false }
                ) {
                    val currentProgressToShow = maxOf(currentProgress, minBarProgress)
                    val currentBarHeight = graphMaxHeight * currentProgressToShow.coerceIn(0f, 1f)
                    //val textPaddingBottom = maxOf(graphMaxHeight * currentProgress.coerceIn(0f, 1f), minGraphHeightForText)
                    val textPaddingBottom = currentBarHeight + minGraphHeightForText
                    Text(
                        text = currentBarText,
                        fontSize = 13.sp,
                        color = MyPageColors.Blue500, // 수정
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = textPaddingBottom)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(currentBarHeight)
                            .background(
                                MyPageColors.Blue500, // 수정
                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MyPageColors.Grey100 // 수정
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = previousBarLabel,
                fontSize = 13.sp,
                color = MyPageColors.Grey700, // 수정
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = currentBarLabel,
                fontSize = 13.sp,
                color = MyPageColors.Grey700, // 수정
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
*/

@Composable
fun BarChartView(
    currentProgress: Float,
    previousProgress: Float,
    currentBarText: String,
    previousBarText: String,
    currentBarLabel: String,
    previousBarLabel: String
) {
    // [수정 1] 이제 텍스트 높이는 BoxWithConstraints가 관리하므로, 별도 계산이 필요 없습니다.
    val graphMaxHeight = 128.dp
    val minBarProgress = 0.02f // 데이터가 0일 때 보여줄 최소 막대 높이

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // [수정 2] Row는 자식의 컨텐츠 크기에 맞춰 높이가 자동으로 조절되도록 합니다.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom // 자식들을 아래쪽 기준으로 정렬
        ) {
            // '어제' 막대
            BarItem(
                progress = previousProgress,
                barText = previousBarText,
                barColor = MyPageColors.Grey200,
                textColor = MyPageColors.Grey500,
                graphMaxHeight = graphMaxHeight,
                minBarProgress = minBarProgress
            )

            // '오늘' 막대
            BarItem(
                progress = currentProgress,
                barText = currentBarText,
                barColor = MyPageColors.Blue500,
                textColor = MyPageColors.Blue500,
                graphMaxHeight = graphMaxHeight,
                minBarProgress = minBarProgress
            )
        }

        // --- (아래 Divider, Spacer, Label Row는 기존 코드와 동일하게 유지) ---
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MyPageColors.Grey100
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = previousBarLabel,
                fontSize = 13.sp,
                color = MyPageColors.Grey700,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = currentBarLabel,
                fontSize = 13.sp,
                color = MyPageColors.Grey700,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * [핵심 수정] 막대와 텍스트를 그리는 단일 아이템입니다.
 * BoxWithConstraints를 사용하여 텍스트 위치를 고정하고, 막대 높이만 동적으로 변경합니다.
 */
/**
 * [핵심 수정] 막대와 텍스트를 그리는 단일 아이템입니다.
 * BoxWithConstraints 대신 일반 Box를 사용하여 코드를 최적화합니다.
 */
@Composable
private fun BarItem(
    progress: Float,
    barText: String,
    barColor: Color,
    textColor: Color,
    graphMaxHeight: Dp,
    minBarProgress: Float
) {
    // [수정 1] BoxWithConstraints를 일반 Box로 변경합니다.
    Box(
        // 자식들을 아래쪽 중앙에 정렬합니다.
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .width(60.dp)
            // 전체 높이를 '막대 최대 높이 + 텍스트를 위한 여유 공간'으로 고정합니다.
            .height(graphMaxHeight + 32.dp) // 32dp는 텍스트와 여백을 위한 공간
    ) { // [수정 2] BoxWithConstraints의 scope 파라미터가 더 이상 필요 없으므로 제거합니다.
        // 보여줄 진행률과 막대의 실제 높이를 계산합니다.
        val progressToShow = maxOf(progress, minBarProgress)
        val currentBarHeight = graphMaxHeight * progressToShow.coerceIn(0f, 1f)

        // 1. 막대를 그립니다.
        // contentAlignment이 BottomCenter이므로 이 Box는 항상 맨 아래에 그려집니다.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(currentBarHeight) // 높이는 progress에 따라 동적으로 변경됩니다.
                .background(
                    color = barColor,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
        )

        // 2. 텍스트를 그립니다.
        // 텍스트의 아래쪽 여백을 '막대 높이'로 지정합니다.
        // 이렇게 하면 텍스트는 항상 막대 바로 위에 위치하게 됩니다.
        Text(
            text = barText,
            fontSize = 13.sp,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = currentBarHeight + 4.dp) // 막대 높이 + 약간의 여백
        )
    }
}



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
