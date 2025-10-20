package com.example.pet_project_frontend.presentation.mypage.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.core.theme.PretendardFont
import com.example.pet_project_frontend.presentation.mypage.main.MyPageViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBirthdateScreen(
    onDismiss: () -> Unit,
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 현재 생년월일을 파싱 (yyyy.MM.dd → yyyy-MM-dd)
    val currentBirthDate = remember(uiState.birthDate) {
        try {
            if (uiState.birthDate.isNotBlank()) {
                LocalDate.parse(uiState.birthDate, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            } else {
                LocalDate.now()
            }
        } catch (e: Exception) {
            LocalDate.now()
        }
    }
    
    // DatePicker 상태 초기화
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentBirthDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(48.dp)
                    .background(
                        color = Color(0xFF3182F6),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        // 선택된 날짜를 yyyy-MM-dd 형식으로 변환
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val formattedDate = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            viewModel.updateBirthDate(formattedDate)
                        }
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "저장",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(600),
                    fontSize = 16.sp,
                    color = Color(0xFF333D4B)
                )
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(48.dp)
                    .background(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "취소",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(600),
                    fontSize = 16.sp,
                    color = Color(0xFF4E5968)
                )
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = Color.White
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = Color(0xFF3182F6),
                todayContentColor = Color(0xFF3182F6),
                todayDateBorderColor = Color(0xFF3182F6),
                selectedDayContentColor = Color.White
            ),
            title = {
                Text(
                    text = "생년월일 선택",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(600),
                    fontSize = 20.sp,
                    color = Color(0xFF333D4B),
                    modifier = Modifier.padding(start = 24.dp, top = 20.dp)
                )
            }
        )
    }
}
