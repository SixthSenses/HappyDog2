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

        KakaoMapSdk.init(this, nativeAppKey)

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
                        // [수정됨] startDestination 파라미터를 여기서 전달합니다!
                        PetCareNavHost(
                            navController = navController,
                            startDestination = if (isLoggedIn) Screen.MyPage.route else Screen.Login.route,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}