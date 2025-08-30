package com.example.pet_project_frontend.data.local.database.dao

import androidx.room.*
import com.example.pet_project_frontend.data.local.database.entities.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity)
    
    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getPetById(petId: String): PetEntity?
    
    @Query("SELECT * FROM pets WHERE ownerId = :ownerId")
    fun getPetsByOwnerId(ownerId: String): Flow<List<PetEntity>>
    
    @Query("SELECT * FROM pets")
    fun getAllPets(): Flow<List<PetEntity>>
    
    @Update
    suspend fun updatePet(pet: PetEntity)
    
    @Delete
    suspend fun deletePet(pet: PetEntity)
    
    @Query("DELETE FROM pets WHERE id = :petId")
    suspend fun deletePetById(petId: String)
}