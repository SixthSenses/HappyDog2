package com.example.pet_project_frontend.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pet_project_frontend.data.local.database.dao.PetDao
import com.example.pet_project_frontend.data.local.database.entities.PetEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class PetDaoTest {
    
    private lateinit var petDao: PetDao
    private lateinit var database: PetCareDatabase
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, PetCareDatabase::class.java
        ).build()
        petDao = database.petDao()
    }
    
    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun insertAndReadPet() = runTest {
        // Given
        val pet = PetEntity(
            id = "test_pet_id",
            name = "레오",
            breed = "골든 리트리버",
            birthDate = "2020-09-21",
            gender = "수컷",
            weight = 25.5,
            ownerId = "test_owner_id",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        
        // When
        petDao.insertPet(pet)
        val retrievedPet = petDao.getPetById("test_pet_id")
        
        // Then
        assertEquals(pet, retrievedPet)
    }
    
    @Test
    fun updatePet() = runTest {
        // Given
        val pet = PetEntity(
            id = "test_pet_id",
            name = "레오",
            breed = "골든 리트리버",
            birthDate = "2020-09-21",
            gender = "수컷",
            weight = 25.5,
            ownerId = "test_owner_id",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        petDao.insertPet(pet)
        
        // When
        val updatedPet = pet.copy(name = "레오(수정됨)", weight = 26.0)
        petDao.updatePet(updatedPet)
        val retrievedPet = petDao.getPetById("test_pet_id")
        
        // Then
        assertEquals(updatedPet, retrievedPet)
    }
    
    @Test
    fun deletePet() = runTest {
        // Given
        val pet = PetEntity(
            id = "test_pet_id",
            name = "레오",
            breed = "골든 리트리버",
            birthDate = "2020-09-21",
            gender = "수컷",
            weight = 25.5,
            ownerId = "test_owner_id",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        petDao.insertPet(pet)
        
        // When
        petDao.deletePetById("test_pet_id")
        val retrievedPet = petDao.getPetById("test_pet_id")
        
        // Then
        assertNull(retrievedPet)
    }
}
