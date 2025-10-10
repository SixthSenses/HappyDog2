// 마이페이지 화면 여백 및 프로필 아이콘 표현 개선을 위한 수정.
package com.example.pet_project_frontend.presentation.mypage.main

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.presentation.mypage.main.components.SettingsItemRow

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
    onNameClick: (String?, String) -> Unit = { _, _ -> },
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
    val systemNavPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarPadding = systemNavPadding + 10.dp

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadUserData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // TODO: Snackbar/Toast 처리
        }
    }

    var showPhotoPicker by remember { mutableStateOf(false) }
    var cropSourceUri by remember { mutableStateOf<Uri?>(null) }
    var showCrop by remember { mutableStateOf(false) }

    val openPhotoPicker: () -> Unit = {
        onProfileImageClick()
        showPhotoPicker = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MyPageColors.Background)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(top = 28.dp, bottom = bottomBarPadding)
            ) {
                ProfileSummarySection(
                    uiState = uiState,
                    onProfileImageClick = openPhotoPicker
                )

                Spacer(modifier = Modifier.height(28.dp))

                ProfileDetailSection(
                    uiState = uiState,
                    onNameClick = onNameClick,
                    onBirthdateClick = onBirthdateClick,
                    onGenderClick = onGenderClick,
                    onBreedClick = onBreedClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                SectionContainer(shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(30.dp)
                    ) {
                        SettingsItemRow(
                            label = "알림 설정",
                            onClick = onNotificationClick
                        )
                        SettingsItemRow(
                            label = "신원 인증",
                            onClick = onVerificationClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SectionContainer(shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "법적 정보",
                            modifier = Modifier
                                .padding(start = 22.dp, top = 28.dp),
                            style = TextStyle(
                                fontSize = 18.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                                fontWeight = FontWeight.W700,
                                color = Variables.unnamed
                            )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 22.dp, end = 22.dp, top = 28.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(30.dp)
                        ) {
                            SettingsItemRow(
                                label = "서비스 이용약관",
                                onClick = onTermsClick
                            )
                            SettingsItemRow(
                                label = "개인정보 처리방침",
                                onClick = onPrivacyClick
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SectionContainer(shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 16.dp)
                    ) {
                        SettingsItemRow(
                            label = "탈퇴하기",
                            onClick = onWithdrawClick,
                            contentPadding = PaddingValues(vertical = 0.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "앱 버전 2025.10.10",
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
            Dialog(
                onDismissRequest = {
                    showCrop = false
                    cropSourceUri = null
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnClickOutside = false,
                    decorFitsSystemWindows = false
                )
            ) {
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
}

@Composable
private fun ProfileSummarySection(
    uiState: MyPageUiState,
    onProfileImageClick: () -> Unit
) {
    val displayName = uiState.petName.ifBlank { "이름을 설정해주세요" }
    val subtitle = buildList {
        if (uiState.breed.isNotBlank()) add(uiState.breed)
        if (uiState.age.isNotBlank()) add(uiState.age)
    }.joinToString(" • ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(59.dp)
                .clip(CircleShape)
                .background(Variables.unnamed)
                .clickable { onProfileImageClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .size(19.dp)
                    .align(Alignment.BottomEnd)
                    .border(width = 3.dp, color = Variables.plusBorderColor, shape = CircleShape)
                    .background(Variables.plusBackgroundColor, shape = CircleShape)
                    .clickable { onProfileImageClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ellipse3),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "이미지 추가",
                    modifier = Modifier
                        .padding(2.dp)
                        .size(15.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(
            modifier = Modifier.padding(top = 5.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = displayName,
                style = TextStyle(
                    fontSize = Variables.StaticTitleLargeSize,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                    fontWeight = FontWeight.W600,
                    color = Variables.unnamed
                )
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
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
}

@Composable
private fun ProfileDetailSection(
    uiState: MyPageUiState,
    onNameClick: (String?, String) -> Unit,
    onBirthdateClick: (String) -> Unit,
    onGenderClick: (String) -> Unit,
    onBreedClick: (String) -> Unit
) {
    SectionContainer(shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            ProfileButtonField(
                label = "이름",
                value = uiState.petName.ifBlank { "미등록" },
                onClick = { onNameClick(uiState.petId, uiState.petName) }
            )
            ProfileButtonField(
                label = "생년월일",
                value = uiState.birthDate.ifBlank { "미등록" },
                onClick = { onBirthdateClick(uiState.birthDate) }
            )
            ProfileButtonField(
                label = "성별",
                value = uiState.gender.ifBlank { "미등록" },
                onClick = { onGenderClick(uiState.gender) }
            )
            ProfileButtonField(
                label = "견종",
                value = uiState.breed.ifBlank { "미등록" },
                onClick = { onBreedClick(uiState.breed) }
            )
        }
    }
}

@Composable
private fun SectionContainer(
    shape: RoundedCornerShape,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Variables.white, shape = shape)
    ) {
        Column(content = content)
    }
}

@Composable
private fun ProfileButtonField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 3.dp),
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                fontWeight = FontWeight.W400,
                color = Variables.primary,
                textAlign = TextAlign.Right
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Image(
            painter = painterResource(id = R.drawable.navigate_next),
            contentDescription = "다음",
            modifier = Modifier.size(24.dp)
        )
    }
}



