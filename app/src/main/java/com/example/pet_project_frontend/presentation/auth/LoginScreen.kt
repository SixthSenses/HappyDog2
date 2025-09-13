// app/src/main/java/com/example/pet_project_frontend/presentation/auth/LoginScreen.kt

package com.example.pet_project_frontend.presentation.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginResult: (isNewUser: Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 로딩 상태 관리
    var isLoading by remember { mutableStateOf(false) }

    // 에러 메시지를 위한 SnackbarHost
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 앱 로고
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🐕",
                            fontSize = 60.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 앱 제목
                Text(
                    text = "HappyDog",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "반려동물과 함께하는\n행복한 시간",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Kakao 로그인 버튼
                Button(
                    onClick = {
                        Log.d("LoginScreen", "Starting Kakao Sign-In...")
                        isLoading = true
                        scope.launch {
                            try {
                                // TODO: 실제 Kakao 로그인 구현
                                kotlinx.coroutines.delay(2000) // 시뮬레이션

                                // 임시로 성공 처리 (실제로는 Kakao SDK 결과 처리)
                                onLoginResult(false) // isNewUser = false
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "로그인에 실패했습니다: ${e.message}",
                                    duration = SnackbarDuration.Short
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFE812) // Kakao Yellow
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "K",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = if (isLoading) {
                                "로그인 중..."
                            } else {
                                "카카오로 계속하기"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 게스트 로그인 버튼
                OutlinedButton(
                    onClick = {
                        Log.d("LoginScreen", "Guest login...")
                        scope.launch {
                            // 게스트 로그인은 바로 성공 처리
                            onLoginResult(true) // 게스트는 신규 사용자로 처리
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "게스트로 둘러보기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 로딩 인디케이터
                if (isLoading) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 약관 안내
                Text(
                    text = "로그인 시 서비스 이용약관 및\n개인정보 처리방침에 동의하게 됩니다.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}