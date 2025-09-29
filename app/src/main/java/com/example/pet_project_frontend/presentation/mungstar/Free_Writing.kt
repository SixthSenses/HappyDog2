package com.example.pet_project_frontend.presentation.mungstar

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.data.mungstar_model.MungstarPostRepository
import com.example.pet_project_frontend.data.mungstar_model.CreatePostRequest
import com.example.pet_project_frontend.data.auth.TokenManager
import kotlinx.coroutines.launch

@Composable
fun FreeWriting(navController: NavController) {
    var textContent by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val maxCharacters = 2000
    var uploadedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val postRepository = remember { MungstarPostRepository(context) }

    // 뒤로가기 처리 함수
    fun handleBackPressed() {
        if (textContent.isNotEmpty() || uploadedImages.isNotEmpty()) {
            showCancelDialog = true
        } else {
            navController.popBackStack()
        }
    }

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

    // 게시물 작성 함수 (API 버전)
    fun createPost() {
        Log.e("SIMPLE_TEST", "createPost 함수 시작!")
        Log.e("SIMPLE_TEST", "TokenManager 테스트 시작")

        val TokenManager = TokenManager(context)
        Log.e("SIMPLE_TEST", "TokenManager 생성 완료")

        val token = TokenManager.getToken()
        Log.e("SIMPLE_TEST", "getToken() 결과: $token")


        Log.d("FreeWriting", "=== 게시물 작성 시작 ===")

        // 디버깅용: 토큰 상태 확인
        val tokenManager = TokenManager(context)
        Log.d("FreeWriting", "토큰 존재: ${tokenManager.hasToken()}")
        Log.d("FreeWriting", "토큰 값: ${tokenManager.getToken()}")
        Log.d("FreeWriting", "텍스트 내용: '$textContent'")
        Log.d("FreeWriting", "이미지 수: ${uploadedImages.size}")

        if (textContent.isBlank()) {
            Log.w("FreeWriting", "텍스트가 비어있음")
            errorMessage = "텍스트를 입력해주세요."
            return
        }

        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            Log.d("FreeWriting", "코루틴 시작, 로딩 상태: true")

            try {
                // 이미지 업로드 후 파일 경로 받기
                val filePaths = mutableListOf<String>()
                Log.d("FreeWriting", "이미지 업로드 시작 - 총 ${uploadedImages.size}개")

                // 이미지가 있는 경우 업로드
                for (imageUri in uploadedImages) {
                    try {
                        Log.d("FreeWriting", "이미지 업로드 중: $imageUri")
                        val filePath = postRepository.uploadImage(context, imageUri)
                        filePaths.add(filePath)
                        Log.d("FreeWriting", "이미지 업로드 성공: $filePath")
                    } catch (e: Exception) {
                        // 개별 이미지 업로드 실패 시 로그만 남기고 계속 진행
                        Log.e("FreeWriting", "개별 이미지 업로드 실패", e)
                    }
                }

                Log.d("FreeWriting", "최종 업로드된 파일 경로: $filePaths")

                // 게시물 생성 요청
                val createPostRequest = CreatePostRequest(
                    text = textContent,
                    filePaths = filePaths
                )

                Log.d("FreeWriting", "게시물 생성 요청 시작")
                val response = postRepository.createPost(createPostRequest)
                Log.d("FreeWriting", "게시물 생성 성공: ${response.postId}")

                // 성공 후 뒤로가기
                isLoading = false
                Log.d("FreeWriting", "게시물 작성 완료, 화면 이동")
                navController.popBackStack()

            } catch (e: Exception) {
                isLoading = false
                val errorMsg = e.message ?: "게시물 작성에 실패했습니다."
                errorMessage = errorMsg
                Log.e("FreeWriting", "게시물 작성 실패: $errorMsg", e)
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
                        .clickable { handleBackPressed() }
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

        // 취소 확인 다이얼로그
        if (showCancelDialog) {
            Dialog(
                onDismissRequest = { showCancelDialog = false },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // 외부 박스 (336*181, 모서리 26)
                    Box(
                        modifier = Modifier
                            .size(336.dp, 181.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(26.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // 내부 박스 (302*137)
                        Box(
                            modifier = Modifier.size(302.dp, 137.dp)
                        ) {
                            // Title1 (위에서 -10, 오른쪽으로 +6)
                            Text(
                                text = "게시물 작성을 취소할까요?",
                                fontSize = 21.sp,
                                color = Color(0xFF333D4B),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.offset(x = 6.dp, y = (-10).dp)
                            )

                            // Title2 (Title1 아래로 4픽셀)
                            Text(
                                text = "지금까지 쓴 내용은 저장되지 않아요",
                                fontSize = 16.sp,
                                color = Color(0xFF6B7684),
                                modifier = Modifier.offset(x = 6.dp, y = 19.dp)
                            )

                            // 버튼들 (Title2 아래로 21픽셀)
                            Row(
                                modifier = Modifier
                                    .offset(x = 6.dp, y = 61.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 닫기 버튼 (146*58, 모서리 14)
                                Box(
                                    modifier = Modifier
                                        .size(146.dp, 58.dp)
                                        .background(
                                            color = Color(0xFFF3F4F6),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { showCancelDialog = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "닫기",
                                        fontSize = 18.sp,
                                        color = Color(0xFF4E5968),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // 취소하기 버튼 (146*58, 모서리 14)
                                Box(
                                    modifier = Modifier
                                        .size(146.dp, 58.dp)
                                        .background(
                                            color = Color(0xFFEC4453),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            showCancelDialog = false
                                            navController.popBackStack()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "취소하기",
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}