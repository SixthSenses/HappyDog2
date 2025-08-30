package com.example.pet_project_frontend.data.local.database.dao

import androidx.room.*
import com.example.pet_project_frontend.data.local.database.entities.PetCareSettingsEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface BreedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreed(breed: BreedEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreeds(breeds: List<BreedEntity>)
    
    @Query("SELECT * FROM breeds ORDER BY breedName ASC")
    fun getAllBreeds(): Flow<List<BreedEntity>>
    
    @Query("SELECT * FROM breeds WHERE breedName LIKE '%' || :query || '%' ORDER BY breedName ASC")
    suspend fun searchBreeds(query: String): List<BreedEntity>
    
    @Query("SELECT * FROM breeds WHERE breedName = :breedName")
    suspend fun getBreedByName(breedName: String): BreedEntity?
    
    @Query("DELETE FROM breeds WHERE cachedAt < :expirationTime")
    suspend fun deleteExpiredBreeds(expirationTime: LocalDateTime)
    
    @Query("SELECT COUNT(*) FROM breeds")
    suspend fun getBreedsCount(): Int
}