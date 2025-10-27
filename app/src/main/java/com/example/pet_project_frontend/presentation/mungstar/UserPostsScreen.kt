package com.example.pet_project_frontend.presentation.mungstar

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun UserPostsScreen(
    authorId: String,
    navController: NavController,
    viewModel: UserPostsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    
    // 화면 진입 시 게시물 로드
    LaunchedEffect(authorId) {
        viewModel.loadUserPosts(authorId, refresh = true)
    }
    
    // 에러 표시
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    // 무한 스크롤
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .filter { lastIndex ->
                lastIndex != null && lastIndex >= uiState.posts.size - 3 && uiState.hasMore
            }
            .collect {
                viewModel.loadMore()
            }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 헤더 바 (412X64)
        Box(
            modifier = Modifier
                .width(412.dp)
                .height(64.dp)
                .background(Color.White)
        ) {
            // back.png (40X40) 왼쪽
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .clickable { navController.popBackStack() }
            )
        }
        
        if (uiState.isLoading && uiState.posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // 사용자 정보 앱바 (174dp 높이) - 항상 표시
                uiState.userInfo?.let { userInfo ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(174.dp)
                            .background(Color.White)
                            .padding(horizontal = 25.dp, vertical = 28.dp)
                    ) {
                        // 왼쪽: 사용자 정보
                        Column(
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            // 사용자 이름 + 인증 배지
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = userInfo.petName ?: userInfo.displayName,
                                    fontFamily = PretendardFont,
                                    fontWeight = FontWeight(600),
                                    fontSize = 25.sp,
                                    color = Color(0xFF000000)
                                )
                                
                                // 신원 인증 배지 (is_verified = true일 때만 표시)
                                if (userInfo.isVerified == true) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Image(
                                        painter = painterResource(id = R.drawable.badge),
                                        contentDescription = "신원 인증",
                                        modifier = Modifier.size(16.dp, 17.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 견종과 나이 (#8B95A1, 14px, weight 400)
                            if (userInfo.breed != null && userInfo.age != null) {
                                Text(
                                    text = "${userInfo.breed} • ${userInfo.age}살",
                                    fontFamily = PretendardFont,
                                    fontWeight = FontWeight(400),
                                    fontSize = 14.sp,
                                    color = Color(0xFF8B95A1)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // 게시물 정보
                            Column {
                                // "게시물" 텍스트 (#8B95A1, 14px, weight 500)
                                Text(
                                    text = "게시물",
                                    fontFamily = PretendardFont,
                                    fontWeight = FontWeight(500),
                                    fontSize = 14.sp,
                                    color = Color(0xFF8B95A1)
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // 게시물 개수 (#4E5968, 17px, weight 500)
                                Text(
                                    text = "${uiState.posts.size}",
                                    fontFamily = PretendardFont,
                                    fontWeight = FontWeight(500),
                                    fontSize = 17.sp,
                                    color = Color(0xFF4E5968)
                                )
                            }
                        }
                        
                        // 오른쪽: 프로필 사진 (61X61, 오른쪽에서 25픽셀 떨어진 위치 - 패딩 이미 적용됨)
                        AsyncImage(
                            model = userInfo.profileImageUrl,
                            contentDescription = "프로필",
                            modifier = Modifier
                                .size(61.dp)
                                .clip(CircleShape)
                                .align(Alignment.TopEnd),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // 구분선 (#E4E8EB, 1dp)
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = Color(0xFFE4E8EB)
                    )
                }
                
                // 게시물이 없을 때 - 화면 중앙에 표시
                if (uiState.posts.isEmpty() && !uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // no_post.png 이미지 (60X65)
                            Image(
                                painter = painterResource(id = R.drawable.no_post),
                                contentDescription = "게시물 없음",
                                modifier = Modifier.size(width = 60.dp, height = 65.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // 안내 텍스트
                            Text(
                                text = "아직 게시물을 남기지 않았어요.",
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(400),
                                fontSize = 16.sp,
                                color = Color(0xFF6B7684)
                            )
                        }
                    }
                } else {
                    // 게시물이 있을 때 - LazyColumn으로 스크롤 가능하게
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White)
                    ) {
                
                        // 게시물 목록
                        items(uiState.posts) { post ->
                            PostItem(
                                post = post,
                                onLikeClick = { viewModel.toggleLike(post.postId) },
                                onPostClick = { navController.navigate("post_detail/${post.postId}") }
                            )
                            
                            // 게시물 사이 구분선 (#E4E8EB, 1dp)
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = Color(0xFFE4E8EB)
                            )
                        }
                        
                        // 로딩 인디케이터
                        if (uiState.isLoading && uiState.posts.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
