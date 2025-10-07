package com.example.pet_project_frontend.presentation.petcare.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.abs

@Composable
fun CalendarComponent(
    selectedDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit = {},
    onMonthChanged: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val coroutineScope = rememberCoroutineScope()
    
    // 연속된 날짜 생성 (오늘 기준 앞뒤로 60일씩, 총 4개월 정도)
    val dateRange = remember(today) {
        val startDate = today.minusDays(60)
        val endDate = today.plusDays(60)
        generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
            .toList()
    }
    
    // 현재 보이는 월 계산
    val listState = rememberLazyListState()
    var displayMonth by remember { mutableStateOf(today.withDayOfMonth(1)) }
    
    // 스크롤 위치를 기반으로 월 업데이트
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (dateRange.isNotEmpty()) {
            val centerIndex = listState.firstVisibleItemIndex + 3 // 가운데 날짜 추정
            val safeIndex = centerIndex.coerceIn(0, dateRange.size - 1)
            val centerDate = dateRange[safeIndex]
            val newMonth = centerDate.withDayOfMonth(1)
            
            if (newMonth != displayMonth) {
                displayMonth = newMonth
                onMonthChanged(newMonth)
            }
        }
    }
    
    // 초기 스크롤 위치 설정 (오늘 날짜가 중앙에 오도록)
    LaunchedEffect(Unit) {
        val todayIndex = dateRange.indexOfFirst { it == today }
        if (todayIndex >= 0) {
            // 오늘 날짜가 중앙에 오도록 스크롤 (앞에 3개 아이템이 보이도록)
            val targetIndex = maxOf(0, todayIndex - 3)
            listState.scrollToItem(targetIndex)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 월 표시 헤더 (동적으로 변경됨)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    // 이전 달의 중간 날짜로 스크롤
                    val prevMonth = displayMonth.minusMonths(1)
                    val targetDate = prevMonth.withDayOfMonth(15)
                    val targetIndex = dateRange.indexOfFirst { it == targetDate }
                    if (targetIndex >= 0) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(maxOf(0, targetIndex - 3))
                        }
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chevron_right),
                    contentDescription = "이전 달",
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = 180f) // 180도 회전으로 왼쪽 화살표
                )
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = displayMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월")),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MyPageColors.Grey800
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            IconButton(
                onClick = {
                    // 다음 달의 중간 날짜로 스크롤
                    val nextMonth = displayMonth.plusMonths(1)
                    val targetDate = nextMonth.withDayOfMonth(15)
                    val targetIndex = dateRange.indexOfFirst { it == targetDate }
                    if (targetIndex >= 0) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(maxOf(0, targetIndex - 3))
                        }
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chevron_right),
                    contentDescription = "다음 달",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // 무한 스크롤 날짜 목록
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(dateRange) { _, date ->
                DateItem(
                    date = date,
                    isSelected = date == selectedDate,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
private fun DateItem(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val isToday = date == today
    val isPastDate = date.isBefore(today)
    val isFutureDate = date.isAfter(today)
    
    // 날짜에 따른 색상 결정
    val textColor = when {
        isSelected -> MyPageColors.Grey800
        isToday -> MyPageColors.Grey600
        isPastDate -> MyPageColors.Grey600
        isFutureDate -> MyPageColors.Grey400
        else -> MyPageColors.Grey800
    }
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 요일
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 날짜 (선택된 날짜만 동그란 회색 배경)
        Box(
            modifier = Modifier
                .then(
                    if (isSelected) {
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MyPageColors.Grey300)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 16.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}