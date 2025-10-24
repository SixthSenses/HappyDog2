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
 * 사료 기록 화면 - 목표 사료량 입력
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedRecordScreen(
    onBackClick: () -> Unit,
    viewModel: FeedRecordViewModel = hiltViewModel()
) {
    var targetCount by remember { mutableStateOf("") }
    var isInputFocused by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 입력값 검증 - 10 이상일 때 에러 표시
    LaunchedEffect(targetCount) {
        val count = targetCount.toIntOrNull()
        showError = count != null && count > 10
    }
    
    // 성공 시 화면 닫기
    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            viewModel.clearSuccess()
            onBackClick()
        }
    }
    
    // 에러 메시지 표시
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // 에러 처리 (필요시 SnackBar 등으로 표시)
            viewModel.clearError()
        }
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
    
    val confirmButtonColor = if (targetCount.isNotEmpty() && !isInputFocused && !showError && !uiState.isLoading) {
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
                    text = "목표 섭취 횟수를 입력해주세요",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey900
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // 1일 목표 섭취 횟수 라벨
                Text(
                    text = "1일 목표 섭취 횟수",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = labelColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 입력값과 "회" 텍스트 또는 TextField
                if (isInputFocused) {
                    // 포커스된 상태 - TextField 표시
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = targetCount,
                            onValueChange = { newValue ->
                                // 숫자만 필터링 (최대 2자리까지만 입력받는 것이 좋음)
                                val filtered = newValue.filter { it.isDigit() }
                                if (filtered.length <= 2) {
                                    targetCount = filtered
                                }
                            },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
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
                        if (targetCount.isNotEmpty()) {
                            Text(
                                text = targetCount,
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
                        text = "10회 이하로 입력해주세요",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MyPageColors.Red600
                    )
                } else if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MyPageColors.Red600
                    )
                }
                
                // 여백 확보
                Spacer(modifier = Modifier.weight(1f))
                
                // 버튼을 위한 공간 (88dp = 56dp(버튼) + 32dp(하단 여백))
                Spacer(modifier = Modifier.height(if (isInputFocused) 56.dp else 88.dp))
                
                // 포커스 요청
                LaunchedEffect(isInputFocused) {
                    if (isInputFocused) {
                        focusRequester.requestFocus()
                    }
                }
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
                        if (targetCount.isNotEmpty() && !showError && !uiState.isLoading) {
                            val count = targetCount.toIntOrNull()
                            if (count != null && count > 0) {
                                viewModel.saveFeedGoal(count)
                            }
                        }
                    },
                    enabled = targetCount.isNotEmpty() && !showError && !uiState.isLoading,
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