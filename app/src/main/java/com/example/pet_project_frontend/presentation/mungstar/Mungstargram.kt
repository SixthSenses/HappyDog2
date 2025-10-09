package com.example.pet_project_frontend.presentation.mungstar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.PretendardFont
import com.example.pet_project_frontend.domain.model.Post
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
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
            // 앱바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .offset(y = 10.dp)
                    .background(Color.White)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mungstargram),
                    contentDescription = "MungStarGram Logo",
                    modifier = Modifier
                        .size(132.dp, 32.dp)
                        .offset(x = 20.dp, y = 16.dp),
                    contentScale = ContentScale.Fit
                )

                Image(
                    painter = painterResource(id = R.drawable.notifications),
                    contentDescription = "Notifications",
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = (20 + 132 + 160).dp, y = 20.dp),
                    contentScale = ContentScale.Fit
                )

                Image(
                    painter = painterResource(id = R.drawable.person),
                    contentDescription = "Person",
                    modifier = Modifier
                        .size(30.dp)
                        .offset(x = (20 + 132 + 160 + 24 + 20).dp, y = 17.dp),
                    contentScale = ContentScale.Fit
                )
            }

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
                        .padding(top = 10.dp)
                ) {
                items(uiState.posts) { post ->
                    PostItem(
                        post = post,
                        onLikeClick = { viewModel.toggleLike(post.postId) },
                        onPostClick = { navController.navigate("post_detail/${post.postId}") },
                        onAuthorClick = { navController.navigate("user_posts/${post.author.userId}") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
                modifier = Modifier.size(30.dp),
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
                    .padding(top = 130.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .width(230.dp)
                        .height(50.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.registeration_check_icon),
                        contentDescription = "등록 완료",
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Text(
                        text = "게시물이 등록되었어요.",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
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
            .padding(horizontal = 16.dp)
    ) {
        // 작성자 정보
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    // 반려견 이름
                    Text(
                        text = post.pet?.name ?: post.author.displayName,
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
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

        // 이미지 (텍스트 아래 10px 간격, 여러 장일 경우 페이저, 클릭 시 상세 화면)
        if (post.mediaUrls.isNotEmpty()) {
            val pagerState = rememberPagerState(initialPage = 0)

            Box(modifier = Modifier.clickable(onClick = onPostClick)) {
                HorizontalPager(
                    count = post.mediaUrls.size,
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) { page ->
                    AsyncImage(
                        model = post.mediaUrls[page],
                        contentDescription = "Post image",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5)),
                        contentScale = ContentScale.Crop
                    )
                }

                // 페이지 인디케이터 (이미지가 2장 이상일 때만)
                if (post.mediaUrls.size > 1) {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        activeColor = Color.White,
                        inactiveColor = Color.White.copy(alpha = 0.5f),
                        indicatorWidth = 8.dp,
                        indicatorHeight = 8.dp,
                        spacing = 4.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 좋아요와 댓글 수
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좋아요 버튼 (24X24) - 0개일 때 like.png, 1개 이상일 때 favorite.png
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLikeClick() }
            ) {
                Image(
                    painter = painterResource(
                        id = if (post.likesCount > 0) R.drawable.favorite else R.drawable.like
                    ),
                    contentDescription = "Like",
                    modifier = Modifier.size(24.dp)
                )
                
                // 좋아요 수 (1개부터 표시, #EC4453, 15px)
                if (post.likesCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likesCount.toString(),
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
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
                    fontWeight = FontWeight(400),
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
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
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

                    Text(
                        text = "어떤 주제인가요?",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
                        modifier = Modifier
                            .offset(x = 27.dp, y = 42.dp),
                        fontSize = 21.sp,
                        color = Color.Black
                    )

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
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(400),
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = "자유글",
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(400),
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }
                    }

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
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(400),
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = "만화로 일상기록하기",
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(400),
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
