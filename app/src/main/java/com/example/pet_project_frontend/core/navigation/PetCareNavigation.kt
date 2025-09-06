package com.example.pet_project_frontend.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.LaunchedEffect
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
import com.example.pet_project_frontend.presentation.petcare.PetCareDashboardScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.CircularProgressIndicator
import com.example.pet_project_frontend.presentation.petcare.bcs.BcsDetailsScreen
import com.example.pet_project_frontend.presentation.petcare.bcs.rememberBcsPaintersFromAssets
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.pet_project_frontend.core.utils.DateFormatter
import com.example.pet_project_frontend.presentation.petcare.history.HistoryScreen
import com.example.pet_project_frontend.presentation.petcare.insights.InsightsScreen
import com.example.pet_project_frontend.presentation.petcare.PetCareViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.presentation.petcare.PetCareDashboardScreen

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
			// 기존 메인(플레이스홀더) 대신 대시보드로 라우팅
			LaunchedEffect(Unit) {
				navController.navigate(Screen.PetCareDashboard.createRoute()) {
					launchSingleTop = true
				}
			}
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
			// petId는 화면에서 DataStore 보완 처리하므로 기본값은 빈 문자열로 둔다
			val petId = backStackEntry.arguments?.getString("petId") ?: ""
			val date = backStackEntry.arguments?.getString("date")
			val tab = backStackEntry.arguments?.getString("tab")
			PetCareDashboardScreen(
				petId = petId,
				onSelectTopTab = { /* 상단 탭 전환 시 추가 화면으로 내비게이션 */ },
				onOpenDetail = { recordType ->
					if (recordType == "bcs") {
						navController.navigate("pet_care/bcs-details?petId=${petId}&selected=3")
					}
				},
				onOpenChart = { type, start, end ->
					navController.navigate("pet_care/charts?type=$type&start=$start&end=$end")
				},
				onOpenSettings = { _ ->
					navController.navigate(Routes.PetCare.Settings)
				}
			)
		}

		// BCS 상세 화면: app://pet-care/bcs-details?petId=...&selected=...
		composable(
			route = Routes.PetCare.BcsDetails,
			arguments = listOf(
				navArgument("petId") { type = NavType.StringType; nullable = true; defaultValue = null },
				navArgument("selected") { type = NavType.IntType; defaultValue = 3 }
			),
			deepLinks = listOf(
				navDeepLink { uriPattern = DeepLinks.PET_CARE_BCS_DETAILS + "?petId={petId}&selected={selected}" },
				navDeepLink { uriPattern = DeepLinks.PET_CARE_BCS_DETAILS }
			)
		) { backStackEntry ->
			val selected = backStackEntry.arguments?.getInt("selected") ?: 3
			val painters = rememberBcsPaintersFromAssets()
			BcsDetailsScreen(selected = selected, images = painters)
		}

		// 지도 화면
		composable(Screen.Map.route) { MapScreen() }

		// 커뮤니티/번역기는 임시로 펫케어 메인으로 연결하거나 별도 화면 구성 필요 시 교체
		composable(Screen.Community.route) { PetCareMainScreen() }
		composable(Screen.Translator.route) { PetCareMainScreen() }

		// 마이페이지 화면
		composable(Screen.MyPage.route) {
			MyPageScreen(
				onRegisterPetClick = {
					navController.navigate(Screen.PetRegistration.route)
				}
			)
		}

		// 차트
		composable(
			route = Routes.PetCare.Charts,
			arguments = listOf(
				navArgument("type") { type = NavType.StringType },
				navArgument("start") { type = NavType.StringType },
				navArgument("end") { type = NavType.StringType }
			),
			deepLinks = listOf(
				navDeepLink { uriPattern = DeepLinks.PET_CARE_CHARTS + "?type={type}&start={start}&end={end}" }
			)
		) { backStackEntry ->
			val type = backStackEntry.arguments?.getString("type") ?: ""
			val start = backStackEntry.arguments?.getString("start") ?: ""
			val end = backStackEntry.arguments?.getString("end") ?: ""
			com.example.pet_project_frontend.presentation.petcare.charts.ChartsScreen(
				type = type,
				start = start,
				end = end,
				onBack = { navController.popBackStack() }
			)
		}

		composable(
			route = Routes.PetCare.Settings,
			deepLinks = listOf(navDeepLink { uriPattern = DeepLinks.PET_CARE_SETTINGS })
		) {
			com.example.pet_project_frontend.presentation.petcare.settings.SettingsScreen(
				onBack = { navController.popBackStack() }
			)
		}

		// 히스토리
		composable("pet_care/history?petId={petId}&type={type}",
			arguments = listOf(
				navArgument("petId") { type = NavType.StringType; nullable = true; defaultValue = null },
				navArgument("type") { type = NavType.StringType; nullable = true; defaultValue = null }
			)
		) { backStackEntry ->
			val petIdArg = backStackEntry.arguments?.getString("petId")
			HistoryScreen(petId = petIdArg ?: "")
		}

		// 인사이트
		composable("pet_care/insights?petId={petId}",
			arguments = listOf(navArgument("petId") { type = NavType.StringType; nullable = true; defaultValue = null })
		) { backStackEntry ->
			val petIdArg = backStackEntry.arguments?.getString("petId")
			InsightsScreen(petId = petIdArg ?: "")
		}
	}
}