// app/src/main/java/com/example/pet_project_frontend/core/di/DatabaseModule.kt

package com.example.pet_project_frontend.core.di

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.pet_project_frontend.data.local.database.PetCareDatabase
import com.example.pet_project_frontend.data.local.database.dao.PlaceDao
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
        val db = Room.databaseBuilder(
            context,
            PetCareDatabase::class.java,
            "pet_care_database"
        )
            .fallbackToDestructiveMigration()
            .build()

        // Seed places data once on first run (idempotent by count check)
        CoroutineScope(Dispatchers.IO).launch {
            initializePlacesData(context, db)
        }

        return db
    }

    private suspend fun initializePlacesData(context: Context, database: PetCareDatabase) {
        try {
            // Already seeded? Exit early.
            if (database.placeDao().getPlacesCount() > 0) return

            context.assets.open("places.csv").bufferedReader().useLines { seq ->
                // 헤더 1줄 제거 후 스트리밍 처리
                val entitiesSeq = seq.drop(1).mapNotNull { line ->
                    // CSV 안전 분리(따옴표 보호)
                    val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                    // 필요한 인덱스 0,3,4,5,11,12,14,16,17,19 → 최소 20개 필요
                    if (tokens.size < 20) return@mapNotNull null

                    try {
                        val name = tokens[0].trim().removeSurrounding("\"")
                        val category = tokens[3].trim().removeSurrounding("\"")
                        val shortAddress = tokens[4].trim().removeSurrounding("\"") + " " + tokens[5].trim().removeSurrounding("\"")
                        val lat = tokens[11].trim().removeSurrounding("\"").toDoubleOrNull()
                        val lon = tokens[12].trim().removeSurrounding("\"").toDoubleOrNull()
                        val address = tokens[14].trim().removeSurrounding("\"")
                        val phoneNumber = tokens[16].trim().removeSurrounding("\"")
                        val homePage = tokens[17].trim().removeSurrounding("\"")
                        val operateTime = tokens[19].trim().removeSurrounding("\"")

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
                    } catch (_: Exception) {
                        null
                    }
                }

                // 대용량 배치 삽입으로 메모리/트랜잭션 부담 완화
                val dao = database.placeDao()
                entitiesSeq.chunked(1000).forEach { batch ->
                    if (batch.isNotEmpty()) dao.insertPlaces(batch)
                }
            }
        } catch (_: Exception) {
            // 실패 시 조용히 무시(맵 화면은 빈 상태로 동작)
        }
    }

    // ===== 맵 기능용 DAO - 유지 =====
    @Provides
    fun providePlaceDao(database: PetCareDatabase): PlaceDao {
        return database.placeDao()
    }
}