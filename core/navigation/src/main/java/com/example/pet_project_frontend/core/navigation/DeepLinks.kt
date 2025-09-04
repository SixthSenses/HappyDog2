package com.example.pet_project_frontend.core.navigation

object DeepLinks {
    const val PET_CARE_DASHBOARD = "app://pet-care/dashboard"
}

object Routes {
    object PetCare {
        const val Dashboard = "pet_care/dashboard?petId={petId}&date={date}&tab={tab}"
    }
}
