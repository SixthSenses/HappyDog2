package com.example.pet_project_frontend.data.repository

import com.example.pet_project_frontend.data.local.database.dao.PlaceDao
import com.example.pet_project_frontend.data.local.database.entities.PlaceEntity
import com.example.pet_project_frontend.domain.repository.MapRepository
import com.example.pet_project_frontend.presentation.map.DetailedPlaceCategory
import com.example.pet_project_frontend.presentation.map.MapPlace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val placeDao: PlaceDao
) : MapRepository {
    
    override fun getPlacesInBounds(
        categories: List<String>,
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): Flow<List<MapPlace>> {
        return placeDao.getPlacesInBounds(categories, minLat, maxLat, minLng, maxLng)
            .map { entities -> entities.map { it.toMapPlace() } }
    }
    
    override fun getPlacesByCategories(categories: List<String>): Flow<List<MapPlace>> {
        return placeDao.getPlacesByCategories(categories)
            .map { entities -> entities.map { it.toMapPlace() } }
    }
    
    override suspend fun getPlacesCount(): Int {
        return placeDao.getPlacesCount()
    }
    
    private fun PlaceEntity.toMapPlace(): MapPlace {
        return MapPlace(
            name = name,
            latitude = latitude,
            longitude = longitude,
            category = DetailedPlaceCategory.fromCsvName(category),
            address = address,
            shortAddress = shortAddress,
            phoneNumber = phoneNumber,
            operateTime = operateTime,
            homePage = homePage
        )
    }
}