package com.example.pet_project_frontend.presentation.mypage.main

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
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
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditBirthdateDialog by remember { mutableStateOf(false) }
    var showEditGenderDialog by remember { mutableStateOf(false) }
    var showEditBreedDialog by remember { mutableStateOf(false) }

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
        uiState.error?.let { errorMessage ->
            android.widget.Toast.makeText(
                context,
                errorMessage,
                android.widget.Toast.LENGTH_LONG
            ).show()
            viewModel.clearError()
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
                    Button(onClick = { viewModel.loadUserData() }) {
                        Text("다시 시도")
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
                            onProfileImageClick()
                            // 프로필 이미지가 있으면 삭제 다이얼로그, 없으면 사진 선택
                            if (uiState.profileImageUrl != null) {
                                showDeleteDialog = true
                            } else {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 반려동물 프로필/설정 섹션
                    ProfileInfoSection(
                        name = uiState.petName,
                        birthDate = uiState.birthDate,
                        gender = uiState.gender,
                        breed = uiState.breed,
                        onNameClick = { 
                            showEditNameDialog = true
                            onNameClick()
                        },
                        onBirthdateClick = { 
                            showEditBirthdateDialog = true
                            onBirthdateClick()
                        },
                        onGenderClick = { 
                            showEditGenderDialog = true
                            onGenderClick()
                        },
                        onBreedClick = { 
                            showEditBreedDialog = true
                            onBreedClick()
                        }
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
        
        // 프로필 사진 삭제 확인 다이얼로그
        if (showDeleteDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showDeleteDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // 검은색 배경 (32% 불투명도)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.32f))
                            .clickable { showDeleteDialog = false }
                    )
                    
                    // 다이얼로그 박스
                    Box(
                        modifier = Modifier
                            .size(336.dp, 181.dp)
                            .background(
                                color = androidx.compose.ui.graphics.Color.White,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 메시지
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "프로필 사진을 삭제할까요?",
                                    fontFamily = com.example.pet_project_frontend.core.theme.PretendardFont,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight(600),
                                    fontSize = 21.sp,
                                    color = androidx.compose.ui.graphics.Color(0xFF333D4B)
                                )
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                Text(
                                    text = "삭제된 사진은 복구할 수 없어요.",
                                    fontFamily = com.example.pet_project_frontend.core.theme.PretendardFont,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight(500),
                                    fontSize = 16.sp,
                                    color = androidx.compose.ui.graphics.Color(0xFF6B7684)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(21.dp))
                            
                            // 버튼들
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 취소 버튼
                                Box(
                                    modifier = Modifier
                                        .width(146.dp)
                                        .height(58.dp)
                                        .background(
                                            color = androidx.compose.ui.graphics.Color(0xFFF3F4F6),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                                        )
                                        .clickable { showDeleteDialog = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "취소",
                                        fontFamily = com.example.pet_project_frontend.core.theme.PretendardFont,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight(600),
                                        fontSize = 18.sp,
                                        color = androidx.compose.ui.graphics.Color(0xFF4E5968)
                                    )
                                }
                                
                                // 삭제 버튼
                                Box(
                                    modifier = Modifier
                                        .width(146.dp)
                                        .height(58.dp)
                                        .background(
                                            color = androidx.compose.ui.graphics.Color(0xFFEC4453),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            showDeleteDialog = false
                                            viewModel.deleteProfileImage()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "삭제",
                                        fontFamily = com.example.pet_project_frontend.core.theme.PretendardFont,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight(600),
                                        fontSize = 18.sp,
                                        color = androidx.compose.ui.graphics.Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 이름 수정 다이얼로그
        if (showEditNameDialog) {
            com.example.pet_project_frontend.presentation.mypage.edit.EditNameScreen(
                onDismiss = { showEditNameDialog = false },
                viewModel = viewModel
            )
        }
        
        // 생년월일 수정 다이얼로그
        if (showEditBirthdateDialog) {
            com.example.pet_project_frontend.presentation.mypage.edit.EditBirthdateScreen(
                onDismiss = { showEditBirthdateDialog = false },
                viewModel = viewModel
            )
        }
        
        // 성별 수정 다이얼로그
        if (showEditGenderDialog) {
            com.example.pet_project_frontend.presentation.mypage.edit.EditGenderScreen(
                onDismiss = { showEditGenderDialog = false },
                viewModel = viewModel
            )
        }
        
        // 견종 수정 다이얼로그
        if (showEditBreedDialog) {
            com.example.pet_project_frontend.presentation.mypage.edit.EditBreedScreen(
                onDismiss = { showEditBreedDialog = false },
                viewModel = viewModel
            )
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


