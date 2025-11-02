@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.example.pet_project_frontend.presentation.mypage.settings.verification

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.pet_project_frontend.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

@Composable
fun TransparentTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationGuideScreen(
    onBack: () -> Unit,
    onPickImage: (String) -> Unit,
    errorDialog: VerificationGuideError? = null,
    onDismissError: () -> Unit = {}
) {
    val context = LocalContext.current
    val cameraController = remember { LifecycleCameraController(context) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPickImage(it.toString()) }
    }

    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var flashOn by remember { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(flashOn) {
        cameraController.enableTorch(flashOn)
    }

    Scaffold(
        topBar = { TransparentTopBar(onBack = onBack) },
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cameraPermissionState.status.isGranted) {
                CameraPreview(cameraController, modifier = Modifier.fillMaxSize())
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("카메라 권한이 필요합니다.", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("권한 요청")
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(46.dp))
                    Text(
                        text = "반려견의 비문(코)을 근접 촬영",
                        style = TextStyle(
                            fontSize = 17.sp,
                            lineHeight = 17.sp,
                            fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFFB1B8C0),
                            textAlign = TextAlign.Center,
                        )
                    )
                    Spacer(modifier = Modifier.height(52.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.size(42.dp)) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "앨범",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clickable { 
                                    takePhoto(context, cameraController) { uri ->
                                        onPickImage(uri.toString())
                                    }
                                 }
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.camera_button),
                                contentDescription = "촬영",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        IconButton(onClick = { flashOn = !flashOn }, modifier = Modifier.size(42.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.flashlight_icon),
                                contentDescription = "플래시",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            containerColor = Color.White,
            dragHandle = {},
            windowInsets = WindowInsets(0,0,0,0)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // Add padding for navigation bar
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(13.dp))
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFCED3D8))
                )
                Spacer(Modifier.height(30.dp))

                Text(
                    text = "비문 촬영하기",
                    color = Color(0xFF191F28),
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                    fontWeight = FontWeight(500),
                    lineHeight = 21.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Left
                )

                Spacer(Modifier.height(19.dp))

                Text(
                    text = "반려견의 코를 가까이에서 정면으로\n촬영해주세요. 빛 반사가 없도록 자연광에서\n촬영하고, 코 전체가 선명하게 나오도록 해주세요.",
                    color = Color(0xFF4E5968),
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    fontWeight = FontWeight(400),
                    lineHeight = 26.1.sp,
                    letterSpacing = (-0.18).sp,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(35.dp))

                Button(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            showSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3182F6)),
                    contentPadding = PaddingValues()
                ) {
                    Text(
                        text = "확인",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    when (errorDialog) {
        VerificationGuideError.Duplicate ->
            VerificationDuplicateNoseDialog(onConfirm = onDismissError)
        VerificationGuideError.DetectionFailed ->
            VerificationDetectionFailedDialog(onConfirm = onDismissError)
        VerificationGuideError.AlreadyVerified,
        VerificationGuideError.Unknown,
        null -> Unit
    }
}

@Composable
fun CameraPreview(controller: LifecycleCameraController, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}

private fun takePhoto(
    context: Context,
    controller: LifecycleCameraController,
    onPhotoTaken: (Uri) -> Unit
) {
    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    val file = File.createTempFile(
        "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}",
        ".jpg",
        context.externalCacheDir
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    controller.takePicture(
        outputOptions,
        mainExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                outputFileResults.savedUri?.let { onPhotoTaken(it) }
            }

            override fun onError(exception: ImageCaptureException) {
                // Handle error
            }
        }
    )
}

/**
 * URI를 File 객체로 변환
 * - content:// URI인 경우 임시 파일로 복사
 * - file:// URI인 경우 직접 File 객체 생성
 */
private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        when (uri.scheme) {
            "file" -> {
                // file:// URI - 직접 파일 경로 사용
                File(uri.path ?: return null)
            }
            "content" -> {
                // content:// URI - 임시 파일로 복사
                val inputStream = context.contentResolver.openInputStream(uri) ?: return null
                val tempFile = File.createTempFile(
                    "temp_nose_print_${System.currentTimeMillis()}",
                    ".jpg",
                    context.cacheDir
                )
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()
                tempFile
            }
            else -> null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
