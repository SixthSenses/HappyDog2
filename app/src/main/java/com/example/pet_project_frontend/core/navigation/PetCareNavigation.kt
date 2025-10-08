package com.example.pet_project_frontend.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
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
import com.example.pet_project_frontend.presentation.community.CommunityScreen
import com.example.pet_project_frontend.presentation.community.PostDetailScreen
import com.example.pet_project_frontend.presentation.community.CreatePostScreen
// import com.example.pet_project_frontend.presentation.breed_guide.BreedGuideListScreen // TODO: BreedGuideListScreen 구현 필요

@Composable
fun PetCareNavHost(
	navController: NavHostController,
	startDestination: String,
	openNotice: (@Composable (closeNotice: () -> Unit) -> Unit) -> Unit,
	onRefreshPetStatus: suspend () -> Unit,
	modifier: Modifier = Modifier
) {
	NavHost(
		navController = navController,
		startDestination = startDestination,
		modifier = modifier
	) {
		// 로그인 화면
		composable(Screen.Login.route) {
			val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
			LoginScreen(
				onLoginResult = { isNewUser ->
					// 로그인 성공 후 펫 상태 새로고침을 기다린 후 화면 전환
					coroutineScope.launch {
						// 🔥 핵심: 펫 상태를 서버에서 확인하고 완료될 때까지 대기
						onRefreshPetStatus()
						
						// isNewUser인 경우 무조건 등록 화면으로
						// 기존 사용자는 PetCare로 이동 (MainActivity의 petStatus가 이미 업데이트됨)
						val target = if (isNewUser) {
							Screen.PetRegistration.route
						} else {
							Screen.PetCare.route
						}
						
						navController.navigate(target) {
							popUpTo(Screen.Login.route) { inclusive = true }
							launchSingleTop = true
						}
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
			PetCareHomeScreen(
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
				onFeedClick = { /* TODO: 사료 기록 화면으로 네비게이션 */ },
				onActivityClick = { /* TODO: 활동 기록 화면으로 네비게이션 */ },
				onWeightClick = { /* TODO: 몸무게 기록 화면으로 네비게이션 */ },
				onPoopClick = { /* TODO: 대변 기록 화면으로 네비게이션 */ },
				onVomitClick = { /* TODO: 구토 기록 화면으로 네비게이션 */ }
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

		// 커뮤니티 화면
		composable(Screen.Community.route) {
			CommunityScreen(
				onNavigateToPostDetail = { postId ->
					navController.navigate(Screen.PostDetail.createRoute(postId))
				},
				onNavigateToCreatePost = {
					navController.navigate(Screen.CreatePost.route)
				},
				onNavigateToUserProfile = { userId ->
					// TODO: 사용자 프로필 화면 구현 시 추가
				}
			)
		}

		// 게시글 상세 화면
		composable(Screen.PostDetail.route) {
			PostDetailScreen(
				onNavigateBack = { navController.popBackStack() },
				onNavigateToUserProfile = { userId ->
					// TODO: 사용자 프로필 화면 구현 시 추가
				}
			)
		}

		// 게시글 작성 화면
		composable(Screen.CreatePost.route) {
			CreatePostScreen(
				onNavigateBack = { navController.popBackStack() },
				onPostCreated = {
					navController.popBackStack()
					// Community 화면으로 돌아가면서 자동 새로고침될 것
				}
			)
		}

		// 번역기 화면
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

		// 견종 가이드북 화면 - TODO: BreedGuideListScreen 구현 필요
		// composable(Screen.BreedGuide.route) {
		// 	BreedGuideListScreen(
		// 		onBackClick = { navController.popBackStack() },
		// 		onBreedClick = { breedName ->
		// 			// TODO: 견종 상세 화면으로 네비게이션 (필요 시 구현)
		// 		}
		// 	)
		// }
	}
}