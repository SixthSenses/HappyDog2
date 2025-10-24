package com.example.pet_project_frontend.core.navigation

import android.net.Uri

// 앱 내에서 사용하는 주요 경로를 정의한다.
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object PetRegistration : Screen("pet_registration")
    object PetCare : Screen("petcare")
    object PetCareHome : Screen("petcare_home")
    object Map : Screen("map")
    object Community : Screen("community")
    object Translator : Screen("translator")
    object MyPage : Screen("mypage")
    object EyeHealth : Screen("eye_health") // AI 안구 검사 화면
    object EyeHealthHistory : Screen("eye_health_history") // 안구 검사 기록 화면
    object ImageViewer : Screen("image_viewer/{imageUrl}") {
        fun createRoute(imageUrl: String) = "image_viewer/${imageUrl}"
    }
    object HealthSurvey : Screen("health_survey") // 건강 설문지 화면
    object BreedGuide : Screen("breed_guide") // 견종 가이드북 리스트 화면
    object BreedGuidebook : Screen("breed_guidebook/{breedName}") { // 견종 가이드북 상세 화면
        fun createRoute(breedName: String) = "breed_guidebook/$breedName"
    }
    
    // 케어 관리 화면들 (중간 단계)
    object FeedManagement : Screen("feed_management?date={date}") { // 사료 관리 화면
        fun createRoute(date: String? = null) = if (date != null) "feed_management?date=$date" else "feed_management"
    }
    object ActivityManagement : Screen("activity_management?date={date}") { // 활동 관리 화면
        fun createRoute(date: String? = null) = if (date != null) "activity_management?date=$date" else "activity_management"
    }
    object WeightManagement : Screen("weight_management?date={date}") { // 몸무게 관리 화면
        fun createRoute(date: String): String {
            return "weight_management?date=$date"
        }
    }
    object PoopManagement : Screen("poop_management?date={date}") { // 대변 관리 화면
        fun createRoute(date: String? = null) = if (date != null) "poop_management?date=$date" else "poop_management"
    }
    object VomitManagement : Screen("vomit_management?date={date}") { // 구토 관리 화면
        fun createRoute(date: String? = null) = if (date != null) "vomit_management?date=$date" else "vomit_management"
    }
    
    // 케어 기록 화면들 (실제 입력 화면)
    object FeedRecord : Screen("feed_record") // 사료 기록 화면
    object ActivityRecord : Screen("activity_record") // 활동 기록 화면
    object WeightRecord : Screen("weight_record") // 몸무게 목표 설정 화면
    object WeightLog : Screen("weight_log") // 몸무게 기록 화면 (날짜별)
    object PoopRecord : Screen("poop_record") // 대변 기록 화면
    object VomitRecord : Screen("vomit_record") // 구토 기록 화면

    object EditPetName : Screen("mypage/edit/name?initialName={initialName}&petId={petId}") {
        fun createRoute(initialName: String, petId: String?): String {
            val encodedName = Uri.encode(initialName)
            val encodedPetId = Uri.encode(petId ?: "")
            return "mypage/edit/name?initialName=$encodedName&petId=$encodedPetId"
        }
    }

    object EditBirthDate : Screen("mypage/edit/birth?initialBirth={initialBirth}") {
        fun createRoute(initialBirth: String): String {
            val encoded = Uri.encode(initialBirth)
            return "mypage/edit/birth?initialBirth=$encoded"
        }
    }

    object SelectGender : Screen("mypage/edit/gender?initialGender={initialGender}") {
        fun createRoute(initialGender: String): String {
            val encoded = Uri.encode(initialGender)
            return "mypage/edit/gender?initialGender=$encoded"
        }
    }

    object SelectBreed : Screen("mypage/edit/breed?initialBreed={initialBreed}") {
        fun createRoute(initialBreed: String): String {
            val encoded = Uri.encode(initialBreed)
            return "mypage/edit/breed?initialBreed=$encoded"
        }
    }

    object NotificationSettings : Screen("mypage/settings/notification")

    object Withdraw : Screen("mypage/withdraw")

    object VerificationIntro : Screen("mypage/verification/intro")
    object VerificationGuide : Screen("mypage/verification/guide")
    object VerificationLoading :
        Screen("mypage/verification/loading?petId={petId}") {
        fun createRoute(petId: String): String =
            "mypage/verification/loading?petId=$petId"
    }

    object VerificationSuccess : Screen("mypage/verification/success")

    object PetCareDashboard : Screen(Routes.PetCare.Dashboard) {
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
