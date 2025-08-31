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
			// [수정됨] LoginScreen 호출 방식을 변경합니다.
			LoginScreen(
				onLoginSuccess = {
					// 로그인 성공 시 마이페이지로 이동하고,
					// 뒤로가기 버튼을 눌러도 로그인 화면으로 돌아오지 않도록 스택을 정리합니다.
					navController.navigate(Screen.MyPage.route) {
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

		// 커뮤니티 화면 (임시)
		composable(Screen.Community.route) { MyPageScreen() }

		// 번역기 화면 (임시)
		composable(Screen.Translator.route) { MyPageScreen() }

		// 마이페이지 화면
		composable(Screen.MyPage.route) { MyPageScreen() }
	}
}