package com.example.pet_project_frontend.presentation.mungstar

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.PretendardFont
import com.example.pet_project_frontend.util.TimeUtil
import com.example.pet_project_frontend.domain.model.Comment
import com.example.pet_project_frontend.domain.model.Post
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PostDetailScreen(
    postId: String,
    navController: NavController,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    
    // 게시글 로드
    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
    }
    
    // 에러 표시
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // 키보드와 함께 올라가도록
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 헤더 바 (412X64)
            Box(
                modifier = Modifier
                    .width(412.dp)
                    .height(64.dp)
                    .background(Color.White)
            ) {
                // back.png (24X24) 왼쪽
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .clickable { navController.popBackStack() }
                )
                
                // "게시물" 텍스트 가운데
                Text(
                    text = "게시물",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(400),
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                uiState.post?.let { post ->
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // 게시글 아이템
                        item {
                            PostDetailItem(
                                post = post,
                                onLikeClick = { viewModel.togglePostLike() },
                                onMoreClick = { viewModel.showMoreMenu() },
                                onAuthorClick = { navController.navigate("user_posts/${post.author.userId}") }
                            )
                        }
                        
                        // 댓글이 없을 때 메시지
                        if (uiState.comments.isEmpty() && !uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "아직 댓글이 없어요.",
                                        fontFamily = PretendardFont,
                                        fontWeight = FontWeight(400),
                                        fontSize = 16.sp,
                                        color = Color(0xFF6B7684)
                                    )
                                }
                            }
                        }
                        
                        // 댓글 목록
                        items(uiState.comments) { comment ->
                            CommentItem(
                                comment = comment,
                                onLikeClick = { viewModel.toggleCommentLike(comment.commentId) },
                                onDeleteClick = { viewModel.deleteComment(comment.commentId) }
                            )
                        }
                        
                        // 더 보기
                        if (uiState.hasMore) {
                            item {
                                TextButton(
                                    onClick = { viewModel.loadMoreComments() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "댓글 더보기",
                                        fontFamily = PretendardFont,
                                        fontWeight = FontWeight(400)
                                    )
                                }
                            }
                        }
                    }
                    
                    // 댓글 입력창
                    CommentInputBar(
                        text = commentText,
                        onTextChange = { commentText = it },
                        onSendClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.createComment(commentText)
                                commentText = ""
                            }
                        }
                    )
                }
            }
        }
        
        // More 메뉴
        if (uiState.showMoreMenu) {
            MoreMenu(
                onEditClick = {
                    viewModel.hideMoreMenu()
                    // 수정 화면으로 이동 (postId, text, imageUrls 전달)
                    uiState.post?.let { post ->
                        val imageUrlsParam = post.mediaUrls.joinToString(",")
                        navController.navigate(
                            "free_writing?postId=${post.postId}&initialText=${post.text}&imageUrls=$imageUrlsParam"
                        )
                    }
                },
                onDeleteClick = {
                    viewModel.showDeleteDialog()
                },
                onDismiss = { viewModel.hideMoreMenu() }
            )
        }
        
        // 삭제 확인 다이얼로그
        if (uiState.showDeleteDialog) {
            DeleteConfirmDialog(
                isDeleting = uiState.isDeleting,
                onConfirm = {
                    viewModel.deletePost {
                        navController.popBackStack()
                    }
                },
                onDismiss = { viewModel.hideDeleteDialog() }
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PostDetailItem(
    post: Post,
    onLikeClick: () -> Unit,
    onMoreClick: () -> Unit,
    onAuthorClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // 작성자 정보
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 이미지 (클릭 시 사용자 게시물 페이지로 이동)
            AsyncImage(
                model = post.pet?.profileImageUrl ?: post.author.profilePictureUrl,
                contentDescription = "프로필",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAuthorClick),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 이름과 품종 (클릭 시 사용자 게시물 페이지로 이동)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onAuthorClick)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 반려견 이름 (#6B7684, 16px)
                    Text(
                        text = post.pet?.name ?: post.author.displayName,
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
                        fontSize = 16.sp,
                        color = Color(0xFF6B7684)
                    )
                    
                    // 타임스탬프 (오른쪽 하단, 이름과 같은 높이, #8B95A1, 14px)
                    Text(
                        text = TimeUtil.getRelativeTimeString(post.createdAt),
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
                        fontSize = 14.sp,
                        color = Color(0xFF8B95A1)
                    )
                }
                
                post.pet?.let {
                    Text(
                        text = "${it.breed} • ${it.age}살",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
                        fontSize = 14.sp,
                        color = Color(0xFF8B95A1)
                    )
                }
            }
        }
        
        // 게시글 텍스트 (사용자 정보 아래 20px, #333D4B, 17px)
        Text(
            text = post.text,
            fontFamily = PretendardFont,
            fontWeight = FontWeight(400),
            fontSize = 17.sp,
            color = Color(0xFF333D4B),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
        )
        
        // 이미지 (텍스트 아래 10px 간격, 여러 장일 경우 Pager)
        if (post.mediaUrls.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            
            val pagerState = rememberPagerState()
            
            Box {
                HorizontalPager(
                    count = post.mediaUrls.size,
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) { page ->
                    AsyncImage(
                        model = post.mediaUrls[page],
                        contentDescription = "게시글 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                if (post.mediaUrls.size > 1) {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        activeColor = Color.White,
                        inactiveColor = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        // 좋아요, 댓글, 더보기
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좋아요 아이콘 (24X24) - 0개일 때 like.png, 1개 이상일 때 favorite.png
            Image(
                painter = painterResource(
                    id = if (post.likesCount > 0) R.drawable.favorite else R.drawable.like
                ),
                contentDescription = "좋아요",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onLikeClick)
            )
            
            // 좋아요 수 (1개부터 표시, #EC4453, 15px)
            if (post.likesCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.likesCount}",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(400),
                    fontSize = 15.sp,
                    color = Color(0xFFEC4453)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 댓글 아이콘 (24X24)
            Image(
                painter = painterResource(id = R.drawable.comment),
                contentDescription = "댓글",
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // 댓글 수 (0개부터 표시, #B1B8C0, 15px)
            Text(
                text = "${post.commentsCount}",
                fontFamily = PretendardFont,
                fontWeight = FontWeight(400),
                fontSize = 15.sp,
                color = Color(0xFFB1B8C0)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 더보기 아이콘 (24X24) - like/comment와 같은 높이
            Image(
                painter = painterResource(id = R.drawable.more),
                contentDescription = "더보기",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onMoreClick)
            )
        }
        
        Divider(color = Color(0xFFE5E5EA), thickness = 8.dp)
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 프로필 이미지
        AsyncImage(
            model = comment.pet?.profileImageUrl ?: comment.author.profilePictureUrl,
            contentDescription = "프로필",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            // 작성자 정보
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.pet?.name ?: comment.author.displayName,
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(400),
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                comment.pet?.let {
                    Text(
                        text = "${it.breed} • ${it.age}살",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(400),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 댓글 텍스트
            Text(
                text = comment.text,
                fontFamily = PretendardFont,
                fontWeight = FontWeight(400),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 좋아요
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(
                        id = if (comment.isLiked) R.drawable.favorite else R.drawable.like
                    ),
                    contentDescription = "좋아요",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onLikeClick)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "${comment.likeCount}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        
        // 삭제 버튼 (작성자만)
        // TODO: 현재 사용자 ID와 비교
        Image(
            painter = painterResource(id = R.drawable.delete),
            contentDescription = "삭제",
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onDeleteClick)
        )
    }
}

@Composable
fun CommentInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    // 412X76 크기의 댓글 입력 바
    Row(
        modifier = Modifier
            .width(412.dp)
            .height(76.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 프로필 사진 (40X40 원)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            // TODO: 실제 사용자 프로필 사진으로 교체
            Text(
                text = "U",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 댓글 입력 박스 (326X42, 모서리 16, #F2F4F6)
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .width(326.dp)
                .height(42.dp)
                .background(Color(0xFFF2F4F6), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color(0xFF4E5968)
            ),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        text = "댓글로 의견을 남겨보세요.",
                        fontSize = 16.sp,
                        color = Color(0xFF4E5968).copy(alpha = 0.4f)
                    )
                }
                innerTextField()
            }
        )
        
        Spacer(modifier = Modifier.width(50.dp)) // 50픽셀 간격
        
        // 전송 버튼 (43X30, 모서리 12, #D1D6DB, send.png 20X20)
        Box(
            modifier = Modifier
                .size(43.dp, 30.dp)
                .background(Color(0xFFD1D6DB), RoundedCornerShape(12.dp))
                .clickable(enabled = text.isNotBlank(), onClick = onSendClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.send),
                contentDescription = "전송",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun MoreMenu(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            )
    ) {
        // 오른쪽 하단에 위치 (more.png 위로 8픽셀)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-8).dp)  // 오른쪽 하단에서 약간 떨어진 위치
                .width(197.dp)
                .height(84.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // 수정 컨테이너 (170×23, 모서리 8)
                Box(
                    modifier = Modifier
                        .width(170.dp)
                        .height(23.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable(onClick = onEditClick)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 수정 텍스트 (왼쪽)
                        Text(
                            text = "수정",
                            fontSize = 18.sp,
                            color = Color(0xFF4E5968)
                        )
                        
                        Spacer(modifier = Modifier.width(100.dp))
                        
                        // edit.png (오른쪽)
                        Image(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = "수정",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // 삭제 컨테이너 (170×23, 모서리 8)
                Box(
                    modifier = Modifier
                        .width(170.dp)
                        .height(23.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable(onClick = onDeleteClick)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 삭제 텍스트 (왼쪽)
                        Text(
                            text = "삭제",
                            fontSize = 18.sp,
                            color = Color(0xFF4E5968)
                        )
                        
                        Spacer(modifier = Modifier.width(100.dp))
                        
                        // delete.png (오른쪽)
                        Image(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "삭제",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(302.dp)
                .height(147.dp)
                .background(Color.White, RoundedCornerShape(26.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 메시지
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "게시물을 완전히 삭제할까요?",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333D4B)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "삭제한 글은 다시 되살릴 수 없어요.",
                    fontSize = 16.sp,
                    color = Color(0xFF6B7684)
                )
            }
            
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
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "닫기",
                        fontSize = 18.sp,
                        color = Color(0xFF4E5968),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // 삭제하기 버튼
                Box(
                    modifier = Modifier
                        .width(146.dp)
                        .height(58.dp)
                        .background(
                            color = Color(0xFFEC4453),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(enabled = !isDeleting, onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "삭제하기",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditCancelDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(302.dp)
                .height(147.dp)
                .background(Color.White, RoundedCornerShape(26.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 메시지
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "게시물 수정을 취소할까요?",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333D4B)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "지금까지 수정한 내용은 저장되지 않아요.",
                    fontSize = 16.sp,
                    color = Color(0xFF6B7684)
                )
            }
            
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
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "닫기",
                        fontSize = 18.sp,
                        color = Color(0xFF4E5968),
                        fontWeight = FontWeight.Medium
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
                        .clickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "나가기",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
