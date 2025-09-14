package com.example.pet_project_frontend.presentation.mypage.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.core.navigation.BottomNavigation
import com.example.pet_project_frontend.presentation.mypage.main.components.ProfileHeader
import com.example.pet_project_frontend.presentation.mypage.main.components.ProfileInfoSection
// import com.example.pet_project_frontend.presentation.mypage.main.components.SettingsSection
import com.example.pet_project_frontend.presentation.mypage.main.components.LegalSection
import com.example.pet_project_frontend.presentation.mypage.main.components.WithdrawalSection
import com.example.pet_project_frontend.presentation.mypage.main.components.AppVersionSection

@Composable
fun MyPageScreen(
    onNameClick: (String) -> Unit = { _ -> },
    onBirthdateClick: (String) -> Unit = {},
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
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // 사진 선택/크롭 상태
    var showPhotoPicker by remember { mutableStateOf(false) }
    var cropSourceUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showCrop by remember { mutableStateOf(false) }

    Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MyPageColors.Background)
                .verticalScroll(scrollState)
        ) {
            // 프로필 헤더
            ProfileHeader(
                name = uiState.petName,
                description = "${uiState.breed} · ${uiState.age}",
                profileImageUrl = uiState.profileImageUrl,
                onProfileImageClick = { showPhotoPicker = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 프로필 정보 섹션
            ProfileInfoSection(
                name = uiState.petName,
                birthDate = uiState.birthDate,
                gender = uiState.gender,
                breed = uiState.breed,
                // 🔹 onNameClick은 현재 이름을 들고 가도록 람다로 감쌈 (중복 라인 제거!)
                onNameClick = { onNameClick(uiState.petName) },
                onBirthdateClick = { onBirthdateClick(uiState.birthDate) },
                onGenderClick = onGenderClick,
                onBreedClick = onBreedClick
            )

            Spacer(modifier = Modifier.height(12.dp))

//            SettingsSection(
//                onNotificationClick = onNotificationClick,
 //               onVerificationClick = onVerificationClick
 //           )

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
        MediaPickerSheet(
            visible = showPhotoPicker,
            onDismissRequest = { showPhotoPicker = false },
            onPicked = { uri ->
                cropSourceUri = uri
                showPhotoPicker = false
                showCrop = true
            }
        )

    // 1-2. 크롭 화면 (uCrop 실행 래퍼)
        if (showCrop && cropSourceUri != null) {
            PhotoCropScreen(
                source = cropSourceUri!!,
                onCropped = { out ->
                    viewModel.updateProfileImage(out.toString())
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


