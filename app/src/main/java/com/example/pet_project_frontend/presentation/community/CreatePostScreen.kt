package com.example.pet_project_frontend.presentation.community

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.io.File

/**
 * 게시글 작성 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }
    
    // 카메라 런처
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.addImages(listOf(tempPhotoUri!!))
        }
    }
    
    // 게시글 생성 완료 시 뒤로가기
    LaunchedEffect(uiState.isPostCreated) {
        if (uiState.isPostCreated) {
            onPostCreated()
            viewModel.resetState()
        }
    }
    
    // 에러 메시지 표시
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("오류") },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("확인")
                }
            }
        )
    }
    
    // 제출 중 로딩 다이얼로그
    if (uiState.isSubmitting) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("게시글 업로드 중") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(uiState.currentStep ?: "처리 중...")
                }
            },
            confirmButton = {
                if (uiState.cartoonJobId != null) {
                    TextButton(onClick = { viewModel.cancelCartoonJob() }) {
                        Text("취소")
                    }
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새로운 게시물") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 선택된 이미지 목록
            if (uiState.imageUris.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.imageUris) { uri ->
                        ImageItem(
                            uri = uri,
                            onRemove = { viewModel.removeImage(uri) }
                        )
                    }
                }
            }
            
            // 텍스트 입력
            OutlinedTextField(
                value = uiState.text,
                onValueChange = { viewModel.updateText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp),
                placeholder = { Text("무슨 생각을 하고 계신가요?") },
                maxLines = 10,
                supportingText = {
                    Text(
                        text = "${uiState.text.length} / 2000",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 만화 모드 토글
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { viewModel.toggleCartoonMode() }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (uiState.isCartoonMode) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Column {
                        Text(
                            text = "만화 변환",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "첫 번째 이미지를 만화 스타일로 변환합니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = uiState.isCartoonMode,
                    onCheckedChange = { viewModel.toggleCartoonMode() }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 하단 버튼 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 갤러리 버튼
                OutlinedButton(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("갤러리")
                }
                
                // 카메라 버튼
                OutlinedButton(
                    onClick = {
                        val uri = createTempPhotoUri(context)
                        tempPhotoUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("카메라")
                }
            }
            
            // 게시하기 버튼
            Button(
                onClick = {
                    viewModel.submitPost { uri ->
                        getFileFromUri(context, uri)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                enabled = uiState.text.isNotBlank() && 
                         uiState.imageUris.isNotEmpty() && 
                         !uiState.isSubmitting
            ) {
                Text(
                    text = "게시하기",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ImageItem(
    uri: Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 삭제 버튼
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "삭제",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Uri로부터 File 객체 가져오기
 */
private fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(filePathColumn[0])
                val filePath = it.getString(columnIndex)
                File(filePath)
            } else {
                // ContentResolver로 복사
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 카메라 촬영용 임시 Uri 생성
 */
private fun createTempPhotoUri(context: Context): Uri {
    val tempFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
