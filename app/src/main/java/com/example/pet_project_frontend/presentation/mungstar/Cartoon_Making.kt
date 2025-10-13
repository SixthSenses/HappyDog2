package com.example.pet_project_frontend.presentation.mungstar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.PretendardFont

@Composable
fun CartoonMaking(
    navController: NavController,
    viewModel: CartoonMakingViewModel = hiltViewModel()
) {
    var textContent by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 이미지 선택 런처
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    // jobId 생성되면 로딩 화면으로 이동
    LaunchedEffect(uiState.jobId) {
        val jobId = uiState.jobId
        if (jobId != null) {
            viewModel.resetJobId() // 재사용을 위해 초기화
            // Base64 인코딩하여 텍스트 안전하게 전달
            val encodedText = if (textContent.isNotBlank()) {
                android.util.Base64.encodeToString(
                    textContent.toByteArray(Charsets.UTF_8),
                    android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
                )
            } else {
                ""
            }
            navController.navigate("cartoon_loading/$jobId?userText=$encodedText")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding() // 키보드와 함께 올라가도록
    ) {
        // 상단 앱바 (디바이스 너비 × 64픽셀)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White)
        ) {
            // back.png (24X24, 왼쪽에서 16픽셀)
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 16.dp)
                    .clickable { navController.popBackStack() }
            )

            // 새로운 만화 생성하기 텍스트 (가운데, FontWeight 500)
            Text(
                text = "새로운 만화 생성하기",
                fontFamily = PretendardFont,
                fontWeight = FontWeight(500),
                fontSize = 18.sp,
                color = Color(0xFF333D4B),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 텍스트 입력 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 25.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            TextField(
                value = textContent,
                onValueChange = { textContent = it },
                modifier = Modifier.fillMaxSize(),
                placeholder = {
                    Text(
                        text = "아침 산책, 놀이, 식사 등 오늘 반려견과 있었던 일을\n적고 사진을 함께 첨부해보세요.",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
                        fontSize = 16.sp,
                        color = Color(0xFF8B95A1)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(400),
                    fontSize = 16.sp,
                    color = Color(0xFF333D4B)
                )
            )
        }

        // 구분선
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFF8B95A1)
        )

        // 하단 앱바 (412X52)
        Box(
            modifier = Modifier
                .width(412.dp)
                .height(52.dp)
                .background(Color.White)
        ) {
            // 왼쪽 아이콘들 (image.png, photo_camera.png)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // image.png (28X28)
                Image(
                    painter = painterResource(id = R.drawable.image),
                    contentDescription = "이미지 선택",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { galleryLauncher.launch("image/*") }
                )

                // photo_camera.png (28X28)
                Image(
                    painter = painterResource(id = R.drawable.photo_camera),
                    contentDescription = "카메라",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { /* TODO: 카메라 기능 */ }
                )
            }

            // 오른쪽 만화 생성하기 버튼 (125X32, 오른쪽에서 18픽셀)
            // 텍스트 + 이미지 모두 필요 (백엔드 file_paths 필수)
            val isActive = textContent.isNotBlank() && uiState.selectedImages.isNotEmpty()
            Box(
                modifier = Modifier
                    .width(125.dp)
                    .height(32.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-18).dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        color = if (isActive) Color(0xFF3182F6) else Color(49, 130, 246, (0.25f * 255).toInt())
                    )
                    .clickable(enabled = isActive) {
                        if (uiState.selectedImages.isEmpty()) {
                            android.widget.Toast.makeText(
                                context,
                                "이미지를 선택해주세요",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.generateCartoon(textContent)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "만화 생성하기",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(600),
                    fontSize = 18.sp,
                    color = Color(0xFFFFFFFF)
                )
            }
        }
    }
}