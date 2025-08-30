package com.example.pet_project_frontend.data.local.database.dao

import androidx.room.*
import com.example.pet_project_frontend.data.local.database.entities.PetCareSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CareRecordEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<CareRecordEntity>)
    
    @Query("""
        SELECT * FROM care_records 
        WHERE petId = :petId 
        AND (:recordType IS NULL OR recordType = :recordType)
        AND (:date IS NULL OR searchDate = :date)
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getRecords(
        petId: String,
        recordType: String? = null,
        date: String? = null,
        limit: Int = 100
    ): List<CareRecordEntity>
    
    @Query("""
        SELECT * FROM care_records 
        WHERE petId = :petId 
        AND searchDate BETWEEN :startDate AND :endDate
        ORDER BY timestamp DESC
    """)
    fun getRecordsByDateRange(
        petId: String,
        startDate: String,
        endDate: String
    ): Flow<List<CareRecordEntity>>
    
    @Query("""
        SELECT * FROM care_records 
        WHERE petId = :petId 
        AND recordType IN (:recordTypes)
        ORDER BY timestamp DESC
    """)
    fun getRecordsByTypes(
        petId: String,
        recordTypes: List<String>
    ): Flow<List<CareRecordEntity>>
    
    @Query("SELECT * FROM care_records WHERE logId = :logId")
    suspend fun getRecordById(logId: String): CareRecordEntity?
    
    @Delete
    suspend fun deleteRecord(record: CareRecordEntity)
    
    @Query("DELETE FROM care_records WHERE logId = :logId")
    suspend fun deleteRecordById(logId: String)
    
    @Query("DELETE FROM care_records WHERE petId = :petId")
    suspend fun deleteAllRecordsForPet(petId: String)
}