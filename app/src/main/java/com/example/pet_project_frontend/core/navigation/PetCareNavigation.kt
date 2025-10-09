package com.example.pet_project_frontend.core.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.pet_project_frontend.presentation.auth.LoginScreen
import com.example.pet_project_frontend.presentation.map.MapScreen
import com.example.pet_project_frontend.presentation.mypage.main.MyPageScreen
import com.example.pet_project_frontend.presentation.mypage.main.MyPageViewModel
import com.example.pet_project_frontend.presentation.mypage.profile.birth.BirthEditRoute
import com.example.pet_project_frontend.presentation.mypage.profile.breed.BreedSelectScreen
import com.example.pet_project_frontend.presentation.mypage.profile.gender.GenderSelectScreen
import com.example.pet_project_frontend.presentation.mypage.profile.gender.GenderUi
import com.example.pet_project_frontend.presentation.mypage.profile.name.NameEditRoute
import com.example.pet_project_frontend.presentation.mypage.settings.notification.NotificationSettingsScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.IdentityVerificationViewModel
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationGuideError
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationGuideDetectionErrorDialog
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationGuideDuplicateErrorDialog
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationGuideScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationLoadingScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationMainScreen
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationResult
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationResultSuccessScreen
import com.example.pet_project_frontend.presentation.mypage.withdrawal.WithdrawalScreen
import com.example.pet_project_frontend.presentation.petcare.PetCareMainScreen
import com.example.pet_project_frontend.presentation.petregistration.PetRegistrationScreen
import com.example.pet_project_frontend.presentation.translator.TranslatorScreen
import java.io.File

