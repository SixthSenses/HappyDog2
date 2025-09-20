package com.example.pet_project_frontend.core.navigation

// sealed class Screen 하나로 모든 경로를 관리합니다.
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object PetRegistration : Screen("pet_registration")
    object PetCare : Screen("petcare")
    object PetCareHome : Screen("petcare_home")
    object Map : Screen("map")
    object Community : Screen("community")
    object Translator : Screen("translator")
    object MyPage : Screen("mypage")

    // 펫케어 대시보드(딥링크 표준과 맞춤) - core:navigation 상수 사용
    object PetCareDashboard : Screen(com.example.pet_project_frontend.core.navigation.Routes.PetCare.Dashboard) {
        fun createRoute(
            petId: String? = null,
            date: String? = null,
            tab: String? = null
        ): String {
            val base = "pet_care/dashboard"
            val params = buildList {
                if (petId != null) add("petId=$petId")
                if (date != null) add("date=$date")
                if (tab != null) add("tab=$tab")
            }.joinToString("&")
            return if (params.isEmpty()) base else "$base?$params"
        }
    }

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