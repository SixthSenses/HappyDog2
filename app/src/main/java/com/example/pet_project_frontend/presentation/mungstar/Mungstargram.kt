package com.example.pet_project_frontend.presentation.mungstar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.data.mungstar_model.CreatePostResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.pet_project_frontend.data.mungstar_model.MungstarPostRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MungStarFeed(navController: NavController) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var posts by remember { mutableStateOf<List<CreatePostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val postRepository = remember { MungstarPostRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    // 게시물 목록을 가져오는 함수 (TODO: API 구현 필요)
    fun loadPosts() {
        coroutineScope.launch {
            isLoading = true
            try {
                // TODO: PostRepository에 getPosts() 메서드 추가 필요
                // val response = postRepository.getPosts()
                // posts = response

                // 임시로 빈 리스트
                posts = emptyList()
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // 화면 진입 시 게시물 로드
    LaunchedEffect(Unit) {
        loadPosts()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 앱바 (412x64, 화면 상단에서 10px 아래)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .offset(y = 10.dp)
                    .background(Color.White)
            ) {
                // 로고 (132x32, 좌측에서 20px, 상단에서 16px)
                Image(
                    painter = painterResource(id = R.drawable.mungstargram),
                    contentDescription = "MungStarGram Logo",
                    modifier = Modifier
                        .size(132.dp, 32.dp)
                        .offset(x = 20.dp, y = 16.dp),
                    contentScale = ContentScale.Fit
                )

                // notifications 아이콘 (로고에서 오른쪽으로 160px)
                Image(
                    painter = painterResource(id = R.drawable.notifications),
                    contentDescription = "Notifications",
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = (20 + 132 + 160).dp, y = 20.dp),
                    contentScale = ContentScale.Fit
                )

                // person 아이콘 (notifications에서 오른쪽으로 20px, 30x30 크기)
                Image(
                    painter = painterResource(id = R.drawable.person),
                    contentDescription = "Person",
                    modifier = Modifier
                        .size(30.dp)
                        .offset(x = (20 + 132 + 160 + 24 + 20).dp, y = 17.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // 메인 컨텐츠 영역 - 게시물 리스트
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 10.dp)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "오류가 발생했습니다",
                                fontSize = 16.sp,
                                color = Color.Red
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { loadPosts() }) {
                                Text("다시 시도")
                            }
                        }
                    }
                    posts.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "아직 게시물이 없습니다",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "첫 번째 게시물을 작성해보세요!",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(posts) { post ->
                                PostItem(
                                    post = post,
                                    onLikeClick = {
                                        // TODO: 좋아요 API 호출
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 플로팅 액션 버튼
        FloatingActionButton(
            onClick = { showBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-15).dp)
                .size(58.dp),
            shape = CircleShape,
            containerColor = Color(0xFF333D4B),
            contentColor = Color.White
        ) {
            Image(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "Add",
                modifier = Modifier.size(30.dp),
                contentScale = ContentScale.Fit
            )
        }

        // 커스텀 바텀 시트
        if (showBottomSheet) {
            CustomBottomSheet(
                navController = navController,
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}

@Composable
fun PostItem(
    post: CreatePostResponse,
    onLikeClick: () -> Unit
) {
    // 게시물 피드 (412x458)
    Card(
        modifier = Modifier
            .size(412.dp, 458.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                // 작성자 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 프로필 이미지
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF333D4B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.author.nickname.firstOrNull()?.toString() ?: "?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = post.author.nickname,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = post.pet.name,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = formatDate(post.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 게시물 텍스트
                if (post.text.isNotBlank()) {
                    Text(
                        text = post.text,
                        fontSize = 16.sp,
                        color = Color.Black,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 이미지들
                if (post.imageUrls.isNotEmpty()) {
                    when (post.imageUrls.size) {
                        1 -> {
                            AsyncImage(
                                model = post.imageUrls[0],
                                contentDescription = "게시물 이미지",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            var currentImageIndex by remember { mutableStateOf(0) }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                // 현재 이미지 표시
                                AsyncImage(
                                    model = post.imageUrls[currentImageIndex],
                                    contentDescription = "게시물 이미지 ${currentImageIndex + 1}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                // 이미지 슬라이더 (작은 이미지들)
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(post.imageUrls.size) { index ->
                                        AsyncImage(
                                            model = post.imageUrls[index],
                                            contentDescription = "썸네일 ${index + 1}",
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { currentImageIndex = index }
                                                .then(
                                                    if (index == currentImageIndex)
                                                        Modifier.padding(2.dp)
                                                    else
                                                        Modifier
                                                ),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 좋아요, 댓글 아이콘 (왼쪽 하단에 위치)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 좋아요 아이콘
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.like),
                        contentDescription = "좋아요",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likeCount.toString(),
                        fontSize = 14.sp,
                        color = if (post.isLiked) Color.Red else Color.Gray
                    )
                }

                // 댓글 아이콘
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.comment),
                        contentDescription = "댓글",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.commentCount.toString(),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomSheet(
    navController: NavController,
    onDismiss: () -> Unit
) {
    var dragOffset by remember { mutableStateOf(0f) }
    var selectedContainer by remember { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 바텀 시트 (392x173, 화면 아래에서 36px 위)
            Card(
                modifier = Modifier
                    .size(392.dp, 173.dp)
                    .offset(y = (-36).dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            dragOffset += dragAmount.y
                            if (dragOffset > 100f) {
                                onDismiss()
                            }
                        }
                    },
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Divider (텍스트 시트 안에서 아래로 12px)
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                Color.Gray,
                                shape = RoundedCornerShape(2.dp)
                            )
                            .align(Alignment.TopCenter)
                            .offset(y = 12.dp)
                            .clickable { onDismiss() }
                    )

                    // Title (위로 42px, 왼쪽으로 27px, 21px 크기)
                    Text(
                        text = "어떤 주제인가요?",
                        modifier = Modifier
                            .offset(x = 27.dp, y = 42.dp),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // 첫 번째 컨테이너 박스 (376x48, title에서 20px 아래)
                    Box(
                        modifier = Modifier
                            .size(376.dp, 48.dp)
                            .offset(x = 8.dp, y = (42 + 20).dp)
                            .background(
                                color = if (selectedContainer == 1) Color(0xFFF3F4F6) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedContainer = 1
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(150)
                                    onDismiss()
                                    navController.navigate("free_writing")
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💬",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = "자유글",
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }
                    }

                    // 두 번째 컨테이너 박스 (376x48, 첫 번째에서 아래로 15px)
                    Box(
                        modifier = Modifier
                            .size(376.dp, 48.dp)
                            .offset(x = 8.dp, y = (42 + 20 + 48 + 15).dp)
                            .background(
                                color = if (selectedContainer == 2) Color(0xFFF3F4F6) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedContainer = 2
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(150)
                                    onDismiss()
                                    navController.navigate("cartoon_making")
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎨",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = "만화로 일상기록하기",
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// 날짜 포맷 함수 - 타임스탬프 표시 규칙 적용
private fun formatDate(dateString: String): String {
    return try {
        // ISO 8601 형식의 날짜 문자열을 파싱
        val postTime = java.time.ZonedDateTime.parse(dateString.replace("T", "T").replace(" ", "T"))
        val now = java.time.ZonedDateTime.now()

        val duration = java.time.Duration.between(postTime, now)
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()
        val weeks = days / 7
        val years = days / 365

        when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            days < 7 -> "${days}일 전"
            weeks < 48 -> "${weeks}주 전"
            else -> "${years}년 전"
        }
    } catch (e: Exception) {
        try {
            // 백업: 간단한 날짜 포맷
            val parts = dateString.split("T")[0].split("-")
            if (parts.size >= 3) {
                "${parts[1]}월 ${parts[2]}일"
            } else {
                dateString.substring(0, minOf(10, dateString.length))
            }
        } catch (ex: Exception) {
            dateString
        }
    }
}