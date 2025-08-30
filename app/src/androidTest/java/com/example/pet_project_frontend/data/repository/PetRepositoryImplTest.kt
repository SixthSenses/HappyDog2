package com.example.pet_project_frontend.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pet_project_frontend.data.local.database.PetCareDatabase
import com.example.pet_project_frontend.data.local.database.dao.PetDao
import com.example.pet_project_frontend.data.remote.api.PetApi
import com.example.pet_project_frontend.data.remote.dto.request.PetRegistrationRequest
import com.example.pet_project_frontend.data.remote.dto.response.PetProfileResponse
import com.example.pet_project_frontend.domain.repository.PetRepository
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PetRepositoryImplTest {
    
    private lateinit var petRepository: PetRepository
    private lateinit var mockWebServer: MockWebServer
    private lateinit var petApi: PetApi
    private lateinit var petDao: PetDao
    private lateinit var database: PetCareDatabase
    
    @Before
    fun setup() {
        // MockWebServer 설정
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        petApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PetApi::class.java)
        
        // Room 데이터베이스 설정
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, PetCareDatabase::class.java
        ).build()
        petDao = database.petDao()
        
        petRepository = PetRepositoryImpl(petApi, petDao)
    }
    
    @After
    @Throws(IOException::class)
    fun tearDown() {
        mockWebServer.shutdown()
        database.close()
    }
    
    @Test
    fun registerPet_성공_시_로컬DB에_저장() = runTest {
        // Given
        val request = PetRegistrationRequest(
            name = "레오",
            breed = "골든 리트리버",
            birthDate = "2020-09-21",
            gender = "수컷",
            weight = 25.5
        )
        
        val expectedResponse = PetProfileResponse(
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
        
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "id": "test_pet_id",
                    "name": "레오",
                    "breed": "골든 리트리버",
                    "birthDate": "2020-09-21",
                    "gender": "수컷",
                    "weight": 25.5,
                    "ownerId": "test_owner_id",
                    "createdAt": "2024-01-01T00:00:00Z",
                    "updatedAt": "2024-01-01T00:00:00Z"
                }
            """.trimIndent())
        
        mockWebServer.enqueue(mockResponse)
        
        // When
        val result = petRepository.registerPet(request)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        
        // 로컬 DB에 저장되었는지 확인
        val savedPet = petDao.getPetById("test_pet_id")
        assertEquals(expectedResponse.id, savedPet?.id)
        assertEquals(expectedResponse.name, savedPet?.name)
    }
    
    @Test
    fun getPetProfile_로컬DB에_있으면_API_호출_안함() = runTest {
        // Given
        val petEntity = com.example.pet_project_frontend.data.local.database.entities.PetEntity(
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
        petDao.insertPet(petEntity)
        
        // When
        val result = petRepository.getPetProfile("test_pet_id")
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("레오", result.getOrNull()?.name)
        
        // API 호출이 없었는지 확인 (MockWebServer에 요청이 없어야 함)
        assertEquals(0, mockWebServer.requestCount)
    }
}
