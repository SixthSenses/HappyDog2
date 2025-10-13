package com.example.pet_project_frontend.presentation.mypage.main

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.presentation.mypage.main.components.LegalSection
import com.example.pet_project_frontend.presentation.mypage.main.components.SettingsSection
import com.example.pet_project_frontend.presentation.mypage.main.components.WithdrawalSection

// 화면 내에서 사용할 공용 변수들 (가이드에 명시된 색상, 폰트 사이즈 등)
object Variables {
    val unnamed: Color = Color(0xFF333D4B)
    val white: Color = Color(0xFFFFFFFF)
    val grey: Color = Color(0xFF8B95A1)
    val primary: Color = Color(0xFF3182F6)
    val appVersionTextColor: Color = Color(0xFF6B7684)
    val StaticTitleLargeSize: TextUnit = 22.sp
    val plusBorderColor: Color = Color(0xFFE4E7EA)
    val plusBackgroundColor: Color = Color(0xFFF2F4F6)
}

@Composable
fun MyPageScreen(
    onNameClick: (String) -> Unit = { _ -> },
    onBirthdateClick: (String) -> Unit = { _ -> },
    onGenderClick: (String) -> Unit = {},
    onBreedClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onVerificationClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    onProfileImageClick: () -> Unit = {},
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadUserData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // TODO: Snackbar/Toast 표시
        }
    }

    var showPhotoPicker by remember { mutableStateOf(false) }
    var cropSourceUri by remember { mutableStateOf<Uri?>(null) }
    var showCrop by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(width = 412.dp, height = 917.dp)
    ) {
        // 상태바 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .background(MyPageColors.Background)
        )

        // 본문
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 90.dp)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MyPageColors.Background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    // 프로필 탭
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // 프로필 사진
                        Box(
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(1.dp)
                                    .size(59.dp)
                                    .background(Variables.unnamed)
                            ) {
                                // 프로필 이미지 (리소스는 사용자가 추가)
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.drawable.icon),
                                    contentDescription = "프로필 사진",
                                    contentScale = ContentScale.None,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // 추가 버튼
                            Box(
                                modifier = Modifier
                                    .offset(x = 40.dp, y = 40.dp)
                                    .size(19.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .border(width = 3.dp, color = Variables.plusBorderColor)
                                        .padding(3.dp)
                                        .size(19.dp)
                                        .background(Variables.plusBackgroundColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(id = R.drawable.ellipse3),
                                        contentDescription = "추가 버튼 배경",
                                        contentScale = ContentScale.None,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(id = R.drawable.add),
                                        contentDescription = "추가 아이콘",
                                        contentScale = ContentScale.None,
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .size(15.dp)
                                    )
                                }
                            }
                        }

                        // 이름/서브텍스트
                        Column(modifier = Modifier.padding(start = 18.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(width = 120.dp, height = 22.dp)
                                    .padding(top = 5.dp)
                            ) {
                                Text(
                                    text = uiState.petName.ifBlank { "레오" },
                                    style = TextStyle(
                                        fontSize = Variables.StaticTitleLargeSize,
                                        lineHeight = 22.sp,
                                        fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                                        fontWeight = FontWeight.W600,
                                        color = Variables.unnamed
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.size(width = 200.dp, height = 17.dp)) {
                                Text(
                                    text = if (uiState.breed.isNotBlank() && uiState.age.isNotBlank())
                                        "${uiState.breed} • ${uiState.age}"
                                    else
                                        listOf(uiState.breed, uiState.age)
                                            .filter { it.isNotBlank() }
                                            .joinToString(" • "),
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        lineHeight = 16.8.sp,
                                        fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                                        fontWeight = FontWeight.W500,
                                        color = Variables.grey
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    ProfileButtonField(
                        label = "이름",
                        value = uiState.petName.ifBlank { "레오" }
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    ProfileButtonField(
                        label = "생년월일",
                        value = uiState.birthDate
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    ProfileButtonField(
                        label = "성별",
                        value = uiState.gender
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    ProfileButtonField(
                        label = "견종",
                        value = uiState.breed
                    )

                    // 설정 섹션(랩핑 Box로 스타일 적용)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(380.dp)
                            .height(121.dp)
                            .background(Variables.white, shape = RoundedCornerShape(20.dp))
                    ) {
                        SettingsSection(
                            onNotificationClick = onNotificationClick,
                            onVerificationClick = onVerificationClick
                        )
                    }

                    // 법적정보 섹션
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(380.dp)
                            .height(171.dp)
                            .background(Variables.white, shape = RoundedCornerShape(20.dp))
                    ) {
                        LegalSection(
                            onTermsClick = onTermsClick,
                            onPrivacyClick = onPrivacyClick
                        )
                    }

                    // 탈퇴 섹션
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(380.dp)
                            .height(50.dp)
                            .background(Variables.white, shape = RoundedCornerShape(16.dp))
                    ) {
                        WithdrawalSection(onClick = onWithdrawClick)
                    }

                    // 앱 버전
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .padding(end = 266.dp)
                            .size(width = 130.dp, height = 16.dp)
                    ) {
                        Text(
                            text = "앱 버전 2025.07.20",
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                                fontWeight = FontWeight.W400,
                                color = Variables.appVersionTextColor
                            )
                        )
                    }
                }
            }
        }

        // 미디어 픽커 시트 & 크롭 화면
        MediaPickerSheet(
            visible = showPhotoPicker,
            onDismissRequest = { showPhotoPicker = false },
            onPicked = { uri ->
                cropSourceUri = uri
                showPhotoPicker = false
                showCrop = true
            }
        )
        if (showCrop && cropSourceUri != null) {
            PhotoCropScreen(
                source = cropSourceUri!!,
                onCropped = { croppedUri ->
                    viewModel.updateProfileImage(croppedUri.toString())
                    showCrop = false
                    cropSourceUri = null
                },
                onCancel = {
                    showCrop = false
                    cropSourceUri = null
                }
            )
        }
    }
}

@Composable
private fun ProfileButtonField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(width = 60.dp, height = 18.dp)) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                    fontWeight = FontWeight.W500,
                    color = Color(0xFF4E5968)
                )
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 3.dp)
        ) {
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                    fontWeight = FontWeight.W400,
                    color = Variables.primary
                ),
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.navigate_next),
            contentDescription = "네비게이션 아이콘",
            contentScale = ContentScale.None
        )
    }
}
