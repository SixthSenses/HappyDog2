package com.example.pet_project_frontend.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pet_project_frontend.data.local.database.converter.DateConverters
import com.example.pet_project_frontend.data.local.database.converter.ListConverters
import com.example.pet_project_frontend.data.local.database.dao.PlaceDao
import com.example.pet_project_frontend.data.local.database.entities.PlaceEntity

@Database(
    entities = [
        PlaceEntity::class
    ],
    version = 3, // Room 스키마 정리(Place만 유지)로 버전 상승
    exportSchema = false
)
@TypeConverters(DateConverters::class, ListConverters::class)
abstract class PetCareDatabase : RoomDatabase() {

    abstract fun placeDao(): PlaceDao
}