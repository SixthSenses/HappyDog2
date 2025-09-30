package com.example.pet_project_frontend.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.navDeepLink
import com.example.pet_project_frontend.core.navigation.DeepLinks
import com.example.pet_project_frontend.core.navigation.Routes
import com.example.pet_project_frontend.presentation.auth.LoginScreen
import com.example.pet_project_frontend.presentation.map.MapScreen
import com.example.pet_project_frontend.presentation.mypage.main.MyPageScreen
import com.example.pet_project_frontend.presentation.petcare.PetCareMainScreen
import com.example.pet_project_frontend.presentation.petregistration.PetRegistrationScreen
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pet_project_frontend.presentation.mungstar.MungStarFeed
import com.example.pet_project_frontend.presentation.mungstar.FreeWriting
import com.example.pet_project_frontend.presentation.mungstar.CartoonMaking
import com.example.pet_project_frontend.presentation.translator.TranslatorScreen

@Composable
fun PetCareNavHost(
	navController: NavHostController,
	startDestination: String,
	openNotice: (@Composable (closeNotice: () -> Unit) -> Unit) -> Unit,
	modifier: Modifier = Modifier
) {
	NavHost(
		navController = navController,
		startDestination = startDestination,
		modifier = modifier
	) {

		// 로그인 화면
		composable(Screen.Login.route) {
			LoginScreen(
				onLoginResult = { isNewUser ->
					val target = if ( isNewUser ) Screen.PetRegistration.route else Screen.PetCare.route
					navController.navigate(target) {
						popUpTo(Screen.Login.route) { inclusive = true }
					}
				}
			)
		}

		// 펫 등록 화면
		composable(Screen.PetRegistration.route) {
			PetRegistrationScreen(navController = navController)
		}

		// 펫케어 화면
		composable(Screen.PetCare.route) {
			PetCareMainScreen()
		}

		// 펫케어 대시보드(딥링크 진입 지원): app://pet-care/dashboard?petId=...&date=...&tab=...
		composable(
			route = Routes.PetCare.Dashboard,
			arguments = listOf(
				navArgument("petId") { type = NavType.StringType; nullable = true; defaultValue = null },
				navArgument("date") { type = NavType.StringType; nullable = true; defaultValue = null },
				navArgument("tab") { type = NavType.StringType; nullable = true; defaultValue = null }
			),
			deepLinks = listOf(
				navDeepLink { uriPattern = DeepLinks.PET_CARE_DASHBOARD + "?petId={petId}&date={date}&tab={tab}" },
				navDeepLink { uriPattern = DeepLinks.PET_CARE_DASHBOARD }
			)
		) { backStackEntry ->
			// TODO: 실제 대시보드 구현 시 인자 사용
			PetCareMainScreen()
		}

		// 지도 화면
		composable(Screen.Map.route) { MapScreen() }

		// 멍스타그램 메인 화면 (NavController 전달)
		composable(Screen.Community.route) {
			MungStarFeed(navController = navController)
		}

		// 자유글 작성 화면
		composable("free_writing") { backStackEntry ->
			FreeWriting(navController = navController)
		}

		// 만화 제작 화면
		composable("cartoon_making") { backStackEntry ->
			CartoonMaking(navController = navController)
		}

		// 번역기 화면
		composable(Screen.Translator.route) { PetCareMainScreen() }

		// 커뮤니티/번역기는 임시로 펫케어 메인으로 연결하거나 별도 화면 구성 필요 시 교체
		composable(Screen.Community.route) { PetCareMainScreen() }
		composable(Screen.Translator.route) { TranslatorScreen(openNotice = openNotice) }

		// 마이페이지 화면
		composable(Screen.MyPage.route) { MyPageScreen() }
	}
}