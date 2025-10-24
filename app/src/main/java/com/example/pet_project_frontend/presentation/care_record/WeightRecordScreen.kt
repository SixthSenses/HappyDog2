package com.example.pet_project_frontend.presentation.care_record

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.presentation.care_management.WeightManagementViewModel
import java.util.Calendar

/**
 * 몸무게 기록 화면 (날짜 선택 + 몸무게 입력)
 * WeightManagementScreen의 '기록하기' 버튼으로 이동
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightLogScreen(
    onBackClick: () -> Unit,
    viewModel: WeightManagementViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var currentWeight by remember { mutableStateOf("") }
    var isInputFocused by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var selectedDate by remember { 
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        mutableStateOf("${year}년 ${month}월 ${day}일")
    }
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 저장 성공시 화면 닫기
    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            viewModel.clearSuccess()
            onBackClick()
        }
    }
    
    // 에러 메시지 표시
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 에러 스낵바 표시 등의 처리
            viewModel.clearError()
        }
    }
    
    // 입력값 검증 - 7자리 이상일 때 에러 표시
    LaunchedEffect(currentWeight) {
        showError = currentWeight.length >= 7
    }

    // 색상 결정
    val labelColor = when {
        showError -> MyPageColors.Red400
        isInputFocused -> MyPageColors.Blue500
        else -> MyPageColors.Grey800
    }
    
    val barColor = when {
        showError -> MyPageColors.Red400
        isInputFocused -> MyPageColors.Blue500
        else -> MyPageColors.Grey100
    }
    
    val confirmButtonColor = if (currentWeight.isNotEmpty() && !isInputFocused && !showError) {
        MyPageColors.Blue500
    } else {
        MyPageColors.Blue200
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = MyPageColors.Grey900
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // 메인 타이틀
                Text(
                    text = "몸무게를 기록해주세요",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey900
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 몸무게 라벨
                Text(
                    text = "몸무게",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = labelColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 입력 영역
                if (isInputFocused) {
                    // 포커스된 상태: TextField
                    TextField(
                        value = currentWeight,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { char -> char.isDigit() || char == '.' }
                            // 삭제는 항상 허용, 7자리 이상 새 입력만 방지
                            if (filtered.length <= currentWeight.length || filtered.length <= 6) {
                                currentWeight = filtered
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                isInputFocused = false
                                focusManager.clearFocus()
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MyPageColors.Grey900,
                            textAlign = TextAlign.End
                        ),
                        suffix = {
                            Text(
                                text = "kg",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Normal,
                                color = MyPageColors.Grey800
                            )
                        }
                    )
                    
                    // 포커스 요청
                    LaunchedEffect(isInputFocused) {
                        if (isInputFocused) {
                            focusRequester.requestFocus()
                        }
                    }
                } else {
                    // 비포커스 상태: 숫자 표시와 단위
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentWeight.isNotEmpty()) {
                            Text(
                                text = currentWeight,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MyPageColors.Grey900
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        
                        Text(
                            text = "kg",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = MyPageColors.Grey800
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 입력 바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            color = barColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .clickable {
                            isInputFocused = true
                        }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 에러 메시지
                if (showError) {
                    Text(
                        text = "너무 큰 숫자예요",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MyPageColors.Red600
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 날짜 선택
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = selectedCalendar
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    selectedDate = "${year}년 ${month + 1}월 ${dayOfMonth}일"
                                    selectedCalendar = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    }
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "날짜",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = MyPageColors.Grey700
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedDate,
                            fontSize = 16.sp,
                            color = MyPageColors.Blue500
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Image(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = "날짜 선택",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // 여백 확보
                Spacer(modifier = Modifier.weight(1f))
                
                // 버튼을 위한 공간
                Spacer(modifier = Modifier.height(if (isInputFocused) 56.dp else 88.dp))
            }
            
            // 키보드 위 완료 버튼
            if (isInputFocused) {
                Button(
                    onClick = {
                        isInputFocused = false
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 0.dp)
                        .padding(bottom = 0.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyPageColors.Blue200
                    )
                ) {
                    Text(
                        text = "완료",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            } else {
                // 확인 버튼 (화면 하단)
                Button(
                    onClick = {
                        if (currentWeight.isNotEmpty() && !showError) {
                            currentWeight.toFloatOrNull()?.let { weight ->
                                // Calendar를 LocalDate로 변환
                                val year = selectedCalendar.get(Calendar.YEAR)
                                val month = selectedCalendar.get(Calendar.MONTH) + 1
                                val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)
                                val localDate = java.time.LocalDate.of(year, month, day)
                                viewModel.saveWeightRecord(weight, localDate)
                            }
                        }
                    },
                    enabled = currentWeight.isNotEmpty() && !showError && !uiState.isLoading,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = confirmButtonColor
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "확인",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
