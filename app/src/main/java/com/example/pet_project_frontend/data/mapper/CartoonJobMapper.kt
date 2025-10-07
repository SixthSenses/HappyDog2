package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.response.CartoonJobHealthResponse
import com.example.pet_project_frontend.data.remote.dto.response.CartoonJobResponse
import com.example.pet_project_frontend.domain.model.CartoonJob
import com.example.pet_project_frontend.domain.model.CartoonJobHealth
import com.example.pet_project_frontend.domain.model.CartoonJobStatus
import java.time.LocalDateTime
import java.time.ZonedDateTime

/**
 * CartoonJob DTO → Domain Model 변환
 */
object CartoonJobMapper {
    
    fun toDomain(response: CartoonJobResponse): CartoonJob {
        return CartoonJob(
            jobId = response.jobId,
            userId = response.userId,
            status = CartoonJobStatus.fromString(response.status),
            originalImageUrl = response.originalImageUrl,
            resultImageUrl = response.resultImageUrl,
            errorMessage = response.errorMessage,
            createdAt = parseDateTime(response.createdAt),
            updatedAt = response.updatedAt?.let { parseDateTime(it) }
        )
    }
    
    fun toDomain(response: CartoonJobHealthResponse): CartoonJobHealth {
        return CartoonJobHealth(
            activeJobs = response.activeJobs,
            queueSize = response.queueSize,
            maxWorkers = response.maxWorkers,
            integrationHealth = response.integrationHealth
        )
    }
    
    private fun parseDateTime(dateTimeStr: String): LocalDateTime {
        return try {
            ZonedDateTime.parse(dateTimeStr).toLocalDateTime()
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}
