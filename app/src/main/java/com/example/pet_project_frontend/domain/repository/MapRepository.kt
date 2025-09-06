package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.presentation.map.MapPlace
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    fun getPlacesInBounds(
        categories: List<String>,
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): Flow<List<MapPlace>>
    
    fun getPlacesByCategories(categories: List<String>): Flow<List<MapPlace>>
    
    suspend fun getPlacesCount(): Int
}