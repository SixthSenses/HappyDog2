package com.example.pet_project_frontend.presentation.mungstar

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.PretendardFont
import com.example.pet_project_frontend.util.TimeUtil
import com.example.pet_project_frontend.domain.model.Comment
import com.example.pet_project_frontend.domain.model.Post
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
    
    // 댓글 등록 토스트 표시
    LaunchedEffect(uiState.showCommentToast) {
        if (uiState.showCommentToast) {
            delay(2000)
            viewModel.hideCommentToast()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // 키보드와 함께 올라가도록
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 헤더 바 (디바이스 너비 × 64픽셀)
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
                
                // "게시물" 텍스트 (가운데, FontWeight 500)
                Text(
                    text = "게시물",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(500),
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
                        
                        // 댓글이 없을 때 no_post.png와 메시지
                        if (uiState.comments.isEmpty() && !uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.no_post),
                                            contentDescription = "댓글 없음",
                                            modifier = Modifier.size(64.dp)
                                        )
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
                    // 수정 화면으로 이동 (postId만 전달, Free_Writing에서 자동으로 불러옴)
                    uiState.post?.let { post ->
                        navController.navigate("free_writing?postId=${post.postId}")
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
                        // 삭제 완료 후 토스트 표시를 위한 플래그 설정
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("post_deleted", true)
                        navController.popBackStack()
                    }
                },
                onDismiss = { viewModel.hideDeleteDialog() }
            )
        }
        
        // 댓글 등록 토스트
        if (uiState.showCommentToast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .offset(y = 78.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp, 50.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(26.dp),
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
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // 체크 원
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
                                contentDescription = "체크",
                                modifier = Modifier.size(9.dp, 7.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        // 텍스트
                        Text(
                            text = "댓글이 등록되었어요.",
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
}

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
            .padding(26.dp)
    ) {
        // 작성자 정보
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 반려견 이름 (닉네임 - weight 500, #6B7684, 16px)
                        Text(
                            text = post.pet?.name ?: post.author.displayName,
                            fontFamily = PretendardFont,
                            fontWeight = FontWeight(500),
                            fontSize = 16.sp,
                            color = Color(0xFF6B7684)
                        )
                        
                        if (post.pet?.isVerified == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Image(
                                painter = painterResource(id = R.drawable.badge),
                                contentDescription = "Verification badge",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
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
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 게시글 텍스트 (#333D4B, 17px)
        Text(
            text = post.text,
            fontFamily = PretendardFont,
            fontWeight = FontWeight(400),
            fontSize = 17.sp,
            color = Color(0xFF333D4B)
        )
        
        // 이미지 (텍스트 아래 10px 간격, 그리드 형태로 표시)
        if (post.mediaUrls.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            
            // 이미지를 2개씩 행으로 나눔
            val rows = post.mediaUrls.chunked(2)
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEach { rowImages ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowImages.forEach { imageUrl ->
                            AsyncImage(
                                model = imageUrl, // FirebaseStorageInterceptor가 자동으로 URL 갱신
                                contentDescription = "게시글 이미지",
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF5F5F5)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // 홀수 개일 경우 빈 공간 채우기
                        if (rowImages.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 좋아요, 댓글, 더보기
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좋아요 아이콘 - 0개일 때 like.png (22dp), 1개 이상일 때 favorite.png (24dp)
            Image(
                painter = painterResource(
                    id = if (post.likesCount > 0) R.drawable.favorite else R.drawable.like
                ),
                contentDescription = "좋아요",
                modifier = Modifier
                    .size(if (post.likesCount > 0) 24.dp else 22.dp)
                    .clickable(onClick = onLikeClick)
            )
            
            // 좋아요 수 (1개부터 표시, #EC4453, 15px)
            if (post.likesCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.likesCount}",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(500),
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
                fontWeight = FontWeight(500),
                fontSize = 15.sp,
                color = Color(0xFFB1B8C0)
            )
            
            Spacer(modifier = Modifier.width(200.dp))
            
            // 더보기 아이콘 (24X24) - like/comment와 200px 떨어진 위치
            Image(
                painter = painterResource(id = R.drawable.more),
                contentDescription = "더보기",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onMoreClick)
            )
        }
    }
    
    // 게시물과 댓글 영역 구분선
    HorizontalDivider(
        color = Color(0xFFE4E8EB),
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CommentItem(
    comment: Comment,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReplyClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 16.dp)
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
            // 사용자 이름 + 타임스탬프
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 사용자 이름
                Text(
                    text = comment.pet?.name ?: comment.author.displayName,
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(600),
                    fontSize = 14.sp,
                    color = Color(0xFF6B7684)
                )
                
                if (comment.pet?.isVerified == true) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.badge),
                        contentDescription = "Verification badge",
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // 점
                Text(
                    text = "·",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(400),
                    fontSize = 14.sp,
                    color = Color(0xFF8B95A1)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // 타임스탬프
                Text(
                    text = TimeUtil.getRelativeTimeString(comment.createdAt),
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(400),
                    fontSize = 14.sp,
                    color = Color(0xFF8B95A1)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 댓글 텍스트
            Text(
                text = comment.text,
                fontFamily = PretendardFont,
                fontWeight = FontWeight(400),
                fontSize = 14.sp,
                color = Color(0xFF333D4B)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 좋아요 & 답글달기
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 좋아요 아이콘
                Image(
                    painter = painterResource(
                        id = if (comment.isLiked) R.drawable.favorite else R.drawable.like
                    ),
                    contentDescription = "좋아요",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onLikeClick)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // 좋아요 개수
                Text(
                    text = "${comment.likeCount}",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(500),
                    fontSize = 15.sp,
                    color = Color(0xFFB1B8C0)
                )
                
                Spacer(modifier = Modifier.width(20.dp))
                
                // 답글달기 아이콘
                Image(
                    painter = painterResource(id = R.drawable.comment2),
                    contentDescription = "답글달기",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onReplyClick)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // 답글달기 텍스트
                Text(
                    text = "답글 달기",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight(500),
                    fontSize = 15.sp,
                    color = Color(0xFF8B95A1),
                    modifier = Modifier.clickable(onClick = onReplyClick)
                )
            }
        }
        
        // More 버튼 (오른쪽 끝, 사용자 이름과 같은 높이)
        Image(
            painter = painterResource(id = R.drawable.more2),
            contentDescription = "더보기",
            modifier = Modifier
                .size(24.dp)
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
            .fillMaxWidth()
            .height(76.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
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
        
        // 댓글 입력 박스 (유연한 너비, 42 높이, 모서리 16, #F2F4F6)
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
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
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 전송 버튼 (43X30, 모서리 12, 텍스트 있으면 #3182F6 없으면 #D1D6DB, send.png 20X20)
        Box(
            modifier = Modifier
                .size(43.dp, 30.dp)
                .background(
                    if (text.isNotBlank()) Color(0xFF3182F6) else Color(0xFFD1D6DB),
                    RoundedCornerShape(12.dp)
                )
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
    var selectedItem by remember { mutableStateOf<Int?>(null) }
    
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 수정 컨테이너 (197×41, 모서리 8, 클릭 시 #F3F4F6)
                Box(
                    modifier = Modifier
                        .width(197.dp)
                        .height(41.dp)
                        .background(
                            color = if (selectedItem == 1) Color(0xFFF3F4F6) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            selectedItem = 1
                            onEditClick()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 수정 텍스트 (왼쪽)
                        Text(
                            text = "수정",
                            fontSize = 18.sp,
                            color = Color(0xFF4E5968)
                        )
                        
                        // edit.png (오른쪽)
                        Image(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = "수정",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // 삭제 컨테이너 (197×41, 모서리 8, 클릭 시 #F3F4F6)
                Box(
                    modifier = Modifier
                        .width(197.dp)
                        .height(41.dp)
                        .background(
                            color = if (selectedItem == 2) Color(0xFFF3F4F6) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            selectedItem = 2
                            onDeleteClick()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 삭제 텍스트 (왼쪽)
                        Text(
                            text = "삭제",
                            fontSize = 18.sp,
                            color = Color(0xFF4E5968)
                        )
                        
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)  // 전체 화면 사용
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 검은색 배경 (32% 불투명도)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(onClick = onDismiss)
            )
            
            Column(
                modifier = Modifier
                    .width(336.dp)
                    .height(181.dp)
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
}

@Composable
fun EditCancelDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)  // 전체 화면 사용
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 검은색 배경 (32% 불투명도)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(onClick = onDismiss)
            )
            
            Column(
                modifier = Modifier
                    .width(336.dp)
                    .height(181.dp)
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
}
