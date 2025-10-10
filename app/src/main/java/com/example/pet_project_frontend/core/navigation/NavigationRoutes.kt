package com.example.pet_project_frontend.core.navigation

import android.net.Uri

// 앱 내에서 사용하는 주요 경로를 정의한다.
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object PetRegistration : Screen("pet_registration")
    object PetCare : Screen("petcare")
    object Map : Screen("map")
    object Community : Screen("community")
    object Translator : Screen("translator")
    object MyPage : Screen("mypage")

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
