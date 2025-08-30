package com.example.pet_project_frontend.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.pet_project_frontend.data.local.database.PetCareDatabase
import com.example.pet_project_frontend.data.local.database.dao.HealthRecordDao
import com.example.pet_project_frontend.data.local.database.dao.PetDao
import com.example.pet_project_frontend.data.local.database.dao.PlaceDao
import com.example.pet_project_frontend.data.local.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun providePetCareDatabase(
        @ApplicationContext context: Context
    ): PetCareDatabase {
        return Room.databaseBuilder(
            context,
            PetCareDatabase::class.java,
            "pet_care_database"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    initializePlacesData(context, db)
                }
            }
        }).build()
    }
    
    private suspend fun initializePlacesData(context: Context, db: androidx.sqlite.db.SupportSQLiteDatabase) {
        try {
            val inputStream = context.assets.open("places.csv")
            val reader = inputStream.bufferedReader()
            val lines = reader.readLines().drop(1) // 헤더 제거
            
            val placeEntities = lines.mapNotNull { line ->
                val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                if (tokens.size < 14) return@mapNotNull null
                
                try {
                    val name = tokens[0].trim().removeSurrounding("\"")
                    val address = tokens[14].trim().removeSurrounding("\"")
                    val lat = tokens[11].trim().removeSurrounding("\"").toDoubleOrNull()
                    val lon = tokens[12].trim().removeSurrounding("\"").toDoubleOrNull()
                    val shortAddress = tokens[4].trim().removeSurrounding("\"") + " " + tokens[5].trim().removeSurrounding("\"")
                    val phoneNumber = tokens[16].trim().removeSurrounding("\"")
                    val operateTime = tokens[19].trim().removeSurrounding("\"")
                    val homePage = tokens[17].trim().removeSurrounding("\"")
                    val category = tokens[3].trim().removeSurrounding("\"")
                    
                    if (lat == null || lon == null || address.isEmpty()) return@mapNotNull null
                    
                    com.example.pet_project_frontend.data.local.database.entities.PlaceEntity(
                        id = "${lat}_${lon}_${name.hashCode()}",
                        name = name,
                        latitude = lat,
                        longitude = lon,
                        category = category,
                        address = address,
                        shortAddress = shortAddress,
                        phoneNumber = if (phoneNumber.isNotEmpty()) phoneNumber else "정보 없음",
                        operateTime = operateTime,
                        homePage = homePage
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            // 데이터베이스에 삽입
            val placeDao = providePetCareDatabase(context).placeDao()
            placeDao.insertPlaces(placeEntities)
            
        } catch (e: Exception) {
            // 에러 처리
        }
    }
    
    @Provides
    fun providePetDao(database: PetCareDatabase): PetDao {
        return database.petDao()
    }
    
    @Provides
    fun provideUserDao(database: PetCareDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideHealthRecordDao(database: PetCareDatabase): HealthRecordDao {
        return database.healthRecordDao()
    }
    
    @Provides
    fun providePlaceDao(database: PetCareDatabase): PlaceDao {
        return database.placeDao()
    }
}