@Composable
fun PetCareNavHost(
    navController: NavHostController,
    startDestination: String,
    openNotice: (@Composable (closeNotice: () -> Unit) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
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

        composable(Screen.PetRegistration.route) {
            PetRegistrationScreen(navController = navController)
        }

        composable(Screen.PetCare.route) {
            PetCareMainScreen()
        }

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
        ) {
            PetCareMainScreen()
        }

        composable(Screen.Map.route) { MapScreen() }
        composable(Screen.Community.route) { PetCareMainScreen() }
        composable(Screen.Translator.route) { TranslatorScreen(openNotice = openNotice) }

        composable(Screen.MyPage.route) {
            MyPageScreen(
                onNameClick = { currentName ->
                    navController.navigate(Screen.EditPetName.createRoute(currentName))
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
                onTermsClick = { /* TODO */ },
                onPrivacyClick = { /* TODO */ },
                onWithdrawClick = {
                    navController.navigate(Screen.Withdraw.route)
                },
                onProfileImageClick = { /* TODO */ }
            )
        }

        composable(
            route = Screen.EditPetName.route,
            arguments = listOf(
                navArgument("initialName") {
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
                    nullable = true
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
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.MyPage.route) }
            val myPageViewModel: MyPageViewModel = hiltViewModel(parentEntry)

            GenderSelectScreen(
                onBack = { navController.popBackStack() },
                onSaved = { selected ->
                    val label = when (selected) {
                        GenderUi.MALE -> "수컷"
                        GenderUi.FEMALE -> "암컷"
                    }
                    myPageViewModel.updateGender(label)
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
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.MyPage.route) }
            val myPageViewModel: MyPageViewModel = hiltViewModel(parentEntry)

            BreedSelectScreen(
                onBack = { navController.popBackStack() },
                onNext = { breed ->
                    myPageViewModel.updateBreed(breed)
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Withdraw.route) {
            WithdrawalScreen(
                onBack = { navController.popBackStack() },
                onFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.VerificationIntro.route) {
            val verificationViewModel: IdentityVerificationViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                verificationViewModel.resetVerificationResult()
            }
            VerificationMainScreen(
                onBack = { navController.popBackStack() },
                onVerifyClick = {
                    navController.navigate(Screen.VerificationGuide.route)
                }
            )
        }

        composable(Screen.VerificationGuide.route) { backStackEntry ->
            val verificationParent = remember(backStackEntry) { navController.getBackStackEntry(Screen.VerificationIntro.route) }
            val verificationViewModel: IdentityVerificationViewModel = hiltViewModel(verificationParent)

            val myPageEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.MyPage.route) }
            val myPageViewModel: MyPageViewModel = hiltViewModel(myPageEntry)
            val myPageState by myPageViewModel.uiState.collectAsStateWithLifecycle()

            val guideEntry = backStackEntry
            val errorFlow = guideEntry.savedStateHandle.getStateFlow<String?>("verification_error", null)
            val errorValue by errorFlow.collectAsState()
            val errorDialog = when (errorValue) {
                VerificationResult.Duplicate.name,
                VerificationResult.AlreadyVerified.name -> VerificationGuideError.Duplicate
                VerificationResult.InvalidImage.name,
                VerificationResult.Failed.name,
                VerificationResult.Unknown.name -> VerificationGuideError.DetectionFailed
                else -> null
            }

            VerificationGuideScreen(
                onBack = { navController.popBackStack() },
                onPickImage = { uriString ->
                    guideEntry.savedStateHandle["verification_error"] = null
                    val file = copyUriToCache(context, uriString)
                    if (file != null) {
                        verificationViewModel.onImageSelected(file)
                        val petId = myPageState.petId
                        if (!petId.isNullOrBlank()) {
                            navController.navigate(Screen.VerificationLoading.createRoute(petId))
                        } else {
                            guideEntry.savedStateHandle["verification_error"] = VerificationResult.Failed.name
                        }
                    } else {
                        guideEntry.savedStateHandle["verification_error"] = VerificationResult.InvalidImage.name
                    }
                },
                onOpenCamera = { /* TODO: open camera */ },
                errorDialog = errorDialog,
                onDismissError = { guideEntry.savedStateHandle["verification_error"] = null }
            )
        }

        composable(
            route = Screen.VerificationLoading.route,
            arguments = listOf(
                navArgument("petId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId")
            if (petId == null) {
                navController.popBackStack()
                return@composable
            }
            val verificationParent = remember(backStackEntry) { navController.getBackStackEntry(Screen.VerificationIntro.route) }
            val verificationViewModel: IdentityVerificationViewModel = hiltViewModel(verificationParent)

            VerificationLoadingScreen(
                petId = petId,
                viewModel = verificationViewModel,
                onResult = { result ->
                    when (result) {
                        VerificationResult.Idle -> Unit
                        VerificationResult.Success -> {
                            verificationViewModel.resetVerificationResult()
                            navController.navigate(Screen.VerificationSuccess.route) {
                                popUpTo(Screen.VerificationGuide.route) { inclusive = false }
                            }
                        }

                        VerificationResult.Duplicate,
                        VerificationResult.AlreadyVerified -> {
                            verificationViewModel.resetVerificationResult()
                            navController.popBackStack(Screen.VerificationGuide.route, inclusive = false)
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "verification_error",
                                VerificationResult.Duplicate.name
                            )
                        }

                        VerificationResult.InvalidImage,
                        VerificationResult.Failed,
                        VerificationResult.Unknown -> {
                            verificationViewModel.resetVerificationResult()
                            navController.popBackStack(Screen.VerificationGuide.route, inclusive = false)
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "verification_error",
                                VerificationResult.InvalidImage.name
                            )
                        }
                    }
                },
                onBack = {
                    verificationViewModel.resetVerificationResult()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.VerificationSuccess.route) { backStackEntry ->
            val verificationParent = remember(backStackEntry) { navController.getBackStackEntry(Screen.VerificationIntro.route) }
            val verificationViewModel: IdentityVerificationViewModel = hiltViewModel(verificationParent)
            val myPageEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.MyPage.route) }
            val myPageViewModel: MyPageViewModel = hiltViewModel(myPageEntry)

            LaunchedEffect(Unit) {
                myPageViewModel.loadUserData()
            }

            VerificationResultSuccessScreen(
                onClose = {
                    verificationViewModel.resetVerificationResult()
                    navController.navigate(Screen.MyPage.route) {
                        popUpTo(Screen.VerificationIntro.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
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

