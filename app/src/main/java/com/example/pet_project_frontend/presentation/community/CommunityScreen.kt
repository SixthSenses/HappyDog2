package com.example.pet_project_frontend.presentation.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.presentation.community.components.PostCard

/**
 * 커뮤니티(멍스타그램) 피드 화면
 * MVVM + UDF 패턴: StateFlow 구독 및 이벤트 전달
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // 스크롤이 끝에 도달하면 더 불러오기
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val state = uiState as? CommunityUiState.Success ?: return@collect
                val totalItems = state.posts.size
                if (lastVisibleIndex != null && lastVisibleIndex >= totalItems - 3 && !state.isLoadingMore && state.nextCursor != null) {
                    viewModel.loadMorePosts()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("멍스타그램") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreatePost,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "새 게시글 작성"
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CommunityUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is CommunityUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "아직 게시글이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "첫 번째 게시글을 작성해보세요!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is CommunityUiState.Success -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(
                        items = state.posts,
                        key = { it.postId }
                    ) { post ->
                        var showBottomSheet by remember { mutableStateOf(false) }

                        PostCard(
                            post = post,
                            onPostClick = { onNavigateToPostDetail(post.postId) },
                            onLikeClick = { viewModel.togglePostLike(post.postId) },
                            onCommentClick = { onNavigateToPostDetail(post.postId) },
                            onAuthorClick = { onNavigateToUserProfile(post.author.userId) },
                            onMoreClick = { showBottomSheet = true }
                        )

                        Divider()

                        // 더보기 옵션 바텀시트
                        if (showBottomSheet) {
                            ModalBottomSheet(
                                onDismissRequest = { showBottomSheet = false }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                ) {
                                    TextButton(
                                        onClick = {
                                            viewModel.deletePost(post.postId)
                                            showBottomSheet = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("삭제", color = MaterialTheme.colorScheme.error)
                                    }
                                    TextButton(
                                        onClick = { showBottomSheet = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("취소")
                                    }
                                }
                            }
                        }
                    }

                    // 더 불러오기 인디케이터
                    if (state.isLoadingMore) {
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

            is CommunityUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("다시 시도")
                        }
                    }
                }
            }
        }
    }
}
