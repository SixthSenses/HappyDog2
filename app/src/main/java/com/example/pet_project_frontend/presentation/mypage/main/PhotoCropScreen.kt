    package com.example.pet_project_frontend.presentation.mypage.main

    import android.net.Uri
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.result.ActivityResult
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Text
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.platform.LocalContext
    import com.yalantis.ucrop.UCrop
    import java.io.File

    /**
     * 1-2. 프로필 사진 크기 설정 화면
     * - uCrop 액티비티를 런치해서 원형 크롭 결과를 URI로 콜백
     * - 뒤 배경은 와이어의 검은 화면/안내 텍스트만 표시
     */
    @Composable
    fun PhotoCropScreen(
        source: Uri,
        onCropped: (Uri) -> Unit,
        onCancel: () -> Unit
    ) {
        val context = LocalContext.current
        var launched by remember { mutableStateOf(false) }

        val cropLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val out = result.data?.let { UCrop.getOutput(it) }
                if (out != null) onCropped(out) else onCancel()
            } else onCancel()
        }

        LaunchedEffect(source) {
            if (!launched) {
                launched = true
                val dest = File(createTempDir(), "cropped_${System.currentTimeMillis()}.jpg")
                val options = UCrop.Options().apply {
                    setCircleDimmedLayer(true)        // 원형 마스크
                    setShowCropFrame(false)
                    setHideBottomControls(true)
                    setToolbarColor(0xFF000000.toInt())
                    //setStatusBarColor(0xFF000000.toInt())
                    setToolbarWidgetColor(0xFFFFFFFF.toInt())
                    setRootViewBackgroundColor(0xFF000000.toInt())
                }

                val intent = UCrop.of(source, Uri.fromFile(dest))
                    .withOptions(options)
                    .withAspectRatio(1f, 1f)
                    .getIntent(context)

                cropLauncher.launch(intent)
            }
        }

        // uCrop이 떠있는 동안의 배경 (디자인 정합용)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .background(Color.Black.copy(alpha = 0f))
            ) {
                Text(
                    text = "사진을 원하는 크기로 맞춘 뒤 확인을 눌러주세요",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
