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
import com.example.pet_project_frontend.presentation.mungstar.CartoonLoadingScreen
import com.example.pet_project_frontend.presentation.mungstar.PostDetailScreen
import com.example.pet_project_frontend.presentation.mungstar.UserPostsScreen
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

		// 게시글 상세 화면
		composable(
			route = "post_detail/{postId}",
			arguments = listOf(
				navArgument("postId") {
					type = NavType.StringType
				}
			)
		) { backStackEntry ->
			val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
			PostDetailScreen(
				postId = postId,
				navController = navController
			)
		}

		// 자유글 작성/수정 화면
		composable(
			route = "free_writing?postId={postId}&initialText={initialText}&imageUrls={imageUrls}",
			arguments = listOf(
				navArgument("postId") { 
					type = NavType.StringType
					nullable = true
					defaultValue = null
				},
				navArgument("initialText") {
					type = NavType.StringType
					nullable = true
					defaultValue = null
				},
				navArgument("imageUrls") {
					type = NavType.StringType
					nullable = true
					defaultValue = null
				}
			)
		) { backStackEntry ->
			val postId = backStackEntry.arguments?.getString("postId")
			val initialText = backStackEntry.arguments?.getString("initialText")
			val imageUrlsString = backStackEntry.arguments?.getString("imageUrls")
			val initialImageUrls = imageUrlsString?.split(",")?.filter { it.isNotBlank() }
			
			FreeWriting(
				navController = navController,
				postId = postId,
				initialText = initialText,
				initialImageUrls = initialImageUrls
			)
		}

		// 만화 제작 화면
		composable("cartoon_making") { backStackEntry ->
			CartoonMaking(navController = navController)
		}

		// 만화 로딩 화면
		composable(
			route = "cartoon_loading/{jobId}?userText={userText}",
			arguments = listOf(
				navArgument("jobId") {
					type = NavType.StringType
				},
				navArgument("userText") {
					type = NavType.StringType
					nullable = true
					defaultValue = null
				}
			)
		) { backStackEntry ->
			CartoonLoadingScreen(navController = navController)
		}

		// 사용자 게시물 화면
		composable(
			route = "user_posts/{author_id}",
			arguments = listOf(
				navArgument("author_id") {
					type = NavType.StringType
				}
			)
		) { backStackEntry ->
			val authorId = backStackEntry.arguments?.getString("author_id") ?: ""
			UserPostsScreen(
				navController = navController,
				authorId = authorId
			)
		}

		// 번역기 화면
		composable(Screen.Translator.route) { TranslatorScreen(openNotice = openNotice) }

		// 마이페이지 화면
		composable(Screen.MyPage.route) { 
			MyPageScreen(
				onNameClick = { /* TODO: 이름 수정 화면으로 이동 */ },
				onBirthdateClick = { /* TODO: 생년월일 수정 화면으로 이동 */ },
				onGenderClick = { /* TODO: 성별 수정 화면으로 이동 */ },
				onBreedClick = { /* TODO: 견종 수정 화면으로 이동 */ },
				onNotificationClick = { /* TODO: 알림 설정 화면으로 이동 */ },
				onVerificationClick = { /* TODO: 본인 인증 화면으로 이동 */ },
				onTermsClick = { /* TODO: 이용약관 화면으로 이동 */ },
				onPrivacyClick = { /* TODO: 개인정보 처리방침 화면으로 이동 */ },
				onWithdrawClick = { /* TODO: 회원 탈퇴 화면으로 이동 */ },
				onProfileImageClick = { /* 프로필 이미지 클릭 (ViewModel에서 처리) */ }
			)
		}
	}
}