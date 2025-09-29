package com.example.pet_project_frontend.presentation.petregistration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.navigation.Screen // [수정됨] Screen import
import com.example.pet_project_frontend.domain.model.Gender
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectField(
    label: String,
    categories: List<String>,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    dialogTitle: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 13.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight(500),
                color = if (isError) Color(0xFFF04452) else Color(0xFF333D4B)
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = selectedCategory,
                onValueChange = { },
                placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
                singleLine = true,
                isError = isError,
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrowdown), // .xml 파일
                            contentDescription = label,
                            tint = Color(0xFFB0B8C1), // 색상도 조절 가능
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFFE5E8EB),
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF333D4B),
                    unfocusedTextColor = Color(0xFF333D4B),
                    errorTextColor = Color(0xFF333D4B),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB),
                    errorContainerColor = Color(0xFFFFEEEE),
                    cursorColor = Color(0xFF426BF2),
                    errorCursorColor = Color(0xFFE42A38),
                    selectionColors = TextSelectionColors(
                        handleColor = Color(0xFF3182F6),
                        backgroundColor = Color(0x1A001B37)
                    ),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = Color(0xFF8B95A1),
                    unfocusedPlaceholderColor = Color(0xFF8B95A1),
                    errorPlaceholderColor = Color.Transparent
                )
            )

            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight(500),
                        color = Color(0xFFF04452)
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }

    if (showDialog) {
        ResizableSelectionDialog(
            title = dialogTitle,
            items = categories,
            selectedItem = selectedCategory,
            onItemSelected = { category ->
                selectedCategory = category
                onValueChange(category)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryMultiSelectField(
    label: String,
    categories: List<String>,
    onValueChange: (Set<String>) -> Unit,
    placeholder: String = "",
    dialogTitle: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 13.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight(500),
                color = if (isError) Color(0xFFF04452) else Color(0xFF333D4B)
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = selectedCategories.joinToString(", "),
                onValueChange = { },
                placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
                singleLine = true,
                isError = isError,
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrowdown), // .xml 파일
                            contentDescription = label,
                            tint = Color(0xFFB0B8C1), // 색상도 조절 가능
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFFE5E8EB),
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF333D4B),
                    unfocusedTextColor = Color(0xFF333D4B),
                    errorTextColor = Color(0xFF333D4B),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB),
                    errorContainerColor = Color(0xFFFFEEEE),
                    cursorColor = Color(0xFF426BF2),
                    errorCursorColor = Color(0xFFE42A38),
                    selectionColors = TextSelectionColors(
                        handleColor = Color(0xFF3182F6),
                        backgroundColor = Color(0x1A001B37)
                    ),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = Color(0xFF8B95A1),
                    unfocusedPlaceholderColor = Color(0xFF8B95A1),
                    errorPlaceholderColor = Color.Transparent
                )
            )

            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight(500),
                        color = Color(0xFFF04452)
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }

    if (showDialog) {
        // 다중 선택 다이얼로그 가정: 리스트 항목마다 선택 체크박스 등
        ResizableMultiSelectionDialog(
            title = dialogTitle,
            items = categories,
            selectedItems = selectedCategories,
            onSelectionChange = { newSelectedSet ->
                selectedCategories = newSelectedSet
                onValueChange(newSelectedSet)
            },
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResizableSelectionDialog(
    title: String,
    items: List<String>,
    selectedItem: String = "",
    onItemSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ModalBottomSheet(
        modifier = Modifier
            .padding(horizontal = 10.dp),
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = {
            // 상단 드래그 핸들
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(4.dp)
                        .background(
                            Color(0xFFE5E8EB),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        },
        containerColor = Color.White,
        windowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 600.dp)
        ) {
            // 헤더
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 14.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // 항목 리스트
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 1.dp)
            ) {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(item) }
                            .padding(horizontal = 25.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 0.24.em,
                            letterSpacing = (-0.01).em,
                            color = Color(0xFF4E5968),
                            modifier = Modifier.weight(1f)
                        )

                        if (item == selectedItem) {
                            // SVG 사용
                            Icon(
                                painter = painterResource(id = R.drawable.ic_checkmark), // .xml 파일
                                contentDescription = "선택됨",
                                tint = Color(0xFF3182F6), // 색상도 조절 가능
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 2.dp)
                            )
                        }
                    }
                }
            }
            // 하단 여백
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResizableMultiSelectionDialog(
    title: String,
    items: List<String>,
    selectedItems: Set<String> = emptySet(),
    onSelectionChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ModalBottomSheet(
        modifier = Modifier
            .padding(horizontal = 10.dp),
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(4.dp)
                        .background(
                            Color(0xFFE5E8EB),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        },
        containerColor = Color.White,
        windowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 600.dp)
        ) {
            // 헤더
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 14.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // 항목 리스트
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 1.dp)
            ) {
                items(items) { item ->
                    val isSelected = selectedItems.contains(item)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newSelected = selectedItems.toMutableSet()
                                if (isSelected) newSelected.remove(item)
                                else newSelected.add(item)
                                onSelectionChange(newSelected)
                            }
                            .padding(horizontal = 25.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 0.24.em,
                            letterSpacing = (-0.01).em,
                            color = Color(0xFF4E5968),
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_checkmark),
                                contentDescription = "선택됨",
                                tint = Color(0xFF3182F6),
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 2.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetRegistrationScreen(
    navController: NavController,
    viewModel: PetRegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 등록 성공 시 펫케어 화면으로 이동
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // [수정됨] NavigationRoutes -> Screen.route 로 변경
            navController.navigate(Screen.PetCare.route) {
                popUpTo(Screen.PetRegistration.route) { inclusive = true }
            }
        }
    }

    Scaffold() { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(21.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                item {
                    Text(
                        text = "회원가입",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3182F6),
                        lineHeight = 1.40.em,
                        letterSpacing = (-0.015).em,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "반려견의 정보를\n알려주세요",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 1.40.em,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                item {
                    val isError = uiState.error?.contains("이름") == true
                    LabeledTextField(
                        label = "이름",
                        value = viewModel.petName,
                        onValueChange = viewModel::updatePetName,
                        placeholder = "반려견의 이름을 알려주세요",
                        isError = isError,
                        errorMessage = uiState.error
                    )
                }
                // 성별 선택
                item {
                    val isError = uiState.error?.contains("성별") == true
                    val categories = listOf(
                        "수컷", "암컷"
                    )
                    CategorySelectField(
                        label = "성별",
                        categories = categories,
                        onValueChange = viewModel::updateGender,
                        placeholder = "반려견의 성별을 알려주세요",
                        dialogTitle = "성별을 선택해주세요",
                        isError = isError,
                        errorMessage = uiState.error
                    )
                }

                // 품종 선택
                item {
                    val isError = uiState.error?.contains("견종") == true
                    val categories = listOf(
                        "가나안 독", "그레이트 데인", "그레이트 피레니즈", "그레이하운드", "그린란드견", "골든 리트리버", "고든 세터", "글렌 오브 이말 테리어", "그리폰 브뤼셀루아", "그리폰 벨주", "꼬똥 드 뚤레아",
                        "뉴기니 싱잉 독", "뉴펀들랜드", "노르웨이 엘크하운드", "노르웨이 룬데훈드", "노바 스코시아 덕 톨링 리트리버", "노리치 테리어", "노르폴크 테리어",
                        "닥스훈트", "달마시안", "댄디 딘몬트 테리어", "도베르만", "동경이", "도사", "드로버스 캐틀 독",
                        "래브라도 리트리버", "라이카", "로트바일러", "로디시안 리지백", "러프 콜리", "레이크랜드 테리어", "라플란드 헤더", "레오베르거",
                        "말라뮤트", "말티즈", "마스티프", "맨체스터 테리어", "미니어처 불 테리어", "미니어처 푸들", "미니어처 핀셔", "미니어처 슈나우저", "무디",
                        "보더 콜리", "복서", "불독", "불테리어", "불마스티프", "비글", "비숑 프리제", "보스턴 테리어", "버니즈 마운틴 독", "바셋 하운드", "브리타니 스패니얼", "보르조이", "보비에 데 플랑드르", "비어디드 콜리", "바르베",
                        "시베리안 허스키", "시츄", "스피츠", "셰퍼드", "사모예드", "세인트 버나드", "살루키", "삽살개", "스코티시 테리어", "셔틀랜드 쉽독", "스태퍼드셔 불 테리어", "스프링거 스패니얼", "스무스 폭스 테리어", "소프트 코티드 휘튼 테리어", "시바견",
                        "아키타", "아프간 하운드", "아이리시 세터", "아이리시 울프하운드", "아메리칸 코커 스패니얼", "아메리칸 불독", "아메리칸 스태퍼드셔 테리어", "잉글리시 불독", "잉글리시 세터", "잉글리시 스프링거 스패니얼", "올드 잉글리시 쉽독", "웰시 코기", "와이마라너", "웨스트 하이랜드 화이트 테리어", "와이어 폭스 테리어",
                        "진돗개", "재패니즈 스피츠", "잭 러셀 테리어", "저먼 쇼트헤어드 포인터", "저먼 와이어헤어드 포인터",
                        "치와와", "차우차우", "체사피크 베이 리트리버", "차이니즈 크레스티드", "체스키 테리어",
                        "코커 스패니얼", "콜리", "케언 테리어", "킹 찰스 스패니얼", "케리 블루 테리어", "코몬도르", "키스훈드",
                        "토이 푸들", "토이 맨체스터 테리어", "티베탄 마스티프", "티베탄 테리어", "토이 폭스 테리어",
                        "푸들", "포메라니안", "퍼그", "페키니즈", "프렌치 불독", "풍산개", "포인터", "파라오 하운드", "핏 불 테리어", "파슨 러셀 테리어", "펨브로크 웰시 코기", "포르투갈 워터 독", "푸미",
                        "허스키", "헝가리안 비즐라", "하바니즈", "하운드", "하바네제", "하리어"
                    )
                    CategorySelectField(
                        label = "견종",
                        categories = categories,
                        onValueChange = viewModel::selectBreed,
                        placeholder = "반려견의 견종을 알려주세요",
                        dialogTitle = "견종을 선택해주세요",
                        isError = isError,
                        errorMessage = uiState.error
                    )
                }

                // 생년월일 선택
                item {
                    var birthDateWithDot by remember { mutableStateOf("") }
                    val isError = uiState.error?.contains("생년월일") == true
                    fun updateBirthDateWithDot(
                        updateBirthDate: (LocalDate?) -> Unit
                    ): (String) -> String = { rawInput ->
                        val digits = rawInput.filter { it.isDigit() }

                        val formatted = buildString {
                            for (i in digits.indices) {
                                append(digits[i])
                                if (i == 3 || i == 5) append('.')
                            }
                        }.take(10)

                        val parsedDate: LocalDate? = if (digits.isEmpty()) {
                            null  // 입력이 없으면 null 반환
                        } else if (digits.length >= 8) {
                            try {
                                LocalDate.parse(formatted.substring(0, 10), DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                            } catch (e: DateTimeParseException) {
                                LocalDate.now().plusDays(1)  // 변환 실패 시 미래 날짜 반환
                            }
                        } else {
                            LocalDate.now().plusDays(1)  // 8자리 미만일 때도 미래 날짜 반환
                        }

                        updateBirthDate(parsedDate)
                        formatted
                    }

                    LabeledTextField(
                        label = "생년월일",
                        value = birthDateWithDot,
                        onValueChange = {
                            birthDateWithDot = updateBirthDateWithDot(viewModel::updateBirthDate)(it)
                        },
                        placeholder = "반려견의 생일을 알려주세요",
                        isError = isError,
                        isNumberType = true,
                        errorMessage = uiState.error
                    )
                }

                item {
                    val isError = uiState.error?.contains("체중") == true
                    LabeledTextField(
                        label = "체중",
                        value = viewModel.weight,
                        onValueChange = viewModel::updateWeight,
                        placeholder = "반려견의 체중을 알려주세요",
                        isError = isError,
                        isNumberType = true,
                        errorMessage = uiState.error
                    )
                }

                // 털 색상 (선택)
                item {
                    val isError = uiState.error?.contains("털 색상") == true
                    val categories = listOf(
                        "흰색", "금색", "황색", "회색", "검은색", "흰색 + 금색", "흰색 + 황색", "흰색 + 회색", "흰색 + 검은색", "검은색 + 금색"
                    )
                    CategorySelectField(
                        label = "털 색상",
                        categories = categories,
                        onValueChange = viewModel::updateFurColor,
                        placeholder = "반려견의 털 색상을 알려주세요",
                        dialogTitle = "털 색상을 선택해주세요",
                        isError = isError,
                        errorMessage = uiState.error
                    )
                }

                // 건강 관심사 (선택)
                item {
                    val isError = uiState.error?.contains("건강 관심사") == true
                    val categories = listOf(
                        "눈", "뼈/관절", "피부/피모", "치아/구강", "노화", "비만", "변비", "심장", "영양", "신장/요로", "면역력", "구토"
                    )
                    CategoryMultiSelectField(
                        label = "건강 관심사",
                        categories = categories,
                        onValueChange = viewModel::updateHealthConcerns,
                        placeholder = "지금 가장 관심있는 내용을 알려주세요",
                        dialogTitle = "건강 관심사를 선택해주세요",
                        isError = isError,
                        errorMessage = uiState.error
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(92.dp))
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(
                            brush = verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0f),
                                    Color.White,
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(78.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.registerPet() },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .padding(horizontal = 21.dp),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3182F6),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "완료",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthConcernsSection(
    healthConcerns: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Column {
        Text(
            text = "건강 관심사 (선택)",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("예: 알러지, 관절염") },
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onAdd(inputText)
                        inputText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "추가")
            }
        }

        if (healthConcerns.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                healthConcerns.forEach { concern ->
                    InputChip(
                        selected = false,
                        onClick = { /* 선택 기능 없음 */ },
                        label = { Text(concern) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onRemove(concern) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "삭제")
                            }
                        }
                    )
                }
            }
        }
    }
}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun BreedSelectionDialog(
//    viewModel: PetRegistrationViewModel,
//    onDismiss: () -> Unit
//) {
//    val searchQuery by viewModel.breedSearchQuery.collectAsState()
//    val searchResults by viewModel.breedSearchResults.collectAsState()
//
//    BasicAlertDialog(
//        onDismissRequest = onDismiss,
//        modifier = Modifier.fillMaxHeight(0.8f),
//        properties = DialogProperties(), content = {
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(16.dp)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                ) {
//                    Text(
//                        text = "품종 선택",
//                        style = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    OutlinedTextField(
//                        value = searchQuery,
//                        onValueChange = viewModel::updateBreedSearchQuery,
//                        label = { Text("품종 검색") },
//                        modifier = Modifier.fillMaxWidth(),
//                        leadingIcon = {
//                            Icon(Icons.Default.Search, contentDescription = "검색")
//                        },
//                        singleLine = true
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f),
//                        verticalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        items(searchResults) { breed ->
//                            Card(
//                                onClick = { viewModel.selectBreed(breed) },
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(16.dp),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        text = breed.breedName,
//                                        style = MaterialTheme.typography.bodyLarge
//                                    )
//                                    Text(
//                                        text = "평균 수명: ${breed.lifeExpectancy}년",
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                                    )
//                                }
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    TextButton(
//                        onClick = onDismiss,
//                        modifier = Modifier.align(Alignment.End)
//                    ) {
//                        Text("취소")
//                    }
//                }
//            }
//        })
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    isError: Boolean = false,
    isNumberType: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 13.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight(500),
                color = if (isError) Color(0xFFF04452) else Color(0xFF333D4B)
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { placeholder?.let { Text(it) }},
                singleLine = singleLine,
                isError = isError,
                keyboardOptions = if (isNumberType) {
                    KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                } else {
                    KeyboardOptions.Default
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFFE5E8EB),
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF333D4B),
                    unfocusedTextColor = Color(0xFF333D4B),
                    errorTextColor = Color(0xFF333D4B),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB),
                    errorContainerColor = Color(0xFFFFEEEE),
                    cursorColor = Color(0xFF426BF2),
                    errorCursorColor = Color(0xFFE42A38),
                    selectionColors = TextSelectionColors(
                        handleColor = Color(0xFF3182F6),
                        backgroundColor = Color(0x1A001B37)
                    ),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = Color(0xFF8B95A1),
                    unfocusedPlaceholderColor = Color(0xFF8B95A1),
                    errorPlaceholderColor = Color.Transparent
                )
            )

            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight(500),
                        color = Color(0xFFF04452)
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
