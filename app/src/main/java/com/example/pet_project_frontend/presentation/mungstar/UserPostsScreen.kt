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
    
    Column(modifier = Modifier.fillMaxSize()) {
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
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                // 사용자 정보 박스 (헤더 아래 28px)
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    // 첫 번째 게시물의 작성자 정보를 표시 (모든 게시물이 같은 작성자)
                    uiState.posts.firstOrNull()?.let { firstPost ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 사용자 정보 (왼쪽)
                            Column(modifier = Modifier.weight(1f)) {
                                // 반려견 이름 (#000, 25px)
                                Text(
                                    text = firstPost.pet?.name ?: firstPost.author.displayName,
                                    fontFamily = PretendardFont,
                                    fontWeight = FontWeight(400),
                                    fontSize = 25.sp,
                                    color = Color(0xFF000000)
                                )
                                
                                // 품종과 나이 (#8B95A1, 14px)
                                firstPost.pet?.let {
                                    Text(
                                        text = "${it.breed} • ${it.age}살",
                                        fontFamily = PretendardFont,
                                        fontWeight = FontWeight(400),
                                        fontSize = 14.sp,
                                        color = Color(0xFF8B95A1)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(190.dp))
                            
                            // 프로필 사진 (61X61, 오른쪽)
                            AsyncImage(
                                model = firstPost.pet?.profileImageUrl ?: firstPost.author.profilePictureUrl,
                                contentDescription = "프로필",
                                modifier = Modifier
                                    .size(61.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 게시물 카운트 정보
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // "게시물" 텍스트 (#8B95A1, 14px)
                        Text(
                            text = "게시물",
                            fontFamily = PretendardFont,
                            fontWeight = FontWeight(400),
                            fontSize = 14.sp,
                            color = Color(0xFF8B95A1)
                        )
                        
                        // 게시물 개수 (#4E5968, 17px)
                        Text(
                            text = "${uiState.posts.size}",
                            fontFamily = PretendardFont,
                            fontWeight = FontWeight(400),
                            fontSize = 17.sp,
                            color = Color(0xFF4E5968)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(26.dp))
                }
                
                // 게시물 목록
                items(uiState.posts) { post ->
                    PostItem(
                        post = post,
                        onLikeClick = { viewModel.toggleLike(post.postId) },
                        onPostClick = { navController.navigate("post_detail/${post.postId}") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
                
                // 게시물이 없을 때
                if (uiState.posts.isEmpty() && !uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "아직 작성한 게시물이 없어요.",
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(400),
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
