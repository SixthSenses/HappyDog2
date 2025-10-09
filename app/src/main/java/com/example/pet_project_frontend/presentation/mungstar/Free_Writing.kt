package com.example.pet_project_frontend.presentation.mungstar

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.PretendardFont
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FreeWriting(
    navController: NavController,
    viewModel: FreeWritingViewModel = hiltViewModel(),
    postId: String? = null,
    initialText: String? = null,
    initialImageUrls: List<String>? = null
) {
    var textContent by remember { mutableStateOf(initialText ?: "") }
    var showCancelDialog by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val maxCharacters = 2000

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 수정 모드 초기화
    val isEditMode = postId != null
    LaunchedEffect(postId) {
        if (postId != null) {
            if (initialText != null) {
                // 파라미터로 받은 경우 (기존 호환성)
                viewModel.initEditMode(postId, initialText, initialImageUrls ?: emptyList())
                textContent = initialText
            } else {
                // postId만 있는 경우 자동으로 불러오기
                viewModel.loadPostForEdit(postId)
            }
        }
    }
    
    // ViewModel에서 불러온 게시글 데이터 반영
    LaunchedEffect(uiState.existingText) {
        if (uiState.isEditMode && uiState.existingText.isNotEmpty() && textContent.isEmpty()) {
            textContent = uiState.existingText
        }
    }

    // 성공 처리
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(
                context,
                if (isEditMode) "게시물이 수정되었습니다" else "게시물이 등록되었습니다",
                Toast.LENGTH_SHORT
            ).show()
            navController.popBackStack()
        }
    }

    // 에러 처리
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    // 뒤로가기 처리 함수
    fun handleBackPressed() {
        if (textContent.isNotEmpty() || uiState.uploadedImages.isNotEmpty()) {
            showCancelDialog = true
        } else {
            navController.popBackStack()
        }
    }

    // 갤러리에서 이미지 선택
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val remainingSlots = 4 - uiState.uploadedImages.size
        val newImages = uris.take(remainingSlots)
        viewModel.addImages(newImages)
    }

    // 카메라 권한 요청
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // 권한이 허용되면 카메라는 cameraLauncher에서 실행
    }

    // 카메라로 사진 촬영
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null && uiState.uploadedImages.size < 4) {
            viewModel.addImages(listOf(photoUri!!))
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 위에서 42px 떨어진 앱바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .offset(y = 42.dp)
                    .background(Color.White)
            ) {
                // back.png 버튼
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = 4.dp, y = 12.dp)
                        .clickable { handleBackPressed() }
                )

                // Title 박스
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 56.dp, vertical = 23.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "새로운 게시물",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
                        fontSize = 18.sp,
                        color = Color.Black
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
                        // 힌트 텍스트
                        if (textContent.isEmpty()) {
                            Text(
                                text = "새로운 소식이 있나요?",
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(400),
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
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 수정 모드: 기존 이미지 표시
                    if (isEditMode && uiState.existingImageUrls.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(uiState.existingImageUrls) { imageUrl ->
                                Box(
                                    modifier = Modifier.size(250.dp)
                                ) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "기존 이미지",
                                        modifier = Modifier
                                            .size(250.dp)
                                            .background(
                                                Color.Gray.copy(alpha = 0.1f),
                                                RoundedCornerShape(8.dp)
                                            )
                                    )
                                }
                            }
                        }
                        
                        // 이미지 변경 불가 안내
                        Text(
                            text = "※ 수정 시 이미지는 변경할 수 없습니다",
                            fontFamily = PretendardFont,
                            fontWeight = FontWeight(400),
                            fontSize = 12.sp,
                            color = Color(0xFF6B7684),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    // 작성 모드: 업로드된 이미지들 (가로 스크롤)
                    if (!isEditMode && uiState.uploadedImages.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(uiState.uploadedImages) { imageUri ->
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

                                    // exclude.png 삭제 버튼
                                    Image(
                                        painter = painterResource(id = R.drawable.exclude),
                                        contentDescription = "이미지 삭제",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .offset(x = 215.dp, y = (-12).dp)
                                            .clickable {
                                                viewModel.removeImage(imageUri)
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Background Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .background(Color.White)
            ) {
                // image.png (갤러리) - 수정 모드에서는 비활성화
                if (!isEditMode) {
                    Image(
                        painter = painterResource(id = R.drawable.image),
                        contentDescription = "갤러리",
                        modifier = Modifier
                            .size(24.dp)
                            .offset(x = 22.dp, y = 19.dp)
                            .clickable {
                                if (uiState.uploadedImages.size < 4) {
                                    galleryLauncher.launch("image/*")
                                }
                            }
                    )

                    // photo_camera.png (카메라) - 수정 모드에서는 비활성화
                    Image(
                        painter = painterResource(id = R.drawable.photo_camera),
                        contentDescription = "카메라",
                        modifier = Modifier
                            .size(24.dp)
                            .offset(x = (22 + 24 + 18).dp, y = 19.dp)
                            .clickable {
                                if (uiState.uploadedImages.size < 4) {
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
                }

                // 게시하기 버튼 (클릭해도 아무 동작 안함)
                Box(
                    modifier = Modifier
                        .size(91.dp, 32.dp)
                        .offset(x = 318.dp, y = 15.dp)
                        .background(
                            color = if (textContent.isNotEmpty() && !uiState.isLoading)
                                Color(0xFF3182F6)
                            else
                                Color(0x403182F6),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable(enabled = textContent.isNotEmpty() && !uiState.isLoading) {
                            if (isEditMode) {
                                viewModel.updatePost(textContent)
                            } else {
                                viewModel.createPost(textContent)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isEditMode) "수정하기" else "게시하기",
                            fontFamily = PretendardFont,
                            fontWeight = FontWeight(400),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                // 글자수 카운트
                if (textContent.isNotEmpty()) {
                    Text(
                        text = "${textContent.length}/$maxCharacters",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
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
                    // 검은색 배경 (32% 불투명도)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.32f))
                            .clickable { showCancelDialog = false }
                    )
                    
                    // 외부 박스
                    Box(
                        modifier = Modifier
                            .size(336.dp, 181.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(26.dp)
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
                                // Title1
                                Text(
                                    text = if (isEditMode) "게시물 수정을 취소할까요?" else "게시물 작성을 취소할까요?",
                                    fontFamily = PretendardFont,
                                    fontWeight = FontWeight(600),
                                    fontSize = 21.sp,
                                    color = Color(0xFF333D4B)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Title2
                                Text(
                                    text = if (isEditMode) "지금까지 수정한 내용은 저장되지 않아요." else "지금까지 쓴 내용은 저장되지 않아요.",
                                    fontFamily = PretendardFont,
                                    fontWeight = FontWeight(500),
                                    fontSize = 16.sp,
                                    color = Color(0xFF6B7684)
                                )
                            }

                            Spacer(modifier = Modifier.height(21.dp))

                            // 버튼들
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 닫기 버튼
                                Box(
                                    modifier = Modifier
                                        .width(146.dp)
                                        .height(58.dp)
                                        .background(
                                            color = Color(0xFFF3F4F6),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { showCancelDialog = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "닫기",
                                        fontFamily = PretendardFont,
                                        fontWeight = FontWeight(600),
                                        fontSize = 18.sp,
                                        color = Color(0xFF4E5968)
                                    )
                                }

                                // 나가기 버튼
                                Box(
                                    modifier = Modifier
                                        .width(146.dp)
                                        .height(58.dp)
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
                                        text = "나가기",
                                        fontFamily = PretendardFont,
                                        fontWeight = FontWeight(600),
                                        fontSize = 18.sp,
                                        color = Color.White
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