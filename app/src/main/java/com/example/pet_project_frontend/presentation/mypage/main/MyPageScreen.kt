package com.example.pet_project_frontend.presentation.mypage.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.core.navigation.BottomNavigation
import com.example.pet_project_frontend.presentation.mypage.main.components.ProfileHeader
import com.example.pet_project_frontend.presentation.mypage.main.components.ProfileInfoSection
import com.example.pet_project_frontend.presentation.mypage.main.components.SettingsSection
import com.example.pet_project_frontend.presentation.mypage.main.components.LegalSection
import com.example.pet_project_frontend.presentation.mypage.main.components.WithdrawalSection
import com.example.pet_project_frontend.presentation.mypage.main.components.AppVersionSection

@Composable
fun MyPageScreen(
    onNameClick: () -> Unit = {},
    onBirthdateClick: () -> Unit = {},
    onGenderClick: () -> Unit = {},
    onBreedClick: () -> Unit = {},
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

    // 에러 메시지 표시
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // TODO: Snackbar나 Toast로 에러 메시지 표시
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MyPageColors.Background)
    ) {
        when {
            uiState.isLoading -> {
                // 로딩 상태
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.error != null -> {
                // 에러 상태
                val errorMessage = uiState.error
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage ?: "알 수 없는 오류가 발생했습니다.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadUserData() }
                    ) {
                        Text("다시 시도")
                    }
                }
            }
            else -> {
                // 성공 상태
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // 프로필 헤더
                    ProfileHeader(
                        name = uiState.petName,
                        description = "${uiState.breed} · ${uiState.age}",
                        profileImageUrl = uiState.profileImageUrl,
                        onProfileImageClick = onProfileImageClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 프로필 정보 섹션
                    ProfileInfoSection(
                        name = uiState.petName,
                        birthDate = uiState.birthDate,
                        gender = uiState.gender,
                        breed = uiState.breed,
                        onNameClick = onNameClick,
                        onBirthdateClick = onBirthdateClick,
                        onGenderClick = onGenderClick,
                        onBreedClick = onBreedClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsSection(
                        onNotificationClick = onNotificationClick,
                        onVerificationClick = onVerificationClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 법적 정보 카드
                    LegalSection(
                        onTermsClick = onTermsClick,
                        onPrivacyClick = onPrivacyClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 탈퇴하기 카드
                    WithdrawalSection(onClick = onWithdrawClick)

                    Spacer(modifier = Modifier.height(32.dp))

                    // 앱 버전
                    AppVersionSection()

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


