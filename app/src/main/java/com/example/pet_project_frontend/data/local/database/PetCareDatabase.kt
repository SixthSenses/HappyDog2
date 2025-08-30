package com.example.pet_project_frontend.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pet_project_frontend.data.local.database.dao.HealthRecordDao
import com.example.pet_project_frontend.data.local.database.dao.PetDao
import com.example.pet_project_frontend.data.local.database.dao.PlaceDao
import com.example.pet_project_frontend.data.local.database.dao.UserDao
import com.example.pet_project_frontend.data.local.database.entities.HealthRecordEntity
import com.example.pet_project_frontend.data.local.database.entities.PetEntity
import com.example.pet_project_frontend.data.local.database.entities.PlaceEntity
import com.example.pet_project_frontend.data.local.database.entities.UserEntity

@Database(
    entities = [
        PetEntity::class,
        UserEntity::class,
        HealthRecordEntity::class,
        PlaceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PetCareDatabase : RoomDatabase() {
    
    abstract fun petDao(): PetDao
    abstract fun userDao(): UserDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun placeDao(): PlaceDao
}