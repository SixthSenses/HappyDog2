// package com.example.pet_project_frontend.data.local.database.dao

// import androidx.room.*
// import com.example.pet_project_frontend.data.local.database.entities.HealthRecordEntity
// import kotlinx.coroutines.flow.Flow

// @Dao
// interface HealthRecordDao {
    
//     @Insert(onConflict = OnConflictStrategy.REPLACE)
//     suspend fun insertHealthRecord(healthRecord: HealthRecordEntity)
    
//     @Query("SELECT * FROM health_records WHERE id = :recordId")
//     suspend fun getHealthRecordById(recordId: String): HealthRecordEntity?
    
//     @Query("SELECT * FROM health_records WHERE petId = :petId ORDER BY date DESC")
//     fun getHealthRecordsByPetId(petId: String): Flow<List<HealthRecordEntity>>
    
//     @Query("SELECT * FROM health_records WHERE petId = :petId AND recordType = :recordType ORDER BY date DESC")
//     fun getHealthRecordsByPetIdAndType(petId: String, recordType: String): Flow<List<HealthRecordEntity>>
    
//     @Query("SELECT * FROM health_records WHERE petId = :petId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
//     fun getHealthRecordsByDateRange(petId: String, startDate: String, endDate: String): Flow<List<HealthRecordEntity>>
    
//     @Update
//     suspend fun updateHealthRecord(healthRecord: HealthRecordEntity)
    
//     @Delete
//     suspend fun deleteHealthRecord(healthRecord: HealthRecordEntity)
    
//     @Query("DELETE FROM health_records WHERE id = :recordId")
//     suspend fun deleteHealthRecordById(recordId: String)
// }