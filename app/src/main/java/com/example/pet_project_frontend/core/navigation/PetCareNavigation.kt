package com.example.pet_project_frontend.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pet_project_frontend.presentation.auth.LoginScreen
import com.example.pet_project_frontend.presentation.map.MapScreen
import com.example.pet_project_frontend.presentation.mypage.main.MyPageScreen
import com.example.pet_project_frontend.presentation.petcare.PetCareMainScreen
import com.example.pet_project_frontend.presentation.petregistration.PetRegistrationScreen

@Composable
fun PetCareNavHost(
	navController: NavHostController,
	startDestination: String,
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

		// 지도 화면
		composable(Screen.Map.route) { MapScreen() }

		// 커뮤니티/번역기는 임시로 펫케어 메인으로 연결하거나 별도 화면 구성 필요 시 교체
		composable(Screen.Community.route) { PetCareMainScreen() }
		composable(Screen.Translator.route) { PetCareMainScreen() }

		// 마이페이지 화면
		composable(Screen.MyPage.route) { MyPageScreen() }
	}
}