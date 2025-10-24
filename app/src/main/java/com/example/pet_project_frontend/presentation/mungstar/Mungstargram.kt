package com.example.pet_project_frontend.presentation.mungstar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.PretendardFont
import com.example.pet_project_frontend.domain.model.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MungStarFeed(
    navController: NavController,
    viewModel: MungStarViewModel = hiltViewModel()
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var showCartoonToast by remember { mutableStateOf(false) }
    var showDeleteToast by remember { mutableStateOf(false) }
    var showUpdateToast by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // 화면이 다시 보일 때마다 피드 새로고침
    val currentBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(currentBackStackEntry) {
        viewModel.refresh()
        
        // Free_Writing에서 돌아왔을 때 토스트 표시
        val postCreated = currentBackStackEntry?.savedStateHandle?.get<Boolean>("post_created")
        if (postCreated == true) {
            showSuccessToast = true
            currentBackStackEntry.savedStateHandle.remove<Boolean>("post_created")
            kotlinx.coroutines.delay(2000)
            showSuccessToast = false
        }
        
        // CartoonLoading에서 돌아왔을 때 만화 등록 토스트 표시
        val cartoonCreated = currentBackStackEntry?.savedStateHandle?.get<Boolean>("cartoon_created")
        if (cartoonCreated == true) {
            showCartoonToast = true
            currentBackStackEntry.savedStateHandle.remove<Boolean>("cartoon_created")
            kotlinx.coroutines.delay(2000)
            showCartoonToast = false
        }
        
        // PostDetail에서 돌아왔을 때 삭제 토스트 표시
        val postDeleted = currentBackStackEntry?.savedStateHandle?.get<Boolean>("post_deleted")
        if (postDeleted == true) {
            showDeleteToast = true
            currentBackStackEntry.savedStateHandle.remove<Boolean>("post_deleted")
            kotlinx.coroutines.delay(2000)
            showDeleteToast = false
        }
        
        // Free_Writing에서 돌아왔을 때 수정 토스트 표시
        val postUpdated = currentBackStackEntry?.savedStateHandle?.get<Boolean>("post_updated")
        if (postUpdated == true) {
            showUpdateToast = true
            currentBackStackEntry.savedStateHandle.remove<Boolean>("post_updated")
            kotlinx.coroutines.delay(2000)
            showUpdateToast = false
        }
    }

    // 스크롤 끝 감지하여 더 로드
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - 3 // 끝에서 3개 전
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && uiState.hasMore && !uiState.isLoading) {
                viewModel.loadMore()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 앱바 (디바이스 너비 × 64픽셀)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .offset(y = 10.dp)
                    .background(Color.White)
            ) {
                // mungstargram.png (왼쪽에서 20픽셀, 수직 중앙)
                Image(
                    painter = painterResource(id = R.drawable.mungstargram),
                    contentDescription = "MungStarGram Logo",
                    modifier = Modifier
                        .size(132.dp, 32.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = 20.dp),
                    contentScale = ContentScale.Fit
                )

                // 오른쪽 아이콘 그룹
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // notifications.png (22×22)
                    Image(
                        painter = painterResource(id = R.drawable.notifications),
                        contentDescription = "Notifications",
                        modifier = Modifier.size(22.dp),
                        contentScale = ContentScale.Fit
                    )

                    // person.png (20×20) - 현재 사용자 프로필로 이동
                    Image(
                        painter = painterResource(id = R.drawable.person),
                        contentDescription = "My Profile",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                uiState.currentUserId?.let { userId ->
                                    navController.navigate("user_posts/$userId")
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // 앱바 아래 8픽셀 간격
            Spacer(modifier = Modifier.height(8.dp))

            // 앱바 아래 구분선
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color(0xFFE4E8EB)
            )

            // 피드 목록
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "아직 게시물이 없습니다",
                            fontFamily = PretendardFont,
                            fontWeight = FontWeight(400),
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "첫 번째 게시물을 작성해보세요!",
                            fontFamily = PretendardFont,
                            fontWeight = FontWeight(400),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                items(uiState.posts) { post ->
                    PostItem(
                        post = post,
                        onLikeClick = { viewModel.toggleLike(post.postId) },
                        onPostClick = { navController.navigate("post_detail/${post.postId}") },
                        onAuthorClick = { navController.navigate("user_posts/${post.author.userId}") }
                    )
                    // 게시물 간 구분선
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = Color(0xFFE4E8EB)
                    )
                }
                
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
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
                modifier = Modifier.size(21.dp),
                contentScale = ContentScale.Fit
            )
        }

        // 바텀 시트
        if (showBottomSheet) {
            CustomBottomSheet(
                navController = navController,
                onDismiss = { showBottomSheet = false }
            )
        }
        
        // 커스텀 성공 토스트
        if (showSuccessToast) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 78.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .width(203.dp)
                        .height(50.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .border(
                            width = 0.5.dp,
                            color = Color(0xFFE4E8EB),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .background(
                            color = Color(0xFFFFFFFF),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 초록색 원 배경 + check 아이콘
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = Color(0xFF15C47E),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = "등록 완료",
                            modifier = Modifier.size(width = 9.dp, height = 7.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Text(
                        text = "게시물이 등록되었어요.",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(500),
                        fontSize = 16.sp,
                        color = Color(0xFF333D4B)
                    )
                }
            }
        }
        
        // 만화 등록 완료 토스트
        if (showCartoonToast) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 78.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .width(203.dp)
                        .height(50.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .border(
                            width = 0.5.dp,
                            color = Color(0xFFE4E8EB),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .background(
                            color = Color(0xFFFFFFFF),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 초록색 원 배경 + check 아이콘
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = Color(0xFF15C47E),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = "완료",
                            modifier = Modifier.size(width = 9.dp, height = 7.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Text(
                        text = "만화가 등록되었어요.",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(500),
                        fontSize = 16.sp,
                        color = Color(0xFF333D4B)
                    )
                }
            }
        }
        
        // 게시물 삭제 완료 토스트
        if (showDeleteToast) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 78.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .width(203.dp)
                        .height(50.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .border(
                            width = 0.5.dp,
                            color = Color(0xFFE4E8EB),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .background(
                            color = Color(0xFFFFFFFF),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 초록색 원 배경 + check 아이콘
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = Color(0xFF15C47E),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = "삭제 완료",
                            modifier = Modifier.size(width = 9.dp, height = 7.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Text(
                        text = "게시물이 삭제되었어요.",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(500),
                        fontSize = 16.sp,
                        color = Color(0xFF333D4B)
                    )
                }
            }
        }
        
        // 게시물 수정 완료 토스트
        if (showUpdateToast) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 78.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .width(203.dp)
                        .height(50.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .border(
                            width = 0.5.dp,
                            color = Color(0xFFE4E8EB),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .background(
                            color = Color(0xFFFFFFFF),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 초록색 원 배경 + check 아이콘
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = Color(0xFF15C47E),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = "수정 완료",
                            modifier = Modifier.size(width = 9.dp, height = 7.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Text(
                        text = "게시물이 수정되었어요.",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(500),
                        fontSize = 16.sp,
                        color = Color(0xFF333D4B)
                    )
                }
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    onLikeClick: () -> Unit,
    onPostClick: () -> Unit,
    onAuthorClick: (() -> Unit)? = null // 작성자 클릭 시 호출 (optional)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(26.dp)
    ) {
        // 작성자 정보
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 디버깅 로그
            android.util.Log.d("MungstarPost", "Post ${post.postId}: pet=${post.pet?.name}, profileImageUrl=${post.pet?.profileImageUrl}")
            
            // 프로필 이미지 (클릭 시 사용자 게시물 페이지로 이동)
            if (post.pet?.profileImageUrl != null) {
                AsyncImage(
                    model = post.pet.profileImageUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable { onAuthorClick?.invoke() },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .clickable { onAuthorClick?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.pet?.name?.firstOrNull()?.toString() ?: "?",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 이름과 품종 (클릭 시 사용자 게시물 페이지로 이동)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAuthorClick?.invoke() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 반려견 이름 (닉네임 - weight 500)
                    Text(
                        text = post.pet?.name ?: post.author.displayName,
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(500),
                        fontSize = 16.sp,
                        color = Color(0xFF6B7684)
                    )
                    
                    // 타임스탬프 (오른쪽, 이름과 같은 높이)
                    Text(
                        text = com.example.pet_project_frontend.util.TimeUtil.getRelativeTimeString(post.createdAt),
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
                        fontSize = 14.sp,
                        color = Color(0xFF8B95A1)
                    )
                }
                
                if (post.pet != null) {
                    Text(
                        text = "${post.pet.breed} • ${post.pet.age ?: ""}살",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
                        fontSize = 14.sp,
                        color = Color(0xFF8B95A1)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 게시글 텍스트 (사용자 정보 아래 20px, #333D4B, 17px, 클릭 시 상세 화면)
        Text(
            text = post.text,
            fontFamily = PretendardFont,
            fontWeight = FontWeight(400),
            fontSize = 17.sp,
            color = Color(0xFF333D4B),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onPostClick)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 이미지 (텍스트 아래 10px 간격)
        if (post.mediaUrls.isNotEmpty()) {
            if (post.mediaUrls.size == 1) {
                // 사진이 하나일 때: 핸드폰 너비에 맞춰 완전히 표시
                AsyncImage(
                    model = post.mediaUrls[0],
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // 정사각형 비율 유지
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .clickable(onClick = onPostClick),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 사진이 여러 개일 때: 가로 스크롤 (250x250)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(post.mediaUrls) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Post image",
                            modifier = Modifier
                                .size(250.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F5))
                                .clickable(onClick = onPostClick),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 좋아요와 댓글 수
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좋아요 버튼 - 사용자가 좋아요 눌렀을 때만 favorite.png (24dp), 아니면 like.png (22dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLikeClick() }
            ) {
                Image(
                    painter = painterResource(
                        id = if (post.isLiked) R.drawable.favorite else R.drawable.like
                    ),
                    contentDescription = "Like",
                    modifier = Modifier.size(if (post.isLiked) 24.dp else 22.dp)
                )
                
                // 좋아요 수 (1개부터 표시, #EC4453, 15px)
                if (post.likesCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likesCount.toString(),
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(500),
                        fontSize = 15.sp,
                        color = Color(0xFFEC4453)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 댓글 수 (24X24, 0개부터 표시, #B1B8C0, 15px)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onPostClick() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.comment),
                    contentDescription = "Comment",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.commentsCount.toString(),
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(500),
                    fontSize = 15.sp,
                    color = Color(0xFFB1B8C0)
                )
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
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 검은색 배경 (32% 불투명도) - 화면 전체
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(onClick = onDismiss)
            )
            
            Card(
                modifier = Modifier
                    .width(472.dp)  // 432 + 40 = 472
                    .wrapContentHeight()
                    .padding(bottom = 20.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // 닫기 바 (Divider) - 위에서 10픽셀 아래
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(4.dp)
                            .background(
                                Color(0xFFD1D5DB),
                                shape = RoundedCornerShape(2.dp)
                            )
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(47.dp))

                    // 어떤 주제인가요? 텍스트
                    Text(
                        text = "어떤 주제인가요?",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(600),
                        fontSize = 21.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // 자유글 컨테이너
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 8.dp)
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
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(500),
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = "자유글",
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(500),
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    // 만화로 일상기록하기 컨테이너
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 8.dp)
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
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(500),
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = "만화로 일상기록하기",
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(500),
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
