package com.example.pet_project_frontend.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.example.pet_project_frontend.presentation.mypage.withdrawal.WithdrawalScreen
import com.example.pet_project_frontend.presentation.petcare.PetCareMainScreen
import com.example.pet_project_frontend.presentation.petregistration.PetRegistrationScreen
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
        // 로그인
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

        // 반려견 등록
        composable(Screen.PetRegistration.route) {
            PetRegistrationScreen(navController = navController)
        }

        // 펫케어 메인
        composable(Screen.PetCare.route) {
            PetCareMainScreen()
        }

        // 펫케어 대시보드 딥링크
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

        // 지도 및 기타 탭
        composable(Screen.Map.route) { MapScreen() }
        composable(Screen.Community.route) { PetCareMainScreen() }
        composable(Screen.Translator.route) { TranslatorScreen(openNotice = openNotice) }

        // 마이페이지 메인
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
                    // TODO: 본인 인증 화면 연결 필요
                },
                onTermsClick = {
                    // TODO: 이용약관 화면 연결 필요
                },
                onPrivacyClick = {
                    // TODO: 개인정보 처리방침 화면 연결 필요
                },
                onWithdrawClick = {
                    navController.navigate(Screen.Withdraw.route)
                },
                onProfileImageClick = {
                    // TODO: 프로필 이미지 편집 흐름 연결 필요
                }
            )
        }

        // 이름 수정
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

        // 생년월일 수정
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

        // 성별 선택
        composable(
            route = Screen.SelectGender.route,
            arguments = listOf(
                navArgument("initialGender") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            val parentEntry = remember(navController) { navController.getBackStackEntry(Screen.MyPage.route) }
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

        // 견종 선택
        composable(
            route = Screen.SelectBreed.route,
            arguments = listOf(
                navArgument("initialBreed") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            val parentEntry = remember(navController) { navController.getBackStackEntry(Screen.MyPage.route) }
            val myPageViewModel: MyPageViewModel = hiltViewModel(parentEntry)

            BreedSelectScreen(
                onBack = { navController.popBackStack() },
                onNext = { breed ->
                    myPageViewModel.updateBreed(breed)
                    navController.popBackStack()
                }
            )
        }

        // 알림 설정
        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }

        // 회원 탈퇴
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
    }
}
