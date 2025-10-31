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
    version = 4, // 전화번호 등 정보 없음 처리 추가로 버전 상승
    exportSchema = false
)
@TypeConverters(DateConverters::class, ListConverters::class)
abstract class PetCareDatabase : RoomDatabase() {

    abstract fun placeDao(): PlaceDao
}