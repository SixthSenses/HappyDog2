// 변경 의도: 이름 편집 화면을 가이드 간격에 맞춰 구성하고 상태별 스타일을 명확히 반영.
package com.example.pet_project_frontend.presentation.mypage.profile.name

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.components.TopBar
import com.example.pet_project_frontend.core.navigation.Screen
import com.example.pet_project_frontend.presentation.mypage.main.MyPageViewModel

object Variables {
    val unnamedText1: Color = Color(0xFF191F28)
    val labelDefault: Color = Color(0xFF333D4B)
    val primary: Color = Color(0xFF3182F6)
    val placeholder: Color = Color(0x8C333D4B)
    val danger: Color = Color(0xFFE42A38)
}

@Composable
fun NameEditRoute(
    navController: NavController,
    viewModel: NameEditViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val parentEntry: NavBackStackEntry = remember(navController) {
        navController.getBackStackEntry(Screen.MyPage.route)
    }
    val myPageViewModel: MyPageViewModel = androidx.hilt.navigation.compose.hiltViewModel(parentEntry)

    NameEditScreen(
        text = ui.text,
        error = ui.error,
        isSaving = ui.isSaving,
        isValidationError = ui.isValidationError,
        onBack = { navController.popBackStack() },
        onTextChange = viewModel::onTextChange,
        onClear = viewModel::onClear,
        onSave = {
            viewModel.onSave { savedName, shouldReload ->
                myPageViewModel.updatePetName(savedName)
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
private fun NameEditScreen(
    text: String,
    error: String?,
    isSaving: Boolean,
    isValidationError: Boolean,
    onBack: () -> Unit,
    onTextChange: (String) -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()
    var isFocused by remember { mutableStateOf(false) }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 120.dp)
        ) {
            TopBar(
                title = {},
                onNavigateBack = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "이름을 입력해주세요",
                    style = TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                        fontWeight = FontWeight.W600,
                        color = Variables.unnamedText1
                    )
                )

                Spacer(modifier = Modifier.height(26.dp))

                val labelColor = when {
                    isValidationError -> Variables.danger
                    text.isBlank() -> Variables.primary
                    else -> Variables.labelDefault
                }

                Text(
                    text = "이름",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                        fontWeight = FontWeight.W500,
                        color = labelColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .onFocusChanged { isFocused = it.isFocused },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                            fontWeight = FontWeight.W500,
                            color = Variables.labelDefault
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (text.isBlank()) {
                                Text(
                                    text = "이름",
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        lineHeight = 24.sp,
                                        fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                                        fontWeight = FontWeight.W500,
                                        color = Variables.placeholder
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (text.isNotBlank() && isFocused) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(19.dp)
                                .clickable { onClear() }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ellipse1),
                                contentDescription = "입력값 삭제 배경",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )
                            Image(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = "입력값 삭제",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(13.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val dividerColor = when {
                    isValidationError -> Variables.danger.copy(alpha = 0.7f)
                    text.isBlank() -> Variables.primary
                    else -> Color(0x0D000000)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(dividerColor)
                )

                if (!error.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = error,
                        modifier = Modifier.padding(start = 1.dp),
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                            fontWeight = FontWeight.W500,
                            color = if (isValidationError) Variables.danger else Variables.labelDefault
                        )
                    )
                }
            }
        }

        if (isKeyboardVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = onSave,
                    enabled = text.isNotBlank() && !isValidationError && !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Variables.primary,
                        disabledContainerColor = Variables.primary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(
                        text = if (isSaving) "저장 중..." else "저장",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
