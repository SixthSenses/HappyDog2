package com.example.pet_project_frontend.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.pet_project_frontend.presentation.map.MapScreen
import com.example.pet_project_frontend.presentation.mypage.main.MyPageScreen
import com.example.pet_project_frontend.presentation.mypage.profile.gender.GenderSelectScreen
import com.example.pet_project_frontend.presentation.mypage.profile.name.NameEditRoute
//import com.example.pet_project_frontend.presentation.mypage.settings.notification.NotificationSettingsScreen

import android.net.Uri

@Composable
fun PetCareNavHost(
	navController: NavHostController,
	modifier: Modifier = Modifier
) {
	NavHost(
		navController = navController,
		startDestination = NavigationRoutes.MY_PAGE,
		modifier = modifier
	) {
		// 펫케어 (임시)
		composable(NavigationRoutes.PET_CARE) { MyPageScreen() }

		// 지도
		composable(NavigationRoutes.MAP) { MapScreen() }

		// 커뮤니티 (임시)
		composable(NavigationRoutes.COMMUNITY) { MyPageScreen() }

		// 번역기 (임시)
		composable(NavigationRoutes.TRANSLATOR) { MyPageScreen() }

		// 마이페이지
		composable(NavigationRoutes.GENDER_SELECT) {
			GenderSelectScreen(
				onBack = { navController.popBackStack() },
				onSaved = { navController.popBackStack() }  // 저장 후 이전 화면
			)
		}

		// 알림
//		composable(NavigationRoutes.NOTIFICATION) {
//			NotificationSettingsScreen(
//				onBack = { navController.popBackStack() }
//			)
//		}


		composable(
			route = "${NavigationRoutes.BIRTH_EDIT}?initialBirth={initialBirth}",
			arguments = listOf(
				navArgument("initialBirth") { type = NavType.StringType; defaultValue = "" }
			)
		) {
			com.example.pet_project_frontend.presentation.mypage.profile.birth.BirthEditRoute(
				navController = navController
			)
		}

		composable(NavigationRoutes.MY_PAGE) {
			MyPageScreen(
			onNameClick = { name ->
				navController.navigate(NavigationRoutes.nameEdit(name))
			},
			onBirthdateClick = { birth ->
				val clean = if (birth == "연/월/일") "" else birth   // ← 플레이스홀더 문자열 방지
				val encoded = Uri.encode(clean)                      // 슬래시 포함 값 안전 전송
				navController.navigate("${NavigationRoutes.BIRTH_EDIT}?initialBirth=$encoded")
			},
			onGenderClick = {
			 navController.navigate(NavigationRoutes.genderSelect())
			},
			onNotificationClick = {
				navController.navigate(NavigationRoutes.NOTIFICATION)
			}
		) }

		// 이름 수정
		composable(NavigationRoutes.NAME_EDIT) {
			// 필요한 경우 아래처럼 콜백을 넘겨도 됩니다.
			// NameEditScreen(
			//     onBack = { navController.popBackStack() },
			//     onFinished = { navController.popBackStack() }
			// )
			NameEditRoute(navController)
		}
	}
}
