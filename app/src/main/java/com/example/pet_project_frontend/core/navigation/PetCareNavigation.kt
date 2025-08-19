package com.example.pet_project_frontend.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier // Modifier import 추가
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pet_project_frontend.presentation.map.MapScreen
import com.example.pet_project_frontend.presentation.mypage.main.MyPageScreen

@Composable
// 1. modifier 파라미터를 추가합니다.
fun PetCareNavHost(
	navController: NavHostController,
	modifier: Modifier = Modifier
) {
	// 2. 전달받은 modifier를 NavHost에 적용합니다.
	NavHost(
		navController = navController,
		startDestination = NavigationRoutes.MY_PAGE,
		modifier = modifier
	) {
		// 펫케어 화면 (아직 파일이 없으므로 임시로 마이페이지를 보여줍니다)
		composable(NavigationRoutes.PET_CARE) { MyPageScreen() }

		// 지도 화면
		composable(NavigationRoutes.MAP) { MapScreen() }

		// 커뮤니티 화면 (임시)
		composable(NavigationRoutes.COMMUNITY) { MyPageScreen() }

		// 번역기 화면 (임시)
		composable(NavigationRoutes.TRANSLATOR) { MyPageScreen() }

		// 마이페이지 화면
		composable(NavigationRoutes.MY_PAGE) { MyPageScreen() }
	}
}