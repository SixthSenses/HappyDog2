package com.example.pet_project_frontend.presentation.care_management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.navigation.Screen
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.presentation.care_management.WeightManagementViewModel
import com.example.pet_project_frontend.presentation.mypage.main.MyPageScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.text.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightManagementScreen(
    navController: NavController,
    selectedDate: String? = null,
    viewModel: WeightManagementViewModel = hiltViewModel()
) {
    // 파라미터로 받은 날짜 파싱
    val initialDate = remember(selectedDate) {
        try {
            if (selectedDate != null) LocalDate.parse(selectedDate) else LocalDate.now()
        } catch (e: Exception) {
            LocalDate.now()
        }
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(initialDate) {
        viewModel.loadDataForDate(initialDate)
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "몸무게",
                        fontSize = 16.sp,
                        color = Color(0xFF4A90E2), // Blue500
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            // 상단 분석 텍스트
            item {
                // 선택된 날짜가 오늘일때 사용하는 변수들
                val targetWeight = uiState.targetWeight
                val selectedDateWeight = uiState.selectedDateWeight
                val weightDiff = uiState.weightDiff
                val selectedDate = uiState.selectedDate // ViewModel에 이 상태가 있다고 가정
                val isTodaySelected = selectedDate == LocalDate.now()
                val todayWeight = uiState.todayWeight

                // 1. 상단 텍스트 경우의 수 로직
                val topText = remember(targetWeight, selectedDateWeight, isTodaySelected) {
                    when {
                        // 1-1. 목표가 없을 때 (오늘 선택 시)
                        isTodaySelected && targetWeight == null ->
                            "몸무게를 기록하기 위해선\n목표를 먼저 설정해주세요"

                        // 1-2. 목표는 있는데 오늘 기록이 없을 때 (오늘 선택 시)
                        isTodaySelected && targetWeight != null && selectedDateWeight == null ->
                            "오늘의 몸무게를\n아직 기록하지 않았어요"

                        // 1-3. 목표도 있고 오늘 기록도 있을 때 (오늘 선택 시)
                        isTodaySelected && targetWeight != null && selectedDateWeight != null -> {
                            when {
                                weightDiff == null -> "목표와의 차이를 계산 중입니다." // 예외 처리
                                weightDiff > 0 -> "목표를 달성하려면\n${abs(weightDiff)}kg을 더 감량해야 해요"
                                weightDiff < 0 -> "목표보다\n${abs(weightDiff)}kg 더 가벼워요!"
                                else -> "축하해요!\n목표 몸무게를 달성했어요"
                            }
                        }

                        // 1-4. 다른 날을 선택했을 때
                        !isTodaySelected -> {
                            val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))
                            when {
                                selectedDateWeight == null -> "${formattedDate}에는\n기록된 몸무게가 없어요"
                                todayWeight == null -> "${formattedDate}의 몸무게는\n${selectedDateWeight}kg 이었어요"
                                selectedDateWeight > todayWeight -> "${formattedDate}은\n오늘보다 무거웠어요"
                                selectedDateWeight < todayWeight -> "${formattedDate}은\n오늘보다 가벼웠어요"
                                else -> "${formattedDate}은\n오늘과 몸무게가 같아요"
                            }
                        }

                        else -> "" // 모든 조건에 해당하지 않을 경우
                    }
                }

                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 계산된 텍스트를 \n 기준으로 분리하여 두 줄로 표시
                    topText.split("\n").forEach { line ->
                        Text(
                            text = line,
                            fontSize = 23.sp,
                            color = MyPageColors.Grey900,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 30.sp
                        )
                    }
                }
            }


            item {
                // WeightBarChart를 호출할 때 ViewModel의 uiState 값을 전달합니다.
                // 선택한 날짜를 "9월 19일" 형식으로 포맷
                val formattedSelectedDate = if (uiState.selectedDate != LocalDate.now()) {
                    uiState.selectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))
                } else {
                    null
                }
                
                WeightBarChart(
                    todayWeight = uiState.todayWeight,
                    targetWeight = uiState.targetWeight,
                    selectedDateWeight = uiState.selectedDateWeight,
                    isTodaySelected = uiState.selectedDate == LocalDate.now(),
                    selectedDate = formattedSelectedDate
                )
            }
            // 기록하기 버튼(경우의 수에 따라 비활성화, 활성화, 텍스트 수정)
            item {
                val targetWeight = uiState.targetWeight
                val todayWeight = uiState.todayWeight
                val isTodaySelected = uiState.selectedDate == LocalDate.now()

                // 2. 버튼 상태 경우의 수 로직
                val buttonText = when {
                    isTodaySelected && targetWeight != null && todayWeight != null -> "수정하기"
                    else -> "기록하기"
                }
                val isButtonEnabled = when {
                    isTodaySelected && targetWeight == null -> false // 목표가 없을때
                    !isTodaySelected -> false // 다른날을 선택했을 때
                    else -> true // 그 외 모든 경우 (오늘 + 목표 있음)
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            // 기록하기/수정하기 화면으로 이동
                            navController.navigate(Screen.WeightLog.route)
                        },
                        enabled = isButtonEnabled, // 계산된 활성화 상태 적용
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (buttonText == "수정하기") {
                                MyPageColors.Blue50 // '수정하기'일 때 Blue50
                            } else {
                                MyPageColors.Blue500 // '기록하기'일 때 Blue500
                            },
                            disabledContainerColor = MyPageColors.Blue200
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = buttonText, // 계산된 텍스트 적용
                            fontSize = 16.sp,
                            color = if (isButtonEnabled && buttonText == "수정하기") MyPageColors.Blue800 else Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            item {
                // 4 & 5. 목표 카드 수정 (높이, 레이아웃)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MyPageColors.Grey100,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "목표",
                            fontSize = 18.sp,
                            color = MyPageColors.Grey800,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "목표 몸무게",
                                fontSize = 18.sp,
                                color = MyPageColors.Grey700,
                            )
                            Spacer(modifier = Modifier.weight(1f)) // 공간 채우기
                            Text(
                                text = if (uiState.targetWeight != null) {
                                    "${uiState.targetWeight}kg"
                                } else {
                                    "미설정"
                                },
                                fontSize = 18.sp,
                                color = MyPageColors.Blue500,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { navController.navigate(Screen.WeightRecord.route) }
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

            // 6. 구분선 추가
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(17.dp)
                        .background(MyPageColors.Grey100)
                )
            }

            item {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    Text(
                        text = "월간 분석",
                        fontSize = 16.sp,
                        color = MyPageColors.Grey500
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // API에서 받은 월간 분석 텍스트 표시
                    Text(
                        text = uiState.monthlyAnalysisText,
                        fontSize = 23.sp,
                        color = MyPageColors.Grey900,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // API에서 받은 월간 데이터로 그래프 표시
                    SixMonthWeightChart(monthlyWeights = uiState.monthlyWeights)
                }
            }
        }
    }
}

