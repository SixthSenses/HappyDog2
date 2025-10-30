package com.example.pet_project_frontend.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.PretendardFont
import com.example.pet_project_frontend.domain.model.Notification
import com.example.pet_project_frontend.util.TimeUtil

@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    
    // 스크롤 끝 감지하여 더 불러오기
    LaunchedEffect(listState.canScrollForward) {
        if (!listState.canScrollForward && uiState.hasMore) {
            viewModel.loadMoreNotifications()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 앱 바 (게시물 작성/만화 생성과 동일한 스타일)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White)
        ) {
            // 뒤로가기 버튼
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF1D1B20),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // 중앙 제목
            Text(
                text = "알림",
                fontFamily = PretendardFont,
                fontSize = 18.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF1D1B20),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 구분선
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFFE4E8EB)
        )
        
        // 알림 목록
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.notifications.isEmpty()) {
                // 초기 로딩
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF3182F6)
                )
            } else if (uiState.notifications.isEmpty()) {
                // 알림 없음
                EmptyNotificationView()
            } else {
                // 알림 목록
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.notifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                // 알림 읽음 처리
                                if (!notification.read) {
                                    viewModel.markAsRead(notification.id)
                                }
                                
                                // 딥링크 처리
                                handleDeepLink(navController, notification.deeplink)
                            }
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = Color(0xFFE4E8EB)
                        )
                    }
                    
                    // 더 불러오는 중 표시
                    if (uiState.isLoading && uiState.notifications.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF3182F6)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    // 알림 컨테이너 박스: 가로 화면 너비, 세로 89dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(89.dp)
            .clickable(onClick = onClick)
            .background(Color.White)
    ) {
        // "멍스타그램" 텍스트 - 왼쪽 25dp, 위 20dp
        Text(
            text = "멍스타그램",
            fontFamily = PretendardFont,
            fontSize = 14.sp,
            fontWeight = FontWeight(400),
            color = Color(0xFF8B95A1),
            modifier = Modifier
                .offset(x = 25.dp, y = 20.dp)
        )
        
        // 타임스탬프 - "멍스타그램"과 같은 높이, 오른쪽 25dp
        Text(
            text = TimeUtil.getRelativeTimeString(notification.createdAt),
            fontFamily = PretendardFont,
            fontSize = 14.sp,
            fontWeight = FontWeight(400),
            color = Color(0xFF8B95A1),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-25).dp, y = 20.dp)
        )
        
        // 알림 메시지 - "멍스타그램" 아래 10dp
        // 예: "보리님이 회원님의 게시물을 좋아합니다."
        Text(
            text = notification.message,
            fontFamily = PretendardFont,
            fontSize = 17.sp,
            fontWeight = FontWeight(600),
            color = Color(0xFF4E5968),
            modifier = Modifier
                .offset(x = 25.dp, y = 44.dp) // 20 + 14(텍스트 높이 추정) + 10
        )
    }
}

@Composable
fun EmptyNotificationView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "알림이 없습니다",
                fontFamily = PretendardFont,
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFF9AA0A6)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "새로운 알림이 오면 여기에 표시됩니다",
                fontFamily = PretendardFont,
                fontSize = 13.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFFBDC1C6)
            )
        }
    }
}

/**
 * 딥링크 처리
 * 
 * 예시:
 * - app://posts/{post_id} -> PostDetailScreen
 * - app://comments/{comment_id} -> PostDetailScreen (해당 댓글로 스크롤)
 * - app://cartoon-jobs/{job_id} -> CartoonLoadingScreen
 */
private fun handleDeepLink(navController: NavController, deeplink: String) {
    when {
        deeplink.startsWith("app://posts/") -> {
            val postId = deeplink.substringAfter("app://posts/")
            navController.navigate("post_detail/$postId")
        }
        deeplink.startsWith("app://comments/") -> {
            // 댓글은 게시물 상세로 이동 (추후 댓글로 스크롤 기능 추가 가능)
            val commentId = deeplink.substringAfter("app://comments/")
            // commentId로 해당 게시물 찾는 로직 필요 (현재는 생략)
        }
        deeplink.startsWith("app://cartoon-jobs/") -> {
            val jobId = deeplink.substringAfter("app://cartoon-jobs/")
            navController.navigate("cartoon_loading/$jobId")
        }
        deeplink.startsWith("app://pet-care/") -> {
            // 펫케어 대시보드로 이동
            navController.navigate("pet_care")
        }
        else -> {
            // 알 수 없는 딥링크는 무시
        }
    }
}
