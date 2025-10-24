// 변경의도: TextFieldValue를 사용해 자동 포맷 후에도 커서가 끝에 머물도록 생년월일 입력 UI를 보완한다.
package com.example.pet_project_frontend.presentation.mypage.profile.birthdate

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pet_project_frontend.core.components.TopBar
import com.example.pet_project_frontend.core.navigation.Screen
import com.example.pet_project_frontend.presentation.mypage.main.MyPageViewModel

@Composable
fun BirthEditRoute(
    navController: NavController,
    viewModel: BirthEditViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val parentEntry = remember(navController) { navController.getBackStackEntry(Screen.MyPage.route) }
    val myPageViewModel: MyPageViewModel = hiltViewModel(parentEntry)

    BirthEditScreen(
        uiState = ui,
        onBack = { navController.popBackStack() },
        onTextChange = viewModel::onTextChange,
        onClear = viewModel::onClear,
        onSave = {
            viewModel.onSave { savedBirth, shouldReload ->
                myPageViewModel.updateBirthDate(savedBirth)
                if (shouldReload) {
                    myPageViewModel.loadUserData()
                }
                navController.popBackStack()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthEditScreen(
    uiState: BirthEditUiState,
    onBack: () -> Unit,
    onTextChange: (TextFieldValue) -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()
    var focused by remember { mutableStateOf(false) }
    val view = LocalView.current
    var isKeyboardVisible by remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.height()
            isKeyboardVisible = keypadHeight > screenHeight * 0.15f
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        TopBar(title = {}, onNavigateBack = onBack)

        Spacer(modifier = Modifier.height(15.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "생년월일을 입력해 주세요",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "생년월일",
                style = MaterialTheme.typography.titleMedium,
                color = if (focused) Color(0xFF3182F6) else Color(0x8C333D4B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                BasicTextField(
                    value = uiState.textFieldValue,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused },
                    textStyle = TextStyle(
                        color = Color(0xFF333D4B),
                        fontSize = 24.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (uiState.textFieldValue.text.isNotBlank() && uiState.error == null && !uiState.isSaving) {
                                onSave()
                            }
                        }
                    ),
                    singleLine = true
                )

                if (uiState.textFieldValue.text.isBlank() && !focused) {
                    Text(
                        text = "YYYY.MM.DD",
                        color = Color(0x8C333D4B),
                        fontSize = 24.sp
                    )
                }

                if (uiState.textFieldValue.text.isNotBlank()) {
                    Text(
                        text = "지우기",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { onClear() },
                        color = Color(0xFFB1B8C0),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        when {
                            uiState.error != null -> Color(0xFFD32F2F)
                            focused -> Color(0xFF3182F6)
                            else -> Color(0x0D000000)
                        }
                    )
            )

            uiState.error?.let { message ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message,
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = true))

        if (isKeyboardVisible) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = onSave,
                    enabled = uiState.textFieldValue.text.isNotBlank() && uiState.error == null && !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3182F6),
                        disabledContainerColor = Color(0x403182F6)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = if (uiState.isSaving) "저장 중..." else "다음",
                        color = Color.White
                    )
                }
            }
        }
    }
}
