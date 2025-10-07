package com.example.pet_project_frontend.presentation.mungstar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.pet_project_frontend.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MungStarFeed(navController: NavController) {
    var showBottomSheet by remember { mutableStateOf(false) }

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

            // 메인 컨텐츠 영역 - 준비중 메시지
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