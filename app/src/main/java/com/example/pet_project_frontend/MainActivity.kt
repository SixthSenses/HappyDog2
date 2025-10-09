package com.example.pet_project_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pet_project_frontend.core.designsystem.AppTheme
import com.example.pet_project_frontend.core.navigation.BottomNavigation
import com.example.pet_project_frontend.core.navigation.PetCareNavHost
import com.example.pet_project_frontend.core.navigation.Screen
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.kakao.sdk.common.util.Utility
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    @Named("NATIVE_APP_KEY")
    lateinit var nativeAppKey: String

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLoading = mutableStateOf(true)

        // Splash 유지 조건
        installSplashScreen().setKeepOnScreenCondition { isLoading.value }

        // 로그인 여부 초기 체크
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoggedIn.first()
                isLoading.value = false
            }
        }

        // Kakao Map SDK 초기화 (키 확인 및 keyHash 로깅)
        val resolvedKey = if (nativeAppKey.isNotBlank()) {
            nativeAppKey
        } else {
            val appInfo = packageManager.getApplicationInfo(
                packageName,
                android.content.pm.PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.kakao.vectormap.APP_KEY", "") ?: ""
        }
        android.util.Log.i("KakaoKey", "resolvedKey length=${resolvedKey.length}")
        runCatching { Utility.getKeyHash(this) }
            .onSuccess { keyHash -> android.util.Log.i("KakaoKey", "keyHash=$keyHash") }
            .onFailure { e -> android.util.Log.w("KakaoKey", "Failed to get keyHash: ${e.message}") }

        if (resolvedKey.isBlank()) {
            android.util.Log.e(
                "MainActivity",
                "Kakao APP KEY is missing. Please set KAKAO_NATIVE_APP_KEY in local.properties or Gradle properties."
            )
        } else {
            KakaoMapSdk.init(this, resolvedKey)
        }

        setContent {
            AppTheme {
                // 원격(develop)과 동일한 분기: isLoggedIn + hasPet만 사용 (selectedPetId 제거)
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                val hasPet by viewModel.hasPet.collectAsStateWithLifecycle()

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 바텀바 라우트
                val bottomBarRoutes = remember {
                    listOf(
                        Screen.PetCare.route,
                        Screen.Map.route,
                        Screen.Community.route,
                        Screen.Translator.route,
                        Screen.MyPage.route
                    )
                }
                val showBottomBar = currentRoute in bottomBarRoutes

                // 공지/안내 시트 상태 (원격 코드 스타일)
                var selectedNotice by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
                val openNotice: (@Composable (closeNotice: () -> Unit) -> Unit) -> Unit = { composable ->
                    selectedNotice = {
                        composable { selectedNotice = null }
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigation(
                                currentRoute = currentRoute ?: "",
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    if (isLoading.value) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // 시작 목적지 분기: 로그인 + 펫 존재 여부만 사용
                        val effectiveStart = when {
                            !isLoggedIn -> Screen.Login.route
                            !hasPet -> Screen.PetRegistration.route
                            else -> Screen.PetCare.route
                        }

                        // 런타임에도 펫이 없으면 등록 화면으로 유도
                        LaunchedEffect(isLoggedIn, hasPet, currentRoute) {
                            if (isLoggedIn &&
                                !hasPet &&
                                currentRoute != Screen.Login.route &&
                                currentRoute != Screen.PetRegistration.route
                            ) {
                                navController.navigate(Screen.PetRegistration.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = false }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                        }

                        PetCareNavHost(
                            navController = navController,
                            startDestination = effectiveStart,
                            openNotice = openNotice,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }

                // 배경 페이드
                AnimatedVisibility(
                    visible = selectedNotice != null,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.32f))
                    )
                }

                // 공지 슬라이드 인/아웃
                AnimatedVisibility(
                    visible = selectedNotice != null,
                    enter = slideInVertically(initialOffsetY = { full -> full }),
                    exit = slideOutVertically(targetOffsetY = { full -> full })
                ) {
                    selectedNotice?.invoke()
                }
            }
        }
    }
}
