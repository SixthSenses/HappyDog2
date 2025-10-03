package com.example.pet_project_frontend.presentation.mypage.main

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.presentation.mypage.main.components.ProfileHeader
import com.example.pet_project_frontend.presentation.mypage.main.components.ProfileInfoSection
import com.example.pet_project_frontend.presentation.mypage.main.components.SettingsSection
import com.example.pet_project_frontend.presentation.mypage.main.components.LegalSection
import com.example.pet_project_frontend.presentation.mypage.main.components.WithdrawalSection
import com.example.pet_project_frontend.presentation.mypage.main.components.AppVersionSection
import com.example.pet_project_frontend.presentation.mypage.main.components.MediaPickerSheet
import com.example.pet_project_frontend.presentation.mypage.main.components.PhotoCropScreen

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

    // 에러 메시지 표시 효과 (Snackbar나 Toast로 실제 구현 시 변경)
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            // TODO: 에러 메시지를 Snackbar나 Toast 등으로 표시 (앱의 에러 처리 정책에 따름)
        }
    }

    // 사진 선택/크롭 상태 관리
    var showPhotoPicker by remember { mutableStateOf(false) }
    var cropSourceUri by remember { mutableStateOf<Uri?>(null) }
    var showCrop by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MyPageColors.Background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "알 수 없는 오류가 발생했습니다.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadUserData() }) {
                            Text("다시 시도")
                        }
                    }
                }
            }
            else -> {
                // 상단 프로필 헤더
                ProfileHeader(
                    name = if (uiState.petName.isBlank()) "프로필" else uiState.petName,
                    description = if (uiState.breed.isNotBlank() && uiState.age.isNotBlank()) {
                        "${uiState.breed} • ${uiState.age}"
                    } else {
                        listOf(uiState.breed, uiState.age).filter { it.isNotBlank() }.joinToString(" • ")
                    },
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
                    onNameClick = { onNameClick(uiState.petName) },
                    onBirthdateClick = { onBirthdateClick(uiState.birthDate) },
                    onGenderClick = { onGenderClick(uiState.gender) },
                    onBreedClick = { onBreedClick(uiState.breed) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 설정 섹션
                SettingsSection(
                    onNotificationClick = onNotificationClick,
                    onVerificationClick = onVerificationClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 법적 정보 섹션
                LegalSection(
                    onTermsClick = onTermsClick,
                    onPrivacyClick = onPrivacyClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 탈퇴 섹션
                WithdrawalSection(onClick = onWithdrawClick)

                Spacer(modifier = Modifier.height(32.dp))

                // 앱 버전 정보 섹션
                AppVersionSection()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 미디어 픽커 시트 (사진 선택)
    MediaPickerSheet(
        visible = showPhotoPicker,
        onDismissRequest = { showPhotoPicker = false },
        onPicked = { uri ->
            cropSourceUri = uri
            showPhotoPicker = false
            showCrop = true
        }
    )

    // 사진 크롭 화면 (uCrop 실행 래퍼)
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