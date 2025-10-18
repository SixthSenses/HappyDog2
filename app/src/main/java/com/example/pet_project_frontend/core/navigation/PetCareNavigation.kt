package com.example.pet_project_frontend.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.pet_project_frontend.presentation.petcare.home.PetCareHomeScreen
import com.example.pet_project_frontend.presentation.petregistration.PetRegistrationScreen
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pet_project_frontend.presentation.translator.TranslatorScreen
import com.example.pet_project_frontend.presentation.eye_health.EyeHealthScreen
import com.example.pet_project_frontend.presentation.eye_health.EyeHealthHistoryScreen
import com.example.pet_project_frontend.presentation.eye_health.ImageViewerScreen
import com.example.pet_project_frontend.presentation.breed_guide.BreedGuideListScreen
import com.example.pet_project_frontend.presentation.breed_guide.BreedGuidebookScreen
import com.example.pet_project_frontend.presentation.care_management.FeedManagementScreen
import com.example.pet_project_frontend.presentation.care_management.ActivityManagementScreen
import com.example.pet_project_frontend.presentation.care_management.WeightManagementScreen
import com.example.pet_project_frontend.presentation.care_management.PoopManagementScreen
import com.example.pet_project_frontend.presentation.care_management.VomitManagementScreen
import com.example.pet_project_frontend.presentation.care_record.FeedRecordScreen
import com.example.pet_project_frontend.presentation.care_record.ActivityRecordScreen
import com.example.pet_project_frontend.presentation.care_record.WeightRecordScreen
import com.example.pet_project_frontend.presentation.care_record.WeightLogScreen
import com.example.pet_project_frontend.presentation.care_record.PoopRecordScreen
import com.example.pet_project_frontend.presentation.care_record.VomitRecordScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.presentation.petcare.home.PetCareHomeViewModel
import java.time.format.DateTimeFormatter

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

		// 펫케어 메인 화면 (새로운 홈화면)
		composable(Screen.PetCare.route) {
			// ViewModel을 여기서 가져와서 선택된 날짜 사용
			val viewModel: PetCareHomeViewModel = hiltViewModel()
			val uiState by viewModel.uiState.collectAsState()
			
			PetCareHomeScreen(
				viewModel = viewModel,
				onNotificationClick = { /* TODO: 알림 화면으로 네비게이션 */ },
				onHealthSurveyClick = { 
					navController.navigate(Screen.HealthSurvey.route)
				},
				onBreedGuideClick = { 
					navController.navigate(Screen.BreedGuide.route)
				},
				onEyeCheckClick = { 
					navController.navigate(Screen.EyeHealth.route)
				},
				onFeedClick = {
					val dateString = uiState.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
					navController.navigate(Screen.FeedManagement.createRoute(dateString))
				},
				onActivityClick = {
					val dateString = uiState.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
					navController.navigate(Screen.ActivityManagement.createRoute(dateString))
				},
				onWeightClick = {
					val dateString = uiState.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
					navController.navigate(Screen.WeightManagement.createRoute(dateString))
				},
				onPoopClick = { date ->
					navController.navigate(Screen.PoopManagement.createRoute(date))
				},
				onVomitClick = { date ->
					navController.navigate(Screen.VomitManagement.createRoute(date))
				}
			)
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

		// 커뮤니티/번역기는 임시로 펫케어 메인으로 연결하거나 별도 화면 구성 필요 시 교체
		composable(Screen.Community.route) { PetCareMainScreen() }
		composable(Screen.Translator.route) { TranslatorScreen(openNotice = openNotice) }

		// 마이페이지 화면
		composable(Screen.MyPage.route) { MyPageScreen() }

		// AI 안구 검사 화면
		composable(Screen.EyeHealth.route) {
			EyeHealthScreen(
				onBackClick = { navController.popBackStack() },
				onNavigateToHistory = { navController.navigate(Screen.EyeHealthHistory.route) }
			)
		}

		// 안구 검사 기록 화면
		composable(Screen.EyeHealthHistory.route) {
			EyeHealthHistoryScreen(
				onBackClick = { navController.popBackStack() },
				onImageClick = { imageUrl ->
					// URL 인코딩해서 이미지 뷰어로 이동
					val encodedUrl = java.net.URLEncoder.encode(imageUrl, "UTF-8")
					navController.navigate(Screen.ImageViewer.createRoute(encodedUrl))
				}
			)
		}

		// 이미지 뷰어 화면
		composable(
			route = Screen.ImageViewer.route,
			arguments = listOf(
				navArgument("imageUrl") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val encodedImageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
			val imageUrl = java.net.URLDecoder.decode(encodedImageUrl, "UTF-8")
			
			ImageViewerScreen(
				imageUrl = imageUrl,
				onCloseClick = { navController.popBackStack() }
			)
		}

		// 건강 설문지 화면
		composable(Screen.HealthSurvey.route) {
			com.example.pet_project_frontend.presentation.health_survey.HealthSurveyScreen(
				onBackClick = { navController.popBackStack() },
				onFinish = { navController.popBackStack() }
			)
		}

		// 견종 가이드북 리스트 화면
		composable(Screen.BreedGuide.route) {
			BreedGuideListScreen(
				onBackClick = { navController.popBackStack() },
				onBreedClick = { breedName ->
					navController.navigate(Screen.BreedGuidebook.createRoute(breedName))
				}
			)
		}

		// 케어 관리 화면들 (중간 단계)
		composable(
			route = Screen.FeedManagement.route,
			arguments = listOf(
				navArgument("date") {
					type = NavType.StringType
					nullable = true
					defaultValue = null
				}
			)
		) { backStackEntry ->
			// 1. ViewModel 인스턴스를 가져옵니다.
			val viewModel: PetCareHomeViewModel = hiltViewModel()
			// 2. ViewModel의 상태에서 현재 선택된 petId를 가져옵니다.
			// 'by'를 사용하므로, .value를 제거합니다.
			val uiState by viewModel.uiState.collectAsState()
			val petId = uiState.selectedPetId

			val dateParam = backStackEntry.arguments?.getString("date")
			FeedManagementScreen(
				navController = navController,
				selectedDate = dateParam,
				//petId = petId, // petId 전달
			)
		}

		composable(
			route = Screen.ActivityManagement.route,
			arguments = listOf(
				navArgument("date") {
					type = NavType.StringType
					nullable = true
					defaultValue = null
				}
			)
		) { backStackEntry ->
			val dateParam = backStackEntry.arguments?.getString("date")
			ActivityManagementScreen(
				navController = navController,
				selectedDate = dateParam
			)
		}

		composable(
			// 이전 답변에서 수정한 NavigationRoutes.kt의 경로를 사용합니다.
			route = Screen.WeightManagement.route,
			arguments = listOf(
				navArgument("date") {
					type = NavType.StringType
					nullable = true
					defaultValue = null
				}
			)
		) { backStackEntry ->
			val dateParam = backStackEntry.arguments?.getString("date")
			WeightManagementScreen(
				navController = navController,
				selectedDate = dateParam
			)
		}

		composable(
			route = Screen.PoopManagement.route,
			arguments = listOf(
				navArgument("date") {
					type = NavType.StringType
					nullable = true
					defaultValue = null
				}
			)
		) { backStackEntry ->
			val dateParam = backStackEntry.arguments?.getString("date")
			PoopManagementScreen(
				navController = navController,
				selectedDate = dateParam
			)
		}

		composable(
			route = Screen.VomitManagement.route,
			arguments = listOf(
				navArgument("date") {
					type = NavType.StringType
					nullable = true
					defaultValue = null
				}
			)
		) { backStackEntry ->
			val dateParam = backStackEntry.arguments?.getString("date")
			VomitManagementScreen(
				navController = navController,
				selectedDate = dateParam
			)
		}

		// 케어 기록 화면들 (실제 입력 화면)
		composable(Screen.FeedRecord.route) {
			FeedRecordScreen(
				onBackClick = { navController.popBackStack() }
			)
		}

		composable(Screen.ActivityRecord.route) {
			ActivityRecordScreen(
				onBackClick = { navController.popBackStack() }
			)
		}

		composable(Screen.WeightRecord.route) {
			WeightRecordScreen(
				onBackClick = { navController.popBackStack() }
			)
		}

		composable(Screen.WeightLog.route) {
			WeightLogScreen(
				onBackClick = { navController.popBackStack() }
			)
		}

		composable(Screen.PoopRecord.route) {
			PoopRecordScreen(
				onBackClick = { navController.popBackStack() }
			)
		}

		composable(Screen.VomitRecord.route) {
			VomitRecordScreen(
				onBackClick = { navController.popBackStack() }
			)
		}
		
		// 품종 가이드북 상세 화면
		composable(
			route = Screen.BreedGuidebook.route,
			arguments = listOf(navArgument("breedName") { type = NavType.StringType })
		) { backStackEntry ->
			val breedName = backStackEntry.arguments?.getString("breedName") ?: ""
			BreedGuidebookScreen(
				breedName = breedName,
				onBackClick = { navController.popBackStack() }
			)
		}
	}
}