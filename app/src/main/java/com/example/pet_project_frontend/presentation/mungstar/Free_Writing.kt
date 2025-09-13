package com.example.pet_project_frontend.presentation.mungstar

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.data.mungstar_model.LocalPostManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun FreeWriting(navController: NavController) {
    var textContent by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val maxCharacters = 2000
    var uploadedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 갤러리에서 이미지 선택
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val newImages = uris.take(4 - uploadedImages.size)
        uploadedImages = uploadedImages + newImages
    }

    // 카메라 권한 요청
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 카메라 실행
        }
    }

    // 카메라로 사진 촬영
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null && uploadedImages.size < 4) {
            uploadedImages = uploadedImages + photoUri!!
        }
    }

    // 임시 파일 생성 함수
    fun createImageFile(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = File(context.cacheDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
    }

    // 게시물 작성 함수 (로컬 버전)
    fun createPost() {
        if (textContent.isBlank()) {
            errorMessage = "텍스트를 입력해주세요."
            return
        }

        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // 로딩 시뮬레이션
                delay(1000)

                // 로컬 게시물 매니저에 게시물 추가
                LocalPostManager.addPost(textContent, uploadedImages)

                // 성공 후 뒤로가기
                isLoading = false
                navController.popBackStack()

            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 위에서 42px 떨어진 앱바 (10px 위로 조정)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .offset(y = 42.dp)
                    .background(Color.White)
            ) {
                // back.png 버튼 (왼쪽 4px)
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = 4.dp, y = 12.dp)
                        .clickable { navController.popBackStack() }
                )

                // Title 박스 (위아래 23px, 좌우 56px)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 56.dp, vertical = 23.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "새로운 게시물",
                        fontSize = 18.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Background 박스와 텍스트 입력 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .offset(y = 42.dp)
                    .background(Color.White)
                    .padding(horizontal = 25.dp, vertical = 20.dp)
            ) {
                Column {
                    // 텍스트 입력 필드와 힌트가 함께 있는 영역
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 힌트 텍스트 (텍스트가 비어있을 때만 표시)
                        if (textContent.isEmpty()) {
                            Text(
                                text = "새로운 소식이 있나요?",
                                fontSize = 18.sp,
                                color = Color(0xFF8B95A1),
                                modifier = Modifier.offset(y = 3.dp)
                            )
                        }

                        // 텍스트 입력 필드
                        BasicTextField(
                            value = textContent,
                            onValueChange = { newText ->
                                if (newText.length <= maxCharacters) {
                                    textContent = newText
                                }
                                errorMessage = null // 에러 메시지 초기화
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp)
                                .verticalScroll(rememberScrollState())
                                .onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused
                                },
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                color = Color.Black
                            ),
                            enabled = !isLoading
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 에러 메시지 표시
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    // 업로드된 이미지들 (가로 스크롤)
                    if (uploadedImages.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(uploadedImages) { imageUri ->
                                Box(
                                    modifier = Modifier.size(250.dp)
                                ) {
                                    // 업로드된 이미지
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = "업로드된 이미지",
                                        modifier = Modifier
                                            .size(250.dp)
                                            .background(
                                                Color.Gray.copy(alpha = 0.1f),
                                                RoundedCornerShape(8.dp)
                                            )
                                    )

                                    // exclude.png 삭제 버튼 (위로 -12, 오른쪽으로 +215)
                                    Image(
                                        painter = painterResource(id = R.drawable.exclude),
                                        contentDescription = "이미지 삭제",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .offset(x = 215.dp, y = (-12).dp)
                                            .clickable {
                                                if (!isLoading) {
                                                    uploadedImages = uploadedImages.filter { it != imageUri }
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Background Bar (412x52, 10px 위로 조정)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .background(Color.White)
            ) {
                // image.png (갤러리)
                Image(
                    painter = painterResource(id = R.drawable.image),
                    contentDescription = "갤러리",
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = 22.dp, y = 19.dp)
                        .clickable {
                            if (uploadedImages.size < 4 && !isLoading) {
                                galleryLauncher.launch("image/*")
                            }
                        }
                )

                // photo_camera.png (카메라)
                Image(
                    painter = painterResource(id = R.drawable.photo_camera),
                    contentDescription = "카메라",
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = (22 + 24 + 18).dp, y = 19.dp)
                        .clickable {
                            if (uploadedImages.size < 4 && !isLoading) {
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) -> {
                                        photoUri = createImageFile()
                                        cameraLauncher.launch(photoUri!!)
                                    }
                                    else -> {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            }
                        }
                )

                // 게시하기 버튼 (오른쪽으로 +318)
                Box(
                    modifier = Modifier
                        .size(91.dp, 32.dp)
                        .offset(x = 318.dp, y = 15.dp)
                        .background(
                            color = if (textContent.isNotEmpty() && !isLoading)
                                Color(0xFF3182F6)
                            else
                                Color(0x403182F6), // rgba(49, 130, 246, 0.25)
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable(enabled = textContent.isNotEmpty() && !isLoading) {
                            createPost()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "게시하기",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 글자수 카운트 (키보드 위, 왼쪽 10px, 위로 5px)
                if (textContent.isNotEmpty()) {
                    Text(
                        text = "${textContent.length}/$maxCharacters",
                        fontSize = 16.sp,
                        color = Color(0xFF8B95A1),
                        modifier = Modifier
                            .offset(x = 10.dp, y = (-5).dp)
                    )
                }
            }
        }
    }
}