package com.example.pet_project_frontend.presentation.care_record

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * 구토 기록 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VomitRecordScreen(
    onBackClick: () -> Unit,
    viewModel: VomitRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var selectedOptions by remember { mutableStateOf(setOf<String>()) }
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(Date())) }
    var showVomitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val vomitOptions = listOf("노란색", "흰색", "거품 섞임", "갈색", "검은색", "초록색", "붉은색", "분홍색")
    
    val confirmButtonColor = if (selectedOptions.isNotEmpty()) {
        MyPageColors.Blue500
    } else {
        MyPageColors.Blue200
    }
    
    // 성공/실패 상태 처리
    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            viewModel.clearSuccess()
            onBackClick()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // TODO: 에러 토스트 메시지 표시
            viewModel.clearError()
        }
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
                    text = "구토를 기록해주세요",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey900
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // 형태 및 색 라벨
                Text(
                    text = "형태 및 색",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = MyPageColors.Grey800
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 형태 및 색 입력란
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showVomitDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedOptions.isEmpty()) "구토의 형태 및 색을 알려주세요" else selectedOptions.joinToString(", "),
                        fontSize = 22.sp,
                        color = if (selectedOptions.isEmpty()) MyPageColors.Grey400 else MyPageColors.Grey900,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = "구토 선택",
                        tint = MyPageColors.Grey400
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 구토 입력 바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            color = if (selectedOptions.isNotEmpty()) MyPageColors.Blue500 else MyPageColors.Grey100,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .clickable { showVomitDialog = true }
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // 날짜 선택
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    selectedDate = "${year}년 ${month + 1}월 ${dayOfMonth}일"
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
                Spacer(modifier = Modifier.height(88.dp))
            }
            
            // 확인 버튼 (화면 하단)
            Button(
                onClick = {
                    if (selectedOptions.isNotEmpty()) {
                        viewModel.saveVomitRecord(selectedOptions.toList(), selectedDate)
                    }
                },
                enabled = selectedOptions.isNotEmpty() && !uiState.isLoading,
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
                        color = Color.White
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
    
    // 구토 형태 및 색 선택 다이얼로그 (다중 선택)
    if (showVomitDialog) {
        Dialog(onDismissRequest = { showVomitDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                var dragOffset by remember { mutableFloatStateOf(0f) }
                
                Box(
                    modifier = Modifier
                        .width(420.dp)
                        .height(570.dp)
                        .offset(y = maxOf(0f, dragOffset).dp)
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 회색 바 (드래그 핸들)
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(4.dp)
                                .background(
                                    MyPageColors.Grey300,
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .align(Alignment.CenterHorizontally)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            if (dragOffset > 100f) {
                                                showVomitDialog = false
                                            }
                                            dragOffset = 0f
                                        }
                                    ) { _, dragAmount ->
                                        dragOffset += dragAmount.y
                                    }
                                }
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "형태 및 색을 선택해주세요",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MyPageColors.Grey900
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "선택지를 여러 개 고를 수 있어요",
                                fontSize = 16.sp,
                                color = MyPageColors.Grey700
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            LazyColumn {
                                items(vomitOptions) { option ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedOptions = if (selectedOptions.contains(option)) {
                                                    selectedOptions - option
                                                } else {
                                                    selectedOptions + option
                                                }
                                            }
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 16.sp,
                                    color = MyPageColors.Grey800
                                )
                                
                                if (selectedOptions.contains(option)) {
                                    Image(
                                        painter = painterResource(id = R.drawable.check_blue),
                                        contentDescription = "선택됨",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            if (option != vomitOptions.last()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                        }
                    }
                }
            }
        }
    }
}