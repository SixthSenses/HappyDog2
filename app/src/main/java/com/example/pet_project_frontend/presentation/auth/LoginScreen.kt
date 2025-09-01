// app/src/main/java/com/example/pet_project_frontend/presentation/auth/LoginScreen.kt

package com.example.pet_project_frontend.presentation.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.BuildConfig
import com.example.pet_project_frontend.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    // 에러 메시지를 위한 SnackbarHost
    val snackbarHostState = remember { SnackbarHostState() }

    // Google Sign-In 설정 - ServerAuthCode를 요청하도록 수정
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(BuildConfig.GOOGLE_SERVER_CLIENT_ID) // 서버 클라이언트 ID 사용
            .requestEmail()
            .requestProfile()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // Google Sign-In 결과 처리
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)

            // serverAuthCode를 가져와서 백엔드로 전송
            account.serverAuthCode?.let { authCode ->
                Log.d("LoginScreen", "Auth Code received: ${authCode.take(10)}...")
                viewModel.socialLogin(authCode)
            } ?: run {
                Log.e("LoginScreen", "No auth code received")
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "인증 코드를 받지 못했습니다. 다시 시도해주세요.",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        } catch (e: ApiException) {
            Log.e("LoginScreen", "Google Sign-In failed", e)
            scope.launch {
                val errorMessage = when (e.statusCode) {
                    12501 -> "로그인이 취소되었습니다."
                    12500 -> "로그인에 실패했습니다. 다시 시도해주세요."
                    else -> "Google 로그인 실패: ${e.message}"
                }
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // 로그인 성공 시 처리
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Log.d("LoginScreen", "Login successful, navigating...")
                onLoginSuccess()
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (authState as AuthState.Error).message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    // 컴포넌트가 시작될 때 기존 로그인 정보 초기화
    DisposableEffect(Unit) {
        googleSignInClient.signOut()
        onDispose { }
    }

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
                // 앱 로고 (실제 로고 이미지가 있다면 사용)
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

                // Google 로그인 버튼
                Button(
                    onClick = {
                        Log.d("LoginScreen", "Starting Google Sign-In...")
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = authState !is AuthState.Loading,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4) // Google Blue
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google 로고 아이콘 (실제 아이콘이 있다면 사용)
                        Text(
                            text = "G",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = if (authState is AuthState.Loading) {
                                "로그인 중..."
                            } else {
                                "Google로 계속하기"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                // 로딩 인디케이터
                if (authState is AuthState.Loading) {
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