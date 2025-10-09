package com.example.pet_project_frontend.presentation.mypage.main

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.presentation.mypage.main.components.AppVersionSection
import com.example.pet_project_frontend.presentation.mypage.main.components.LegalSection
import com.example.pet_project_frontend.presentation.mypage.main.components.ProfileHeader
import com.example.pet_project_frontend.presentation.mypage.main.components.ProfileInfoSection
import com.example.pet_project_frontend.presentation.mypage.main.components.SettingsSection
import com.example.pet_project_frontend.presentation.mypage.main.components.WithdrawalSection

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
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadUserData()
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    // TODO: handle snackbar/toast when needed
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // show snackbar or toast
        }
    }

    var showPhotoPicker by remember { mutableStateOf(false) }
    var cropSourceUri by remember { mutableStateOf<Uri?>(null) }
    var showCrop by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MyPageColors.Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MyPageColors.Background)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            ProfileHeader(
                name = uiState.petName.ifBlank { "우리집 댕댕이" },
                description = when {
                    uiState.breed.isNotBlank() && uiState.age.isNotBlank() ->
                        "${uiState.breed} · ${uiState.age}"
                    uiState.breed.isNotBlank() -> uiState.breed
                    uiState.age.isNotBlank() -> uiState.age
                    else -> ""
                },
                profileImageUrl = uiState.profileImageUrl,
                onProfileImageClick = {
                    showPhotoPicker = true
                    onProfileImageClick()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

            AppVersionSection()

            Spacer(modifier = Modifier.height(16.dp))
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
