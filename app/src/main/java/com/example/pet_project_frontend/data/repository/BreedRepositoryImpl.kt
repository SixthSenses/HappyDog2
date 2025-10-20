package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.remote.api.BreedApi
import com.example.pet_project_frontend.domain.model.Breed
import com.example.pet_project_frontend.domain.repository.BreedRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreedRepositoryImpl @Inject constructor(
    private val breedApi: BreedApi
) : BreedRepository {
    
    companion object {
        private const val TAG = "BreedRepositoryImpl"
    }
    
    override suspend fun getAllBreeds(): AppResult<List<Breed>> {
        Log.d(TAG, "Fetching all breeds")
        return SafeApi.response { breedApi.getBreedList() }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        val breeds = res.data.breeds.map { dto ->
                            Breed(
                                breedName = dto.breedName,
                                lifeExpectancy = dto.lifeExpectancy,
                                heightMin = dto.heightCm.min,
                                heightMax = dto.heightCm.max,
                                weightMin = dto.weightKg.min,
                                weightMax = dto.weightKg.max
                            )
                        }
                        Log.d(TAG, "Successfully fetched ${breeds.size} breeds")
                        AppResult.Success(breeds)
                    }
                    is AppResult.Error -> {
                        Log.e(TAG, "Failed to fetch breeds: ${res.message}")
                        res
                    }
                    is AppResult.Exception -> {
                        Log.e(TAG, "Exception while fetching breeds", res.throwable)
                        res
                    }
                }
            }
    }
    
    override suspend fun searchBreeds(query: String): AppResult<List<Breed>> {
        Log.d(TAG, "Searching breeds with query: $query")
        return SafeApi.response { breedApi.searchBreeds(query) }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        val breeds = res.data.breeds.map { dto ->
                            Breed(
                                breedName = dto.breedName,
                                lifeExpectancy = dto.lifeExpectancy,
                                heightMin = dto.heightCm.min,
                                heightMax = dto.heightCm.max,
                                weightMin = dto.weightKg.min,
                                weightMax = dto.weightKg.max
                            )
                        }
                        Log.d(TAG, "Found ${breeds.size} breeds matching '$query'")
                        AppResult.Success(breeds)
                    }
                    is AppResult.Error -> {
                        Log.e(TAG, "Failed to search breeds: ${res.message}")
                        res
                    }
                    is AppResult.Exception -> {
                        Log.e(TAG, "Exception while searching breeds", res.throwable)
                        res
                    }
                }
            }
    }
}
