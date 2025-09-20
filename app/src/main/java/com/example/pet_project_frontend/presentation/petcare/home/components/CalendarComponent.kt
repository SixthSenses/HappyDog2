package com.example.pet_project_frontend.presentation.petcare.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarComponent(
    selectedDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit = {},
    onMonthChanged: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(selectedDate.withDayOfMonth(1)) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 월 표시 헤더 (2025년 9월, 좌우 화살표)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    currentMonth = currentMonth.minusMonths(1)
                    onMonthChanged(currentMonth)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "이전 달",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월")),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(
                onClick = {
                    currentMonth = currentMonth.plusMonths(1)
                    onMonthChanged(currentMonth)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "다음 달",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // 수평 스크롤 날짜 목록
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            val daysInMonth = currentMonth.lengthOfMonth()
            val dates = (1..daysInMonth).map { currentMonth.withDayOfMonth(it) }
            
            items(dates) { date ->
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
    val isToday = date == LocalDate.now()
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 요일
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
            fontSize = 12.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 날짜
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 16.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                   else if (isToday) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurface
        )
        
        // 오늘 표시 점
        if (isToday && !isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}