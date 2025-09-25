package com.example.pet_project_frontend.presentation.eye_health

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.core.theme.MyPageColors
import java.io.File

@Composable
fun EyeHealthScreen(
    onBackClick: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    viewModel: EyeHealthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionType by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    val hasStoragePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { viewModel.analyzeEyeHealth(it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.analyzeEyeHealth(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera(context) { uri ->
                photoUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            permissionType = "camera"
            showPermissionDialog = true
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            permissionType = "storage"
            showPermissionDialog = true
        }
    }

    val handleCameraClick = {
        if (hasCameraPermission) {
            launchCamera(context) { uri ->
                photoUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        showBottomSheet = false
    }

    val handleGalleryClick = {
        if (hasStoragePermission) {
            galleryLauncher.launch("image/*")
        } else {
            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        showBottomSheet = false
    }

    when {
        uiState.isLoading -> {
            EyeHealthLoadingScreen()
        }
        uiState.analysis != null -> {
            val analysis = uiState.analysis
            if (analysis != null) {
                EyeAnalysisResultScreen(
                    analysis = analysis,
                    onBackClick = {
                        viewModel.resetAnalysis()
                        onBackClick()
                    },
                    onRetakeClick = {
                        viewModel.resetAnalysis()
                        showBottomSheet = true
                    }
                )
            }
        }
        else -> {
            EyeHealthMainContent(
                onBackClick = onBackClick,
                onEyeCheckClick = { showBottomSheet = true },
                onCheckHistoryClick = onNavigateToHistory,
                showBottomSheet = showBottomSheet,
                onDismissBottomSheet = { showBottomSheet = false },
                onCameraClick = handleCameraClick,
                onGalleryClick = handleGalleryClick
            )
        }
    }

    if (showPermissionDialog) {
        PermissionDialog(
            permissionType = permissionType,
            onDismiss = { showPermissionDialog = false },
            onConfirm = {
                showPermissionDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun PermissionDialog(
    permissionType: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "권한이 필요합니다",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MyPageColors.Grey900
            )
        },
        text = {
            Text(
                text = when (permissionType) {
                    "camera" -> "사진 촬영을 위해 카메라 권한이 필요합니다.\n설정에서 권한을 허용해주세요."
                    "storage" -> "갤러리 접근을 위해 저장소 권한이 필요합니다.\n설정에서 권한을 허용해주세요."
                    else -> "앱 사용을 위해 권한이 필요합니다."
                },
                fontSize = 14.sp,
                color = MyPageColors.Grey700
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "설정",
                    color = MyPageColors.Blue500,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "취소",
                    color = MyPageColors.Grey600,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

private fun launchCamera(context: Context, onUriCreated: (Uri) -> Unit) {
    try {
        val photoFile = File.createTempFile(
            "eye_health_photo_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
        val photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        onUriCreated(photoUri)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}