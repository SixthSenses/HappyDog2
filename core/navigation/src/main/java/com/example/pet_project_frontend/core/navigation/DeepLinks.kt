package com.example.pet_project_frontend.core.navigation

object DeepLinks {
    const val PET_CARE_DASHBOARD = "app://pet-care/dashboard"
    const val PET_CARE_BCS_DETAILS = "app://pet-care/bcs-details"
    const val PET_CARE_CHARTS = "app://pet-care/charts"
    const val PET_CARE_SETTINGS = "app://pet-care/settings"
}

object Routes {
    object PetCare {
        const val Dashboard = "pet_care/dashboard?petId={petId}&date={date}&tab={tab}"
        const val BcsDetails = "pet_care/bcs-details?petId={petId}&selected={selected}"
        const val Charts = "pet_care/charts?type={type}&start={start}&end={end}"
        const val Settings = "pet_care/settings"
    }
}
