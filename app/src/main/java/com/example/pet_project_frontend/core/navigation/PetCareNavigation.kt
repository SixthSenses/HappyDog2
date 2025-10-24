package com.example.pet_project_frontend.core.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.pet_project_frontend.presentation.auth.LoginScreen
import com.example.pet_project_frontend.presentation.breed_guide.BreedGuideListScreen
import com.example.pet_project_frontend.presentation.breed_guide.BreedGuidebookScreen
import com.example.pet_project_frontend.presentation.care_management.ActivityManagementScreen
import com.example.pet_project_frontend.presentation.care_management.FeedManagementScreen
import com.example.pet_project_frontend.presentation.care_management.PoopManagementScreen
import com.example.pet_project_frontend.presentation.care_management.VomitManagementScreen
import com.example.pet_project_frontend.presentation.care_management.WeightManagementScreen
import com.example.pet_project_frontend.presentation.care_record.ActivityRecordScreen
import com.example.pet_project_frontend.presentation.care_record.FeedRecordScreen
import com.example.pet_project_frontend.presentation.care_record.PoopRecordScreen
import com.example.pet_project_frontend.presentation.care_record.VomitRecordScreen
import com.example.pet_project_frontend.presentation.care_record.WeightLogScreen
import com.example.pet_project_frontend.presentation.care_record.WeightRecordScreen
import com.example.pet_project_frontend.presentation.eye_health.EyeHealthHistoryScreen
import com.example.pet_project_frontend.presentation.eye_health.EyeHealthScreen
import com.example.pet_project_frontend.presentation.eye_health.ImageViewerScreen
import com.example.pet_project_frontend.presentation.health_survey.HealthSurveyScreen
import com.example.pet_project_frontend.presentation.map.MapScreen
import com.example.pet_project_frontend.presentation.mungstar.CartoonLoadingScreen
import com.example.pet_project_frontend.presentation.mungstar.CartoonMaking
import com.example.pet_project_frontend.presentation.mungstar.FreeWriting
import com.example.pet_project_frontend.presentation.mungstar.MungStarFeed
import com.example.pet_project_frontend.presentation.mungstar.PostDetailScreen
import com.example.pet_project_frontend.presentation.mungstar.UserPostsScreen
import com.example.pet_project_frontend.presentation.mypage.main.MyPageScreen
import com.example.pet_project_frontend.presentation.mypage.main.MyPageViewModel
import com.example.pet_project_frontend.presentation.mypage.profile.birthdate.BirthEditRoute
import com.example.pet_project_frontend.presentation.mypage.profile.breed.BreedSelectScreen
import com.example.pet_project_frontend.presentation.mypage.profile.gender.GenderSelectScreen
import com.example.pet_project_frontend.presentation.mypage.profile.gender.GenderUi
import com.example.pet_project_frontend.presentation.mypage.profile.name.NameEditRoute
import com.example.pet_project_frontend.presentation.mypage.settings.notification.NotificationSettingsScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.IdentityVerificationViewModel
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationGuideError
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationGuideScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.components.VerificationLoadingScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationMainScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.components.VerificationResult
import com.example.pet_project_frontend.presentation.mypage.settings.verification.components.VerificationResultScreen
import com.example.pet_project_frontend.presentation.mypage.withdrawal.WithdrawalScreen
import com.example.pet_project_frontend.presentation.petcare.home.PetCareHomeScreen
import com.example.pet_project_frontend.presentation.petcare.home.PetCareHomeViewModel
import com.example.pet_project_frontend.presentation.petregistration.PetRegistrationScreen
import com.example.pet_project_frontend.presentation.translator.TranslatorScreen
import java.io.File
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
                    val target = if (isNewUser) Screen.PetRegistration.route else Screen.PetCare.route
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
            val viewModel: PetCareHomeViewModel = hiltViewModel()
            PetCareHomeScreen(
                viewModel = viewModel,
                onHealthSurveyClick = { navController.navigate(Screen.HealthSurvey.route) },
                onBreedGuideClick = { navController.navigate(Screen.BreedGuide.route) },
                onEyeCheckClick = { navController.navigate(Screen.EyeHealth.route) },
                onFeedClick = { navController.navigate(Screen.FeedManagement.createRoute(null)) },
                onActivityClick = { navController.navigate(Screen.ActivityManagement.createRoute(null)) },
                onWeightClick = { navController.navigate(Screen.WeightManagement.createRoute(null)) },
                onPoopClick = { date -> navController.navigate(Screen.PoopManagement.createRoute(date)) },
                onVomitClick = { date -> navController.navigate(Screen.VomitManagement.createRoute(date)) }
            )
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
        composable("cartoon_making") {
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
        ) {
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
                onNameClick = { petId, currentName ->
                    navController.navigate(Screen.EditPetName.createRoute(currentName, petId))
                },
                onBirthdateClick = { birth ->
                    navController.navigate(Screen.EditBirthDate.createRoute(birth))
                },
                onGenderClick = { gender ->
                    navController.navigate(Screen.SelectGender.createRoute(gender))
                },
                onBreedClick = { breed ->
                    navController.navigate(Screen.SelectBreed.createRoute(breed))
                },
                onNotificationClick = {
                    navController.navigate(Screen.NotificationSettings.route)
                },
                onVerificationClick = {
                    navController.navigate(Screen.VerificationIntro.route)
                },
                onTermsClick = { /* TODO 서버 연동 시 치환 */ },
                onPrivacyClick = { /* TODO 서버 연동 시 치환 */ },
                onWithdrawClick = {
                    navController.navigate(Screen.Withdraw.route)
                },
                onProfileImageClick = { /* TODO 서버 연동 시 치환 */ }
            )
        }

        composable(
            route = Screen.EditPetName.route,
            arguments = listOf(
                navArgument("initialName") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("petId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            NameEditRoute(navController = navController)
        }

        composable(
            route = Screen.EditBirthDate.route,
            arguments = listOf(
                navArgument("initialBirth") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            BirthEditRoute(navController = navController)
        }

        composable(
            route = Screen.SelectGender.route,
            arguments = listOf(
                navArgument("initialGender") {
                    type = NavType.StringType
                    defaultValue = "수컷"
                }
            )
        ) {
            val myPageViewModel: MyPageViewModel = hiltViewModel(
                navController.getBackStackEntry(Screen.MyPage.route)
            )
            GenderSelectScreen(
                onBack = { navController.popBackStack() },
                onSaved = { selected, shouldReload ->
                    val label = when (selected) {
                        GenderUi.MALE -> "수컷"
                        GenderUi.FEMALE -> "암컷"
                    }
                    myPageViewModel.updateGender(label)
                    if (shouldReload) {
                        myPageViewModel.loadUserData()
                    }
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.SelectBreed.route,
            arguments = listOf(
                navArgument("initialBreed") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            val myPageViewModel: MyPageViewModel = hiltViewModel(
                navController.getBackStackEntry(Screen.MyPage.route)
            )
            BreedSelectScreen(
                onBack = { navController.popBackStack() },
                onNext = { breed, shouldReload ->
                    myPageViewModel.updateBreed(breed)
                    if (shouldReload) {
                        myPageViewModel.loadUserData()
                    }
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Withdraw.route) {
            WithdrawalScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.VerificationIntro.route) {
            val myPageViewModel: MyPageViewModel = hiltViewModel(
                navController.getBackStackEntry(Screen.MyPage.route)
            )
            val myPageState by myPageViewModel.uiState.collectAsState()

            val verificationViewModel: IdentityVerificationViewModel = hiltViewModel()
            val introUiState by verificationViewModel.introUiState.collectAsState()

            LaunchedEffect(myPageState.isPetVerified, myPageState.hasRegisteredNosePrint) {
                if (myPageState.isPetVerified && myPageState.hasRegisteredNosePrint) {
                    verificationViewModel.showAlreadyVerifiedDialog()
                }
            }

            LaunchedEffect(myPageState.error) {
                if (!myPageState.error.isNullOrBlank()) {
                    verificationViewModel.showUnknownErrorDialog(myPageState.error)
                }
            }

            VerificationMainScreen(
                onBack = { navController.popBackStack() },
                onVerifyClick = {
                    navController.navigate(Screen.VerificationGuide.route)
                },
                showAlreadyVerifiedDialog = introUiState.showAlreadyVerifiedDialog,
                onDismissAlreadyVerifiedDialog = {
                    verificationViewModel.dismissAlreadyVerifiedDialog()
                    navController.popBackStack()
                },
                showUnknownErrorDialog = introUiState.showUnknownErrorDialog,
                onDismissUnknownErrorDialog = {
                    verificationViewModel.dismissUnknownErrorDialog()
                    myPageViewModel.clearError()
                }
            )
        }

        composable(Screen.VerificationGuide.route) { guideEntry ->
            val verificationViewModel: IdentityVerificationViewModel = hiltViewModel(
                navController.getBackStackEntry(Screen.VerificationIntro.route)
            )
            val myPageViewModel: MyPageViewModel = hiltViewModel(
                navController.getBackStackEntry(Screen.MyPage.route)
            )
            val myPageState by myPageViewModel.uiState.collectAsState()
            val context = LocalContext.current

            val errorType by guideEntry.savedStateHandle
                .getStateFlow<String?>("verification_error", null)
                .collectAsState()
            val errorDialogType = VerificationGuideError.fromName(errorType)

            VerificationGuideScreen(
                onBack = { navController.popBackStack() },
                onPickImage = { uriString ->
                    guideEntry.savedStateHandle["verification_error"] = null
                    val file = copyUriToCache(context, uriString)
                    val petId = myPageState.petId
                    if (file != null && !petId.isNullOrBlank()) {
                        verificationViewModel.onImageSelected(file)
                        navController.navigate(Screen.VerificationLoading.createRoute(petId))
                    } else {
                        verificationViewModel.showUnknownErrorDialog()
                        navController.popBackStack(Screen.VerificationIntro.route, false)
                    }
                },
                errorDialog = errorDialogType,
                onDismissError = {
                    guideEntry.savedStateHandle["verification_error"] = null
                }
            )
        }

        composable(
            route = Screen.VerificationLoading.route,
            arguments = listOf(navArgument("petId") { type = NavType.StringType })
        ) {
            val verificationViewModel: IdentityVerificationViewModel = hiltViewModel(
                navController.getBackStackEntry(Screen.VerificationIntro.route)
            )
            val petId = it.arguments?.getString("petId").orEmpty()
            VerificationLoadingScreen(
                viewModel = verificationViewModel,
                petId = petId,
                onResult = { result ->
                    when (result) {
                        is VerificationResult.Success -> {
                            navController.navigate(Screen.VerificationSuccess.route) {
                                popUpTo(Screen.VerificationIntro.route) { inclusive = true }
                            }
                        }

                        VerificationResult.Duplicate -> {
                            runCatching {
                                navController.getBackStackEntry(Screen.VerificationGuide.route).savedStateHandle
                            }.getOrNull()?.set(
                                "verification_error",
                                VerificationGuideError.Duplicate.name
                            )
                            navController.popBackStack(Screen.VerificationGuide.route, false)
                        }

                        VerificationResult.DetectionFailed,
                        VerificationResult.InvalidImage -> {
                            runCatching {
                                navController.getBackStackEntry(Screen.VerificationGuide.route).savedStateHandle
                            }.getOrNull()?.set(
                                "verification_error",
                                VerificationGuideError.DetectionFailed.name
                            )
                            navController.popBackStack(Screen.VerificationGuide.route, false)
                        }

                        VerificationResult.AlreadyVerified -> {
                            verificationViewModel.showAlreadyVerifiedDialog()
                            navController.popBackStack(Screen.VerificationIntro.route, false)
                        }

                        else -> {
                            verificationViewModel.showUnknownErrorDialog()
                            navController.popBackStack(Screen.VerificationIntro.route, false)
                        }
                    }
                }
            )
        }

        composable(Screen.VerificationSuccess.route) {
            VerificationResultScreen(
                onConfirm = {
                    navController.popBackStack(Screen.MyPage.route, false)
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
            HealthSurveyScreen(
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
    }
}

private fun copyUriToCache(context: Context, uriString: String): File? {
    return try {
        val uri = Uri.parse(uriString)
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.takeIf { it.isNotBlank() }
            ?: "verification_${System.currentTimeMillis()}.jpg"
        val tempFile = File(context.cacheDir, fileName)
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        null
    }
}
