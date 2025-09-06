package com.example.pet_project_frontend.presentation.mypage.main

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.presentation.mypage.main.components.AppVersionSection
import com.example.pet_project_frontend.presentation.mypage.main.components.LegalSection
import com.example.pet_project_frontend.presentation.mypage.main.components.ProfileHeader
import com.example.pet_project_frontend.presentation.mypage.main.components.ProfileInfoSection
import com.example.pet_project_frontend.presentation.mypage.main.components.SettingsSection
import com.example.pet_project_frontend.presentation.mypage.main.components.WithdrawalSection
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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
    onRegisterPetClick: () -> Unit = {},
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Photo picker launcher
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = copyUriToCache(context.contentResolver, uri, context.cacheDir)
            if (localPath != null) {
                viewModel.uploadAndApplyProfileImage(localPath)
            }
        }
    }
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
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.error != null -> {
                val errorMessage = uiState.error
                val isMissingPet = errorMessage?.contains("등록된 반려동물") == true
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
                    if (isMissingPet) {
                        Button(onClick = onRegisterPetClick) { Text("반려동물 등록하기") }
                    } else {
                        Button(onClick = { viewModel.loadUserData() }) { Text("다시 시도") }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(top = 8.dp, bottom = 24.dp)
                ) {
                    // 상단 프로필 헤더
                    ProfileHeader(
                        name = uiState.petName.ifBlank { "프로필" },
                        description = listOfNotNull(
                            uiState.breed.takeIf { it.isNotBlank() },
                            uiState.age.takeIf { it.isNotBlank() }
                        ).joinToString(" • "),
                        profileImageUrl = uiState.profileImageUrl,
                        onProfileImageClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 반려동물 프로필/설정 섹션
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

                    LegalSection(
                        onTermsClick = onTermsClick,
                        onPrivacyClick = onPrivacyClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    WithdrawalSection(onClick = onWithdrawClick)

                    Spacer(modifier = Modifier.height(16.dp))

                    AppVersionSection()
                }

                if (uiState.isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}

private fun copyUriToCache(cr: ContentResolver, uri: Uri, cacheDir: File): String? {
    return try {
        val name = "picked_${System.currentTimeMillis()}.jpg"
        val outFile = File(cacheDir, name)
        cr.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        outFile.absolutePath
    } catch (_: Throwable) {
        null
    }
}


