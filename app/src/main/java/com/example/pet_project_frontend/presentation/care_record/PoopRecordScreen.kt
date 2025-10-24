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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
 * 대변 기록 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoopRecordScreen(
    onBackClick: () -> Unit,
    viewModel: PoopRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedShape by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(Date())) }
    var showShapeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val shapeOptions = listOf("적당한 무르기", "딱딱한 토끼", "진흙", "죽이나 물", "점액 섞임", "대변 안 봄")
    val colorOptions = listOf("갈색", "검은색", "빨간색", "주황색", "회색", "노란색", "초록색")

    // "대변 안 봄" 선택 여부 확인
    val isNoPoopSelected by remember { derivedStateOf { selectedShape == "대변 안 봄" } }

    // "대변 안 봄"이 선택되면, 색상 선택을 초기화하는 로직
    LaunchedEffect(isNoPoopSelected) {
        if (isNoPoopSelected) {
            selectedColor = ""
        }
    }

    // 버튼 활성화 조건을 명확한 상태 변수로 관리
    val isConfirmButtonEnabled by remember {
        derivedStateOf {
            (!uiState.isLoading) && ((selectedShape.isNotEmpty() && selectedColor.isNotEmpty()) || isNoPoopSelected)
        }
    }

    val confirmButtonColor = if (isConfirmButtonEnabled) {
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
        uiState.error?.let {
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
    ) { innerPadding -> // <-- 1. innerPadding 파라미터 받기
        // Column과 Box가 화면 전체를 채우도록 Box로 한 번 감싸기
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // 메인 타이틀
                Text(
                    text = "대변을 기록해주세요",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey900
                )

                Spacer(modifier = Modifier.height(40.dp))

                // 형태 라벨
                Text(
                    text = "형태",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = MyPageColors.Grey800
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 형태 입력란
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showShapeDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedShape.isEmpty()) "대변의 형태를 알려주세요" else selectedShape,
                        fontSize = 22.sp,
                        color = if (selectedShape.isEmpty()) MyPageColors.Grey400 else MyPageColors.Grey900,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = "형태 선택",
                        tint = MyPageColors.Grey400
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 형태 입력 바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            color = if (selectedShape.isNotEmpty()) MyPageColors.Blue500 else MyPageColors.Grey100,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .clickable { showShapeDialog = true }
                )


                Spacer(modifier = Modifier.height(32.dp)) // 간격 조정

                // 색 라벨
                Text(
                    text = "색",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isNoPoopSelected) MyPageColors.Grey400 else MyPageColors.Grey800
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 색 입력란
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isNoPoopSelected) { showColorDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedColor.isEmpty() && !isNoPoopSelected) "대변의 색을 알려주세요" else selectedColor,
                        fontSize = 22.sp,
                        color = if (selectedColor.isEmpty() || isNoPoopSelected) MyPageColors.Grey400 else MyPageColors.Grey900,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = "색 선택",
                        tint = if (isNoPoopSelected) Color.Transparent else MyPageColors.Grey400
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 색 입력 바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            color = if (selectedColor.isNotEmpty() && !isNoPoopSelected) MyPageColors.Blue500 else MyPageColors.Grey100,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .clickable(enabled = !isNoPoopSelected) { showColorDialog = true }
                )

                Spacer(modifier = Modifier.height(32.dp)) // 간격 조정

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
            }

            // 확인 버튼을 화면 하단에 고정
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 10.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = {
                        if (isConfirmButtonEnabled) { // <-- 2. isConfirmButtonEnabled 사용
                            val finalColor = if (isNoPoopSelected) "" else selectedColor
                            viewModel.savePoopRecord(selectedShape, finalColor, selectedDate)
                        }
                    },
                    enabled = isConfirmButtonEnabled, // <-- 2. isConfirmButtonEnabled 사용
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = confirmButtonColor,
                        disabledContainerColor = MyPageColors.Blue200
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
    }

    // 형태 선택 다이얼로그
    if (showShapeDialog) {
        Dialog(onDismissRequest = { showShapeDialog = false }) {
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
                        .height(450.dp)
                        .offset {
                            IntOffset(
                                0,
                                maxOf(0f, dragOffset).toInt()
                            )
                        } // offset 수정
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) // 상단만 둥글게
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    if (dragOffset > 100f) { // 아래로 충분히 드래그하면 닫기
                                        showShapeDialog = false
                                    }
                                    dragOffset = 0f
                                }
                            ) { _, dragAmount ->
                                dragOffset += dragAmount.y
                            }
                        }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(4.dp)
                                .background(MyPageColors.Grey300, shape = RoundedCornerShape(2.dp))
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                text = "형태를 선택해주세요",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MyPageColors.Grey900
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            LazyColumn {
                                items(shapeOptions) { option ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedShape = option
                                                // "대변 안 봄" 선택 시 색상 초기화 로직은 LaunchedEffect가 담당
                                                showShapeDialog = false
                                            }
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = option, fontSize = 16.sp, color = MyPageColors.Grey800)
                                        if (selectedShape == option) {
                                            Image(
                                                painter = painterResource(id = R.drawable.check_blue),
                                                contentDescription = "선택됨",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    if (option != shapeOptions.last()) {
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

    // 색 선택 다이얼로그 (동일하게 드래그 및 UI 개선 적용)
    if (showColorDialog) {
        Dialog(onDismissRequest = { showColorDialog = false }) {
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
                        .height(500.dp)
                        .offset {
                            IntOffset(
                                0,
                                maxOf(0f, dragOffset).toInt()
                            )
                        }
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    if (dragOffset > 100f) {
                                        showColorDialog = false
                                    }
                                    dragOffset = 0f
                                }
                            ) { _, dragAmount ->
                                dragOffset += dragAmount.y
                            }
                        }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(4.dp)
                                .background(MyPageColors.Grey300, shape = RoundedCornerShape(2.dp))
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                text = "색을 선택해주세요",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MyPageColors.Grey900
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            LazyColumn {
                                items(colorOptions) { option ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedColor = option
                                                showColorDialog = false
                                            }
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = option, fontSize = 16.sp, color = MyPageColors.Grey800)
                                        if (selectedColor == option) {
                                            Image(
                                                painter = painterResource(id = R.drawable.check_blue),
                                                contentDescription = "선택됨",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    if (option != colorOptions.last()) {
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
