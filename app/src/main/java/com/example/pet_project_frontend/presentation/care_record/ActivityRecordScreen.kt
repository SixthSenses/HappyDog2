package com.example.pet_project_frontend.presentation.care_record

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.core.theme.MyPageColors


/**
 * 활동 기록 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityRecordScreen(
    onBackClick: () -> Unit,
    viewModel: ActivityRecordViewModel = hiltViewModel()
) {
    // ViewModel 상태 수집
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var dailyTargetSessions by remember { mutableStateOf("") }  // 1일 목표 활동 횟수
    var perSessionTime by remember { mutableStateOf("") }  // 1회 활동 시간
    var isDailyInputFocused by remember { mutableStateOf(false) }
    var isSessionInputFocused by remember { mutableStateOf(false) }
    var showDailyError by remember { mutableStateOf(false) }
    var showSessionError by remember { mutableStateOf(false) }
    val dailyFocusRequester = remember { FocusRequester() }
    val sessionFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // 입력값 검증 - 7자리 이상일 때 에러 표시
    LaunchedEffect(dailyTargetSessions) {
        showDailyError = dailyTargetSessions.length >= 7
    }
    
    LaunchedEffect(perSessionTime) {
        showSessionError = perSessionTime.length >= 7
    }
    
    // 성공 시 뒤로 가기
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.clearSuccessState()
            onBackClick()
        }
    }
    
    // 에러 메시지 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            // TODO: 스낵바로 에러 메시지 표시
            viewModel.clearErrorMessage()
        }
    }

    // 1일 목표 활동 시간 색상 결정
    val dailyLabelColor = when {
        showDailyError -> MyPageColors.Red400
        isDailyInputFocused -> MyPageColors.Blue500
        else -> MyPageColors.Grey800
    }
    
    val dailyBarColor = when {
        showDailyError -> MyPageColors.Red400
        isDailyInputFocused -> MyPageColors.Blue500
        else -> MyPageColors.Grey100
    }
    
    // 1회 활동 시간 색상 결정
    val sessionLabelColor = when {
        showSessionError -> MyPageColors.Red400
        isSessionInputFocused -> MyPageColors.Blue500
        else -> MyPageColors.Grey800
    }
    
    val sessionBarColor = when {
        showSessionError -> MyPageColors.Red400
        isSessionInputFocused -> MyPageColors.Blue500
        else -> MyPageColors.Grey100
    }
    
    val confirmButtonColor = if (dailyTargetSessions.isNotEmpty() && perSessionTime.isNotEmpty() && 
                                 !isDailyInputFocused && !isSessionInputFocused && 
                                 !showDailyError && !showSessionError) {
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
                    text = "목표 활동량을 입력해주세요",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey900
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // 1일 목표 활동 횟수 라벨
                Text(
                    text = "1일 목표 활동 횟수",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = dailyLabelColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 1일 목표 활동 횟수 입력값과 "회" 텍스트 또는 TextField
                if (isDailyInputFocused) {
                    // 포커스된 상태 - TextField 표시
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = dailyTargetSessions,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { char -> char.isDigit() }
                                // 7자리까지 입력 허용
                                if (filtered.length <= 7) {
                                    dailyTargetSessions = filtered
                                }
                            },
                            modifier = Modifier
                                .focusRequester(dailyFocusRequester)
                                .weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    isDailyInputFocused = false
                                    focusManager.clearFocus()
                                }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MyPageColors.Grey900,
                                unfocusedTextColor = MyPageColors.Grey900
                            ),
                            textStyle = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "회",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = MyPageColors.Grey800
                        )
                    }
                } else {
                    // 포커스되지 않은 상태 - 텍스트만 표시
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dailyTargetSessions.isNotEmpty()) {
                            Text(
                                text = dailyTargetSessions,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MyPageColors.Grey900
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        
                        Text(
                            text = "회",
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
                            color = dailyBarColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .clickable {
                            isDailyInputFocused = true
                            isSessionInputFocused = false
                        }
                )
                
                // 1일 목표 활동 횟수 에러 메시지
                if (showDailyError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "너무 큰 숫자예요",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MyPageColors.Red600
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                // 1회 활동 시간 라벨
                Text(
                    text = "1회 활동 시간",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = sessionLabelColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 1회 활동 시간 입력값과 "분" 텍스트 또는 TextField
                if (isSessionInputFocused) {
                    // 포커스된 상태 - TextField 표시
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = perSessionTime,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { char -> char.isDigit() }
                                // 7자리까지 입력 허용
                                if (filtered.length <= 7) {
                                    perSessionTime = filtered
                                }
                            },
                            modifier = Modifier
                                .focusRequester(sessionFocusRequester)
                                .weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    isSessionInputFocused = false
                                    focusManager.clearFocus()
                                }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MyPageColors.Grey900,
                                unfocusedTextColor = MyPageColors.Grey900
                            ),
                            textStyle = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "분",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = MyPageColors.Grey800
                        )
                    }
                } else {
                    // 포커스되지 않은 상태 - 텍스트만 표시
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (perSessionTime.isNotEmpty()) {
                            Text(
                                text = perSessionTime,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MyPageColors.Grey900
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        
                        Text(
                            text = "분",
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
                            color = sessionBarColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .clickable {
                            isSessionInputFocused = true
                            isDailyInputFocused = false
                        }
                )
                
                // 1회 활동 시간 에러 메시지
                if (showSessionError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "너무 큰 숫자예요",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MyPageColors.Red600
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 여백 확보
                Spacer(modifier = Modifier.weight(1f))
                
                // 버튼을 위한 공간 (88dp = 56dp(버튼) + 32dp(하단 여백))
                Spacer(modifier = Modifier.height(if (isDailyInputFocused || isSessionInputFocused) 56.dp else 88.dp))
                
                // 포커스 요청
                LaunchedEffect(isDailyInputFocused) {
                    if (isDailyInputFocused) {
                        dailyFocusRequester.requestFocus()
                    }
                }
                
                LaunchedEffect(isSessionInputFocused) {
                    if (isSessionInputFocused) {
                        sessionFocusRequester.requestFocus()
                    }
                }
            }
            
            // 키보드 위 완료 버튼
            if (isDailyInputFocused || isSessionInputFocused) {
                Button(
                    onClick = {
                        isDailyInputFocused = false
                        isSessionInputFocused = false
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
                        if (dailyTargetSessions.isNotEmpty() && perSessionTime.isNotEmpty()) {
                            val dailySessions = dailyTargetSessions.toIntOrNull() ?: 0
                            val sessionMinutes = perSessionTime.toIntOrNull() ?: 0
                            viewModel.saveActivityGoal(sessionMinutes, dailySessions)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = confirmButtonColor
                    ),
                    enabled = !uiState.isLoading && dailyTargetSessions.isNotEmpty() && perSessionTime.isNotEmpty() && 
                             !showDailyError && !showSessionError
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