@Composable
private fun WeightBarChart(
    todayWeight: Float?,
    targetWeight: Float?,
    selectedDateWeight: Float?,
    isTodaySelected: Boolean,
    selectedDate: String?
) {
    // 1. 경우의 수에 따라 왼쪽/오른쪽 막대에 표시될 값과 라벨을 결정합니다.
    val (leftWeight, leftLabel) = if (isTodaySelected) {
        todayWeight to "오늘"
    } else {
        selectedDateWeight to "$selectedDate" // '선택한 날'을 줄여서 '선택'으로
    }

    val (rightWeight, rightLabel) = if (isTodaySelected) {
        targetWeight to "목표"
    } else {
        todayWeight to "오늘"
    }
    // 2. 두 값 중 더 큰 값을 기준으로 최대값을 설정합니다. (0으로 나누기 방지 포함)
    val maxValue = maxOf(leftWeight ?: 0f, rightWeight ?: 0f, 1f)
    val maxHeight = 128.dp
    val minHeightValue = maxHeight.value * 0.02f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // 왼쪽 막대 (오늘 or 선택일)
            Bar(
                weight = leftWeight,
                // 비율에 맞게 높이를 동적으로 계산합니다.
                height = ((maxHeight.value * (leftWeight ?: 0f) / maxValue)
                    .coerceAtLeast(if (leftWeight == null) minHeightValue else 0f)
                        ).roundToInt().dp,
                color = MyPageColors.Blue500
            )

            // 오른쪽 막대 (목표 or 오늘)
            Bar(
                weight = rightWeight,
                // 비율에 맞게 높이를 동적으로 계산합니다.
                height = ((maxHeight.value * (rightWeight ?: 0f) / maxValue)
                    .coerceAtLeast(if (leftWeight == null) minHeightValue else 0f)
                        ).roundToInt().dp,
                color = MyPageColors.Grey200
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MyPageColors.Grey300)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. 하단 라벨도 경우의 수에 따라 동적으로 표시합니다.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = leftLabel, fontSize = 13.sp, color = MyPageColors.Grey700, modifier = Modifier.offset(x = (-7).dp) )
            Text(text = rightLabel, fontSize = 13.sp, color = MyPageColors.Grey700, modifier = Modifier.offset(x = (-4).dp) )
        }
    }
}



