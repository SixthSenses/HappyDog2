package com.example.pet_project_frontend.presentation.mypage.profile.name

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pet_project_frontend.presentation.mypage.common.GestureBar
import com.example.pet_project_frontend.core.components.TopBar
import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.ui.platform.LocalView
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.pet_project_frontend.presentation.mypage.common.UiColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.DisposableEffect


@Composable
fun NameEditRoute(
    navController: NavController,
    viewModel: NameEditViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()

    val initial = if (ui.text == "이름") "" else ui.text

    NameEditScreen(
        initialName = initial,
        errorText = ui.error,
        isSaving = ui.isSaving,
        onBack = { navController.popBackStack() },
        onTextChange = viewModel::onTextChange,
        onClear = viewModel::onClear,
        onNext = {
            viewModel.onSave(onSuccess = { navController.popBackStack() })
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NameEditScreen(
    initialName: String,
    errorText: String?,
    isSaving: Boolean,
    onBack: () -> Unit,
    onTextChange: (String) -> Unit,
    onClear: () -> Unit,
    onNext: () -> Unit
) {
    // 디자인 컬러(시안 고정값)
    val Blue = Color(0xFF3182F6)
    val Blue25 = Color(0x403182F6) // 25% alpha
    val OnPrimary = Color(0xFFFFFFFF)
    val Headline = Color(0xFF191F28)
    val LabelDefault = Color(0xFF333D4B)
    val Placeholder = Color(0x8C333D4B) // 55% opacity
    val DividerDefault = Color(0x0D000000) // 5% black
    val ErrorRed = Color(0xFFD32F2F)

    var text by rememberSaveableState(initialName)
    var focused by remember { mutableStateOf(false) }

    // 유효성 (버튼 enable 및 에러 라인 색상 결정)
    val localError = remember(text) { validateLocal(text) }
    val error = errorText ?: localError
    val isNextEnabled = error == null && text.isNotBlank() && !isSaving
    val view = LocalView.current
    var isKeyboardVisible by remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val r = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(r)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - r.height()
            isKeyboardVisible = keypadHeight > screenHeight * 0.15f
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단바
        TopBar(title = {}, onNavigateBack = onBack)

        Spacer(Modifier.height(24.dp))

        // 입력 섹션
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .wrapContentHeight()
        ) {
            Text(
                text = "이름을 입력해주세요",
                color = Headline,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
                )
            Spacer(Modifier.height(24.dp))
            // 🔹라벨: "이름" (포커스 시 파란색)
            val labelColor =
                if (focused) Blue else Placeholder // 55% 회색
            Text(
                "이름",
                color = labelColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
                )
            Spacer(Modifier.height(8.dp))

            // 값(크게) + 플레이스홀더 + 삭제(X)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                BasicTextField(
                    value = text,
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
                        // ⬇️ 추가: 포커스 생길 때 "이름"이 실제 값으로 들어왔으면 즉시 비우기
                        if (now && text == "이름") {
                            text = ""
                            onTextChange("")
                        }
                        focused = now
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { if (isNextEnabled) onNext() }),
                    singleLine = true,
                ) { innerField ->
                    // ⬇️ 여기 주석 해제
                    if (text.isEmpty() && !focused) {
                        Text(
                            "이름",
                            color = Placeholder,          // 55% 투명 회색
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    innerField()
                }


                // 삭제 버튼
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

            // 구분선
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

            // 에러 문구(작고 빨강) - 2-4
            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text(error, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 하단 고정 버튼 (2-2 빈값시 반투명, 2-3 입력시 활성)
        if (isKeyboardVisible) {                 // ⬅️ 추가
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

        // 제스처 바 (디자인 맞춤)
        GestureBar(modifier = Modifier.fillMaxWidth())
    }
}

private fun validateLocal(input: String): String? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return "이름을 입력해 주세요."
    if (trimmed.length !in 1..12) return "이름은 1~12자까지 입력할 수 있어요."
    val ok = Regex("^[가-힣a-zA-Z0-9 ]+$").matches(trimmed)
    return if (ok) null else "한글, 영문, 숫자만 사용할 수 있어요."
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
