package com.example.pet_project_frontend.data.local.database.dao

import androidx.room.*
import com.example.pet_project_frontend.data.local.database.entities.PetCareSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetCareSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: PetCareSettingsEntity)
    
    @Query("SELECT * FROM pet_care_settings WHERE petId = :petId")
    suspend fun getSettings(petId: String): PetCareSettingsEntity?
    
    @Query("SELECT * FROM pet_care_settings WHERE petId = :petId")
    fun getSettingsFlow(petId: String): Flow<PetCareSettingsEntity?>
    
    @Update
    suspend fun updateSettings(settings: PetCareSettingsEntity)
    
    @Query("DELETE FROM pet_care_settings WHERE petId = :petId")
    suspend fun deleteSettings(petId: String)
}