package com.example.pet_project_frontend.data.local.database

import androidx.room.*
import com.example.pet_project_frontend.data.local.database.converter.DateConverters
import com.example.pet_project_frontend.data.local.database.converter.ListConverters
import com.example.pet_project_frontend.data.local.database.dao.*
import com.example.pet_project_frontend.data.local.database.entities.*

@Database(
    entities = [
        UserEntity::class,
        PetEntity::class,
        PetCareSettingsEntity::class,
        CareRecordEntity::class,
        PlaceEntity::class,
        BreedEntity::class,
        HealthRecordEntity::class // <-- 1. 이 줄을 추가해주세요!
    ],
    version = 2, //
    exportSchema = false
)
@TypeConverters(DateConverters::class, ListConverters::class)
abstract class PetCareDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao
    abstract fun petCareSettingsDao(): PetCareSettingsDao
    abstract fun careRecordDao(): CareRecordDao
    abstract fun placeDao(): PlaceDao
    abstract fun breedDao(): BreedDao
    abstract fun healthRecordDao(): HealthRecordDao //
}