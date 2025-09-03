package com.example.pet_project_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pet_project_frontend.core.navigation.BottomNavigation
import com.example.pet_project_frontend.core.navigation.PetCareNavHost
import com.example.pet_project_frontend.core.navigation.Screen
import com.example.pet_project_frontend.core.theme.PetCareTheme
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.*
import com.kakao.sdk.common.util.Utility
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLoading = mutableStateOf(true)

        installSplashScreen().setKeepOnScreenCondition {
            isLoading.value
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoggedIn.first()
                isLoading.value = false
            }
        }

        // Initialize Kakao Map SDK only when app key is available to avoid runtime crash
        val resolvedKey = when {
            !nativeAppKey.isNullOrBlank() -> nativeAppKey
            else -> {
                // Fallback: read from AndroidManifest meta-data if provided
                val appInfo = packageManager.getApplicationInfo(packageName, android.content.pm.PackageManager.GET_META_DATA)
                appInfo.metaData?.getString("com.kakao.vectormap.APP_KEY", "") ?: ""
            }
        }

        if (resolvedKey.isNullOrBlank()) {
            android.util.Log.e("MainActivity", "Kakao APP KEY is missing. Please set KAKAO_NATIVE_APP_KEY in local.properties or Gradle properties.")
        } else {
            KakaoMapSdk.init(this, resolvedKey)
        }

        setContent {
            PetCareTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

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
                        // startDestination은 로그인 여부에 따라 분기합니다.
                        // 로그인 상태라면 기본 홈은 PetCare로 둡니다(신규 유저 분기는 LoginScreen 내부에서 처리).
                        PetCareNavHost(
                            navController = navController,
                            startDestination = if (isLoggedIn) Screen.PetCare.route else Screen.Login.route,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}