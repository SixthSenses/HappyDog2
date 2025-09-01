// app/src/main/java/com/example/pet_project_frontend/data/local/database/dao/HealthRecordDao.kt

package com.example.pet_project_frontend.data.local.database.dao

import androidx.room.*
import com.example.pet_project_frontend.data.local.database.entities.HealthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: HealthRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecords(records: List<HealthRecordEntity>)

    @Query("SELECT * FROM health_records WHERE petId = :petId ORDER BY date DESC")
    fun getHealthRecordsByPetId(petId: String): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE petId = :petId AND recordType = :recordType ORDER BY date DESC")
    fun getHealthRecordsByType(petId: String, recordType: String): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE petId = :petId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getHealthRecordsByDateRange(
        petId: String,
        startDate: String,
        endDate: String
    ): List<HealthRecordEntity>

    @Query("SELECT * FROM health_records WHERE id = :recordId")
    suspend fun getHealthRecordById(recordId: String): HealthRecordEntity?

    @Update
    suspend fun updateHealthRecord(record: HealthRecordEntity)

    @Delete
    suspend fun deleteHealthRecord(record: HealthRecordEntity)

    @Query("DELETE FROM health_records WHERE petId = :petId")
    suspend fun deleteAllHealthRecordsByPetId(petId: String)

    @Query("DELETE FROM health_records WHERE id = :recordId")
    suspend fun deleteHealthRecordById(recordId: String)
}