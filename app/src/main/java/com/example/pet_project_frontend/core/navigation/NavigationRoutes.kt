package com.example.pet_project_frontend.core.navigation

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

// 기존 코드와의 호환성을 위한 object
object NavigationRoutes {
    const val LOGIN = "login"
    const val PET_REGISTRATION = "pet_registration"
    const val PET_CARE = "petcare"
    const val MAP = "map"
    const val COMMUNITY = "community"
    const val TRANSLATOR = "translator"
    const val MY_PAGE = "mypage"
    
    // route 프로퍼티 추가
    val Login = Route(LOGIN)
    val PetRegistration = Route(PET_REGISTRATION)
    val PetCare = Route(PET_CARE)
    val Map = Route(MAP)
    val Community = Route(COMMUNITY)
    val Translator = Route(TRANSLATOR)
    val MyPage = Route(MY_PAGE)
}

data class Route(val route: String)