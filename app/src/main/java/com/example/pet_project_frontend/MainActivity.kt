package com.example.pet_project_frontend

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pet_project_frontend.core.navigation.BottomNavigation
import com.example.pet_project_frontend.core.navigation.PetCareNavHost
import com.example.pet_project_frontend.core.theme.PetCareTheme
import com.example.pet_project_frontend.core.navigation.NavigationRoutes
import com.kakao.sdk.common.util.Utility
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    @Named("NATIVE_APP_KEY")
    lateinit var nativeAppKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KakaoMapSdk.init(this, nativeAppKey)

        setContent {
            PetCareTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        val showBottomBar = when (currentRoute) {
                            NavigationRoutes.PET_CARE,
                            NavigationRoutes.MAP,
                            NavigationRoutes.COMMUNITY,
                            NavigationRoutes.TRANSLATOR,
                            NavigationRoutes.MY_PAGE -> true
                            else -> false
                        }
                        if (showBottomBar) {
                            BottomNavigation(
                                currentRoute = currentRoute ?: "",
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    PetCareNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}