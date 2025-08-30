package com.example.pet_project_frontend.data.local.database.dao

import androidx.room.*
import com.example.pet_project_frontend.data.local.database.entities.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<PlaceEntity>)
    
    @Query("SELECT * FROM places WHERE category IN (:categories) AND latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    fun getPlacesInBounds(
        categories: List<String>,
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): Flow<List<PlaceEntity>>
    
    @Query("SELECT * FROM places WHERE category IN (:categories)")
    fun getPlacesByCategories(categories: List<String>): Flow<List<PlaceEntity>>
    
    @Query("SELECT * FROM places")
    fun getAllPlaces(): Flow<List<PlaceEntity>>
    
    @Query("DELETE FROM places")
    suspend fun deleteAllPlaces()
    
    @Query("SELECT COUNT(*) FROM places")
    suspend fun getPlacesCount(): Int
}
