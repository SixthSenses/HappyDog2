package com.example.pet_project_frontend.core.navigation

// sealed class Screen 하나로 모든 경로를 관리합니다.
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object PetRegistration : Screen("pet_registration")
    object PetCare : Screen("petcare")
    object Map : Screen("map")
    object Community : Screen("community")
    object Translator : Screen("translator")
    object MyPage : Screen("mypage")

    // 인자가 필요한 화면들
    object PetProfile : Screen("pet_profile/{petId}") {
        fun createRoute(petId: String) = "pet_profile/$petId"
    }

    object CareRecord : Screen("care_record/{petId}/{recordType}") {
        fun createRoute(petId: String, recordType: String) = "care_record/$petId/$recordType"
    }

    object BreedDetail : Screen("breed_detail/{breedName}") {
        fun createRoute(breedName: String) = "breed_detail/$breedName"
    }

    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
}