@Composable
private fun Bar(
    weight: Float?,
    height: Dp,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (weight != null) "${weight}kg" else "?", // 값이 없으면 "-" 표시
            fontSize = 13.sp,
            color = MyPageColors.Grey500
        )
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(height.coerceAtLeast(1.dp)) // 최소 높이 1dp 보장
                .background(
                    color = color,
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        )
    }
}
/**
 * 지난 6개월간의 몸무게 데이터를 시각화하는 차트 컴포저블입니다.
 * ViewModel로부터 받은 데이터를 그대로 사용하여 모든 UI 요구사항을 처리합니다.
 */
@Composable
private fun SixMonthWeightChart(
    monthlyWeights: List<WeightManagementViewModel.MonthlyWeightData>
) {
    // 데이터가 없으면 차트를 그리지 않고 종료
    if (monthlyWeights.isEmpty()) {
        return
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val maxHeight = 128.dp

        // [요구사항 반영 1] 마지막 라벨을 "이번 달"로 변경하여 UI에 표시할 최종 데이터 생성
        val displayData = monthlyWeights.mapIndexed { index, data ->
            val finalLabel = if (index == monthlyWeights.lastIndex) "이번달" else data.month
            // Triple: (라벨, 몸무게, 이번달 여부) -> 몸무게가 0f이면 null로 취급
            Triple(finalLabel, data.weight.takeIf { it > 0f }, index == monthlyWeights.lastIndex)
        }

        // [요구사항 반영 2] 유효한(null이 아닌) 몸무게 값 중에서 최대값 찾기
        val maxWeight = displayData.mapNotNull { it.second }.maxOfOrNull { it } ?: 50f
        val randomHeights = remember { listOf(60.dp, 80.dp, 45.dp, 70.dp, 55.dp, 90.dp) }

        // 막대그래프 Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            displayData.forEachIndexed { index, (label, weight, isCurrentMonth) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // [요구사항 반영 3] 막대 위 텍스트: weight가 null이면 "?", 아니면 값 표시
                    Text(
                        text = if (weight == null) "?" else String.format("%.1fkg", weight),
                        fontSize = 13.sp,
                        color = MyPageColors.Grey500,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // [요구사항 반영 4] 막대 높이: weight가 null이면 랜덤, 아니면 계산된 높이
                    val barHeight = if (weight == null) {
                        randomHeights[index % randomHeights.size]
                    } else {
                        ((maxHeight.value * weight / maxWeight).roundToInt().dp).coerceAtLeast(1.dp)
                    }

                    // 막대 색상은 이번달 여부(isCurrentMonth)로 결정
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(barHeight)
                            .background(
                                color = if (isCurrentMonth) MyPageColors.Blue500 else MyPageColors.Grey200,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
        }

        // 구분선
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MyPageColors.Grey300
        )

        Spacer(modifier = Modifier.height(8.dp))

        // [요구사항 반영 5] 하단 라벨 Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            displayData.forEach { (label, _, _) ->
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = MyPageColors.Grey700,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}




