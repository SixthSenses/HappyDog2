package com.example.pet_project_frontend.presentation.mypage.profile.birth

import android.view.ViewTreeObserver
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pet_project_frontend.core.components.TopBar
import com.example.pet_project_frontend.presentation.mypage.common.GestureBar

@Composable
fun BirthEditRoute(
    navController: NavController,
    viewModel: BirthEditViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()

    // "연/월/일"이 값으로 들어오면 빈값으로 치환
    val initial = if (ui.text == "연/월/일") "" else ui.text

    BirthEditScreen(
        initialBirth = initial,
        errorText = ui.error,
        isSaving = ui.isSaving,
        onBack = { navController.popBackStack() },
        onTextChange = viewModel::onTextChange,
        onClear = viewModel::onClear,
        onNext = { viewModel.onSave(onSuccess = { navController.popBackStack() }) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthEditScreen(
    initialBirth: String,
    errorText: String?,
    isSaving: Boolean,
    onBack: () -> Unit,
    onTextChange: (String) -> Unit,
    onClear: () -> Unit,
    onNext: () -> Unit
) {
    // 디자인 컬러
    val Blue = Color(0xFF3182F6)
    val Blue25 = Color(0x403182F6)
    val OnPrimary = Color(0xFFFFFFFF)
    val Headline = Color(0xFF191F28)
    val LabelDefault = Color(0xFF333D4B)
    val Placeholder = Color(0x8C333D4B) // 55%
    val DividerDefault = Color(0x0D000000)
    val ErrorRed = Color(0xFFD32F2F)

    var text by rememberSaveableState(initialBirth)
    var focused by remember { mutableStateOf(false) }

    // 키보드(IME) 가시성
    val view = LocalView.current
    var isKeyboardVisible by remember { mutableStateOf(false) }
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val r = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(r)
            val screen = view.rootView.height
            isKeyboardVisible = (screen - r.height()) > screen * 0.15f
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }

    // 유효성
    val localError = remember(text) { validateBirth(text) }
    val error = errorText ?: localError
    val isNextEnabled = error == null && text.isNotBlank() && !isSaving

    Column(Modifier.fillMaxSize()) {
        TopBar(title = {}, onNavigateBack = onBack)

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .wrapContentHeight()
        ) {
            Text(
                text = "반려견의 생년월일을 입력해주세요",
                color = Headline,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(24.dp))

            val labelColor = if (focused) Blue else Placeholder
            Text(
                "생년월일",
                color = labelColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = if (text == "연/월/일") "" else text,
                    onValueChange = {
                        text = it
                        onTextChange(it)
                    },
                    textStyle = TextStyle(
                        color = LabelDefault,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { state ->
                            val now = state.isFocused
                            if (now && text == "연/월/일") {
                                text = ""
                                onTextChange("")
                            }
                            focused = now
                        },
                    // 숫자+구분자 입력을 위해 Text 사용 (Number는 슬래시 입력 제한)
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isNextEnabled) onNext() }
                    ),
                    singleLine = true
                ) { inner ->
                    // 값이 비어 있고 포커스가 아닐 때만 회색 플레이스홀더 표시
                    val showPlaceholder = text.isEmpty() && !focused
                    if (showPlaceholder) {
                        Text(
                            text = "연/월/일",
                            color = Placeholder,          // ← 회색(투명) 고정
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    inner()
                }

                if (text.isNotEmpty()) {
                    Text(
                        "✕",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { onClear(); text = "" },
                        color = Color(0xFFB1B8C0),
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        when {
                            error != null -> ErrorRed
                            focused -> Blue
                            else -> DividerDefault
                        }
                    )
            )

            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text(error, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.weight(1f))

        // 키보드가 올라왔을 때만 '다음' 표시
        if (isKeyboardVisible) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(if (isNextEnabled) Blue else Blue25)
                        .clickable(enabled = isNextEnabled) { onNext() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("다음", color = OnPrimary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                }
            }
        }

        GestureBar(modifier = Modifier.fillMaxWidth())
    }
}

/** rememberSaveable 대신 값 주입 시 첫 렌더 한 번만 초기화 */
@Composable
private fun rememberSaveableState(initial: String): MutableState<String> {
    val state = remember { mutableStateOf(initial) }
    LaunchedEffect(initial) {
        if (state.value.isEmpty() && initial.isNotEmpty()) state.value = initial
    }
    return state
}

private fun validateBirth(input: String): String? {
    val t = input.trim()
    if (t.isEmpty()) return "생년월일을 입력해 주세요."
    // 형식: YYYY/MM/DD
    val regex = Regex("""^\d{4}/\d{1,2}/\d{1,2}$""")
    if (!regex.matches(t)) return "YYYY/MM/DD 형식으로 입력해 주세요."

    val parts = t.split("/")
    val y = parts[0].toInt()
    val m = parts[1].toInt()
    val d = parts[2].toInt()
    if (y !in 1900..2100) return "연도 값을 확인해 주세요."
    if (m !in 1..12) return "월은 1~12 사이여야 해요."
    val maxDay = when (m) {
        1,3,5,7,8,10,12 -> 31
        4,6,9,11 -> 30
        2 -> if ((y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)) 29 else 28
        else -> 31
    }
    if (d !in 1..maxDay) return "유효한 날짜가 아니에요."
    return null
}
