package com.example.pet_project_frontend.presentation.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.presentation.community.components.CommentInput
import com.example.pet_project_frontend.presentation.community.components.CommentItem
import com.example.pet_project_frontend.presentation.community.components.PostCard

/**
 * 게시글 상세 + 댓글 화면
 * MVVM + UDF 패턴
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // 스크롤이 끝에 도달하면 댓글 더 불러오기
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val state = uiState as? PostDetailUiState.Success ?: return@collect
                val totalItems = 1 + state.comments.size // 게시글 1개 + 댓글 개수
                if (lastVisibleIndex != null && lastVisibleIndex >= totalItems - 3 && !state.isLoadingMoreComments && state.nextCursor != null) {
                    viewModel.loadMoreComments()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게시글") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (uiState is PostDetailUiState.Success) {
                val state = uiState as PostDetailUiState.Success
                CommentInput(
                    onSendComment = { text ->
                        viewModel.submitComment(text) {}
                    },
                    isSubmitting = state.isSubmittingComment
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is PostDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is PostDetailUiState.Success -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 게시글
                    item {
                        var showPostBottomSheet by remember { mutableStateOf(false) }

                        PostCard(
                            post = state.post,
                            onPostClick = {},
                            onLikeClick = { viewModel.togglePostLike() },
                            onCommentClick = {},
                            onAuthorClick = { onNavigateToUserProfile(state.post.author.userId) },
                            onMoreClick = { showPostBottomSheet = true }
                        )

                        if (showPostBottomSheet) {
                            ModalBottomSheet(
                                onDismissRequest = { showPostBottomSheet = false }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                ) {
                                    TextButton(
                                        onClick = { showPostBottomSheet = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("신고")
                                    }
                                    TextButton(
                                        onClick = { showPostBottomSheet = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("취소")
                                    }
                                }
                            }
                        }

                        Divider(thickness = 8.dp)
                    }

                    // 댓글 헤더
                    item {
                        Text(
                            text = "댓글 ${state.comments.size}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // 댓글 리스트
                    items(
                        items = state.comments,
                        key = { it.commentId }
                    ) { comment ->
                        var showCommentBottomSheet by remember { mutableStateOf(false) }

                        CommentItem(
                            comment = comment,
                            onLikeClick = { viewModel.toggleCommentLike(comment.commentId) },
                            onAuthorClick = { onNavigateToUserProfile(comment.author.userId) },
                            onMoreClick = { showCommentBottomSheet = true }
                        )

                        if (showCommentBottomSheet) {
                            ModalBottomSheet(
                                onDismissRequest = { showCommentBottomSheet = false }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                ) {
                                    TextButton(
                                        onClick = {
                                            viewModel.deleteComment(comment.commentId)
                                            showCommentBottomSheet = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("삭제", color = MaterialTheme.colorScheme.error)
                                    }
                                    TextButton(
                                        onClick = { showCommentBottomSheet = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("취소")
                                    }
                                }
                            }
                        }
                    }

                    // 댓글 더 불러오기 인디케이터
                    if (state.isLoadingMoreComments) {
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

            is PostDetailUiState.Error -> {
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
