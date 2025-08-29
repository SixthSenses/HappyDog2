package com.example.pet_project_frontend.presentation.map

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

data class MapPlace(
	val name: String,
	val latitude: Double,
	val longitude: Double,
	val category: DetailedPlaceCategory,
	val address: String,
	val shortAddress: String,
	val phoneNumber: String,
	val operateTime: String,
	val homePage: String,
)

enum class PlaceCategory(val displayName: String) {
	HOSPITAL("반려의료"),
	PET_FACILITY("반려동반")
}
enum class DetailedPlaceCategory(
	val csvName: String, // CSV 파일에 있는 카테고리 이름
	val parentCategory: PlaceCategory, // 이 카테고리가 속한 대분류
	val markerResId: Int, // 지도에 표시될 마커 리소스
	val icon: Int // 목록에 표시될 아이콘
) {
	// --- 반려의료 ---
	ANIMAL_HOSPITAL("동물병원", PlaceCategory.HOSPITAL, R.drawable.hospital, R.drawable.hospital),
	ANIMAL_PHARMACY("동물약국", PlaceCategory.HOSPITAL, R.drawable.my_location_marker, R.drawable.my_location_marker),

	// --- 반려동물 동반가능 ---
	ART_MUSEUM("미술관", PlaceCategory.PET_FACILITY, R.drawable.my_location_marker, R.drawable.my_location_marker),
	CAFE("카페", PlaceCategory.PET_FACILITY, R.drawable.cafe, R.drawable.cafe),
	PET_SUPPLIES("반려동물용품", PlaceCategory.PET_FACILITY, R.drawable.my_location_marker, R.drawable.my_location_marker),
	GROOMING("미용", PlaceCategory.PET_FACILITY, R.drawable.my_location_marker, R.drawable.my_location_marker),
	CULTURE_CENTER("문예회관", PlaceCategory.PET_FACILITY, R.drawable.my_location_marker, R.drawable.my_location_marker),
	PENSION("펜션", PlaceCategory.PET_FACILITY, R.drawable.pension, R.drawable.pension),
	RESTAURANT("식당", PlaceCategory.PET_FACILITY, R.drawable.restaurant, R.drawable.restaurant),
	TOURIST_SPOT("여행지", PlaceCategory.PET_FACILITY, R.drawable.travel, R.drawable.travel),
	PET_SITTING("위탁관리", PlaceCategory.PET_FACILITY, R.drawable.my_location_marker, R.drawable.my_location_marker),
	MUSEUM("박물관", PlaceCategory.PET_FACILITY, R.drawable.my_location_marker, R.drawable.my_location_marker),

	// CSV의 카테고리 이름과 일치하지 않을 경우를 대비한 기본값 -> 임의로 현재 위치 마커와 동일한 이미지 사용
	UNKNOWN("기타", PlaceCategory.PET_FACILITY, R.drawable.my_location_marker,R.drawable.my_location_marker);

	companion object {
		// CSV 이름(String)으로 해당하는 Enum 값을 찾기 위한 함수
		fun fromCsvName(csvName: String): DetailedPlaceCategory {
			return values().find { it.csvName == csvName.trim() } ?: DetailedPlaceCategory.UNKNOWN
		}
	}
}
data class MapUiState(
	val selectedCategory: PlaceCategory = PlaceCategory.HOSPITAL,
	val places: List<MapPlace> = emptyList(),
	val currentLocation: Location? = null,
	val isLoading: Boolean = true // 초기 상태는 항상 로딩중
)

@HiltViewModel
class MapViewModel @Inject constructor(
	private val application: Application
) : ViewModel() {

	private val _uiState = MutableStateFlow(MapUiState())
	val uiState = _uiState.asStateFlow()

	private var allPlaces: List<MapPlace> = emptyList()

	init {
		loadPlacesFromCsv()
	}

	fun updateCurrentLocation(location: Location) {
		_uiState.update { it.copy(currentLocation = location) }
		// 위치 정보가 업데이트되면 필터링을 다시 시도
		filterAndFetchPlaces()
	}

	fun selectCategory(category: PlaceCategory) {
		_uiState.update { it.copy(selectedCategory = category) }
		filterAndFetchPlaces()
	}

	private fun loadPlacesFromCsv() {
		viewModelScope.launch {
			try {
				val placesList = withContext(Dispatchers.IO) {
					val inputStream = application.assets.open("places.csv")
					val reader = BufferedReader(InputStreamReader(inputStream, "euc-kr"))
					reader.readLines().drop(1)
						.mapNotNull { line ->
							val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
							if (tokens.size < 14) return@mapNotNull null

							try {
								val name = tokens[0].trim().removeSurrounding("\"")
								val address = tokens[14].trim().removeSurrounding("\"")
								val lat = tokens[11].trim().removeSurrounding("\"").toDoubleOrNull()
								val lon = tokens[12].trim().removeSurrounding("\"").toDoubleOrNull()
								val shortAddress = tokens[4].trim().removeSurrounding("\"") + " " + tokens[5].trim().removeSurrounding("\"")
								val phoneNumber = tokens[16].trim().removeSurrounding("\"")
								val operateTime = tokens[19].trim().removeSurrounding("\"")
								val homePage = tokens[17].trim().removeSurrounding("\"")
								val detailedCategoryStr = tokens[3].trim().removeSurrounding("\"")

								Log.d("CSV_DEBUG", "CSV에서 읽은 카테고리: '(${detailedCategoryStr})'")
								if (lat == null || lon == null || address.isEmpty()) return@mapNotNull null

								val category = DetailedPlaceCategory.fromCsvName(detailedCategoryStr)

								MapPlace(
									name = name,
									latitude = lat,
									longitude = lon,
									category = category,
									address = address,
									shortAddress = shortAddress,
									phoneNumber = if (phoneNumber.isNotEmpty()) phoneNumber else "정보 없음",
									operateTime = operateTime,
									homePage = homePage,
								)
							} catch (e: Exception) {
								Log.e("CsvParsingError", "Error parsing line: $line", e)
								null
							}
						}
				}
				allPlaces = placesList
				// CSV 로딩이 완료되면 필터링을 다시 시도
				filterAndFetchPlaces()
			} catch (e: Exception) {
				Log.e("CsvLoadingError", "Failed to load CSV file", e)
				_uiState.update { it.copy(isLoading = false) }
			}
		}
	}

	private fun filterAndFetchPlaces() {
		val currentState = _uiState.value
		val currentLocation = currentState.currentLocation

		if (currentLocation == null || allPlaces.isEmpty()) {
			return
		}

		val filteredPlaces = allPlaces.filter { place ->
			val isCategoryMatch = place.category.parentCategory == currentState.selectedCategory
			val placeLocation = Location("place").apply {
				latitude = place.latitude
				longitude = place.longitude
			}
			val distanceInMeters = currentLocation.distanceTo(placeLocation)
			// 주변 2km 이내의 장소만 필터링
			val isWithinDistance = distanceInMeters <= 2000
			isCategoryMatch && isWithinDistance
		}
			.sortedBy { place ->
				val placeLocation = Location("place").apply {
					latitude = place.latitude
					longitude = place.longitude
				}
				currentLocation.distanceTo(placeLocation)
			}

		_uiState.update { it.copy(places = filteredPlaces, isLoading = false) }
	}
}