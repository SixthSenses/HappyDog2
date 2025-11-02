package com.example.pet_project_frontend.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.navDeepLink
import com.example.pet_project_frontend.R
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
import com.example.pet_project_frontend.presentation.mungstar.MungStarFeed
import com.example.pet_project_frontend.presentation.mungstar.FreeWriting
import com.example.pet_project_frontend.presentation.mungstar.CartoonMaking
import com.example.pet_project_frontend.presentation.mungstar.CartoonLoadingScreen
import com.example.pet_project_frontend.presentation.mungstar.PostDetailScreen
import com.example.pet_project_frontend.presentation.mungstar.UserPostsScreen
import com.example.pet_project_frontend.presentation.notification.NotificationScreen
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
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationMainScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationGuideScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationGuideError
import com.example.pet_project_frontend.presentation.mypage.settings.verification.components.VerificationLoadingScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.components.VerificationResultScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.components.VerificationResult
import com.example.pet_project_frontend.presentation.mypage.settings.verification.IdentityVerificationViewModel
import com.example.pet_project_frontend.presentation.mypage.profile.name.NameEditRoute
import com.example.pet_project_frontend.presentation.mypage.profile.birthdate.BirthEditRoute
import com.example.pet_project_frontend.presentation.mypage.profile.gender.GenderSelectScreen
import com.example.pet_project_frontend.presentation.mypage.profile.breed.BreedSelectScreen
import com.example.pet_project_frontend.presentation.mypage.settings.notification.NotificationSettingsScreen
import com.example.pet_project_frontend.presentation.mypage.withdrawal.WithdrawalScreen
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

		// 알림 목록 화면
		composable("notification") {
			NotificationScreen(navController = navController)
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
				onNameClick = { initialName, petId ->
					navController.navigate(Screen.EditPetName.createRoute(initialName ?: "", petId))
				},
				onBirthdateClick = { initialBirth ->
					navController.navigate(Screen.EditBirthDate.createRoute(initialBirth))
				},
				onGenderClick = { initialGender ->
					navController.navigate(Screen.SelectGender.createRoute(initialGender))
				},
				onBreedClick = { initialBreed ->
					navController.navigate(Screen.SelectBreed.createRoute(initialBreed))
				},
				onNotificationClick = {
					navController.navigate(Screen.NotificationSettings.route)
				},
				onVerificationClick = {
					navController.navigate(Screen.VerificationIntro.route)
				},
				onTermsClick = {
					// TODO: 이용약관 화면으로 이동
				},
				onPrivacyClick = {
					// TODO: 개인정보처리방침 화면으로 이동
				},
				onWithdrawClick = {
					navController.navigate(Screen.Withdraw.route)
				}
			)
		}

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
			val dateParam = backStackEntry.arguments?.getString("date")
			FeedManagementScreen(
				navController = navController,
				selectedDate = dateParam,
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

		// 신원 인증 메인 화면 (소개 화면)
		composable(Screen.VerificationIntro.route) { backStackEntry ->
			val parentEntry = remember(backStackEntry) {
				navController.currentBackStackEntry ?: backStackEntry
			}
			val viewModel: IdentityVerificationViewModel = hiltViewModel(parentEntry)
			val introUiState by viewModel.introUiState.collectAsState()
			
			// 화면 진입 시 펫의 인증 상태 확인
			LaunchedEffect(Unit) {
				viewModel.checkVerificationStatus()
			}
			
			VerificationMainScreen(
				onBack = { navController.popBackStack() },
				onVerifyClick = {
					// 이미 인증된 경우 이동하지 않음 (다이얼로그가 표시됨)
					if (!introUiState.showAlreadyVerifiedDialog) {
						navController.navigate(Screen.VerificationGuide.route)
					}
				},
				showAlreadyVerifiedDialog = introUiState.showAlreadyVerifiedDialog,
				onDismissAlreadyVerifiedDialog = { 
					viewModel.dismissAlreadyVerifiedDialog()
					navController.popBackStack()  // 다이얼로그 닫고 뒤로 가기
				},
				showUnknownErrorDialog = introUiState.showUnknownErrorDialog,
				onDismissUnknownErrorDialog = { viewModel.dismissUnknownErrorDialog() }
			)
		}

		// 신원 인증 가이드 화면 (카메라 촬영)
		// Navigation graph 레벨에서 ViewModel을 생성하여 Guide와 Loading 화면 간 공유
		composable(Screen.VerificationGuide.route) { backStackEntry ->
			val parentEntry = remember(backStackEntry) {
				navController.getBackStackEntry(Screen.VerificationIntro.route)
			}
			val viewModel: IdentityVerificationViewModel = hiltViewModel(parentEntry)
			val guideUiState by viewModel.guideUiState.collectAsState()
			
			VerificationGuideScreen(
				onBack = { navController.popBackStack() },
				onPickImage = { imageUri ->
					// 이미지 URI를 ViewModel에 저장
					viewModel.setImageFromUri(imageUri)
					
					// 로딩 화면으로 이동 (petId는 로딩 화면에서 조회)
					navController.navigate(Screen.VerificationLoading.createRoute("loading")) {
						popUpTo(Screen.VerificationIntro.route) { inclusive = false }
					}
				},
				errorDialog = guideUiState.errorDialog,
				onDismissError = { viewModel.dismissGuideError() }
			)
		}

		// 신원 인증 로딩 화면
		composable(
			route = Screen.VerificationLoading.route,
			arguments = listOf(navArgument("petId") { type = NavType.StringType })
		) { backStackEntry ->
			// Guide 화면과 동일한 ViewModel 인스턴스 공유
			val parentEntry = remember(backStackEntry) {
				navController.getBackStackEntry(Screen.VerificationIntro.route)
			}
			val viewModel: IdentityVerificationViewModel = hiltViewModel(parentEntry)
			
			// petId를 ViewModel에서 동적으로 가져옴
			var petId by remember { mutableStateOf<String?>(null) }
			
			LaunchedEffect(Unit) {
				petId = viewModel.getPetId()
			}
			
			// petId가 로드될 때까지 대기
			petId?.let { id ->
				VerificationLoadingScreen(
					viewModel = viewModel,
					petId = id,
					onResult = { result ->
						when (result) {
							is VerificationResult.Success -> {
								navController.navigate(Screen.VerificationSuccess.route) {
									popUpTo(Screen.VerificationIntro.route) { inclusive = true }
								}
							}
							is VerificationResult.Duplicate -> {
								// 중복 비문 에러 다이얼로그 표시
								viewModel.showGuideError(VerificationGuideError.Duplicate)
								navController.popBackStack()
							}
							is VerificationResult.DetectionFailed,
							is VerificationResult.InvalidImage -> {
								// 비문 인식 실패 에러 다이얼로그 표시
								viewModel.showGuideError(VerificationGuideError.DetectionFailed)
								navController.popBackStack()
							}
							is VerificationResult.AlreadyVerified -> {
								// 이미 인증됨 다이얼로그는 Intro 화면에 표시
								viewModel.showAlreadyVerifiedDialog()
								navController.popBackStack(Screen.VerificationIntro.route, inclusive = false)
							}
							is VerificationResult.Failed,
							is VerificationResult.Unknown -> {
								// 알 수 없는 오류 다이얼로그는 Intro 화면에 표시
								viewModel.showUnknownErrorDialog()
								navController.popBackStack(Screen.VerificationIntro.route, inclusive = false)
							}
						}
					}
				)
			}
		}

		// 신원 인증 성공 화면
		composable(Screen.VerificationSuccess.route) {
			VerificationResultScreen(
				title = "신원 인증 성공!",
				subtitle = "멍스타그램에서\n인증 배지를 받았어요",
				imageResId = R.drawable.dog,
				onConfirm = {
					navController.navigate(Screen.MyPage.route) {
						popUpTo(Screen.MyPage.route) { inclusive = true }
					}
				}
			)
		}

		// 마이페이지 - 이름 수정
		composable(
			route = Screen.EditPetName.route,
			arguments = listOf(
				navArgument("initialName") { 
					type = NavType.StringType
					nullable = true
					defaultValue = null
				},
				navArgument("petId") { 
					type = NavType.StringType
					nullable = true
					defaultValue = null
				}
			)
		) {
			NameEditRoute(navController = navController)
		}

		// 마이페이지 - 생년월일 수정
		composable(
			route = Screen.EditBirthDate.route,
			arguments = listOf(
				navArgument("initialBirth") { 
					type = NavType.StringType
					nullable = true
					defaultValue = null
				}
			)
		) {
			BirthEditRoute(navController = navController)
		}

		// 마이페이지 - 성별 선택
		composable(
			route = Screen.SelectGender.route,
			arguments = listOf(
				navArgument("initialGender") { 
					type = NavType.StringType
					nullable = true
					defaultValue = null
				}
			)
		) {
			GenderSelectScreen(
				onBack = { navController.popBackStack() },
				onSaved = { gender, shouldReload ->
					navController.popBackStack()
				}
			)
		}

		// 마이페이지 - 견종 선택
		composable(
			route = Screen.SelectBreed.route,
			arguments = listOf(
				navArgument("initialBreed") { 
					type = NavType.StringType
					nullable = true
					defaultValue = null
				}
			)
		) {
			BreedSelectScreen(
				onBack = { navController.popBackStack() },
				onNext = { breed, shouldReload ->
					navController.popBackStack()
				}
			)
		}

		// 마이페이지 - 알림 설정
		composable(Screen.NotificationSettings.route) {
			NotificationSettingsScreen(
				onBack = { navController.popBackStack() }
			)
		}

		// 마이페이지 - 회원 탈퇴
		composable(Screen.Withdraw.route) {
			WithdrawalScreen(
				onBack = { navController.popBackStack() },
				onFinished = {
					// 로그인 화면으로 이동하고 백스택 모두 제거
					navController.navigate(Screen.Login.route) {
						popUpTo(0) { inclusive = true }
					}
				}
			)
		}
	}
}