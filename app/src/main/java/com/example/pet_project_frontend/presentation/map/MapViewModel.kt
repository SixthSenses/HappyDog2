package com.example.pet_project_frontend.presentation.map

import android.location.Location
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
import com.example.pet_project_frontend.domain.repository.MapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
	private val mapRepository: MapRepository
) : ViewModel() {

	private val _uiState = MutableStateFlow(MapUiState())
	val uiState = _uiState.asStateFlow()

	init {
		loadPlacesFromDatabase()
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

	private fun loadPlacesFromDatabase() {
		viewModelScope.launch {
			try {
				// 데이터베이스에서 장소 데이터 로드
				val categories = getCategoriesForSelectedCategory(_uiState.value.selectedCategory)
				mapRepository.getPlacesByCategories(categories).collect { places ->
					_uiState.update { it.copy(places = places, isLoading = false) }
				}
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false) }
			}
		}
	}

	private fun filterAndFetchPlaces() {
		val currentState = _uiState.value
		val currentLocation = currentState.currentLocation

		if (currentLocation == null) {
			return
		}

		viewModelScope.launch {
			try {
				val categories = getCategoriesForSelectedCategory(currentState.selectedCategory)
				
				// 현재 위치 기준으로 2km 반경 내의 장소들만 필터링
				val bounds = calculateBounds(currentLocation, 2000.0) // 2km
				
				mapRepository.getPlacesInBounds(
					categories = categories,
					minLat = bounds[0],
					maxLat = bounds[1],
					minLng = bounds[2],
					maxLng = bounds[3]
				).collect { places ->
					// 거리순으로 정렬
					val sortedPlaces = places.sortedBy { place ->
						val placeLocation = Location("place").apply {
							latitude = place.latitude
							longitude = place.longitude
						}
						currentLocation.distanceTo(placeLocation)
					}
					
					_uiState.update { it.copy(places = sortedPlaces, isLoading = false) }
				}
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false) }
			}
		}
	}
	
	private fun getCategoriesForSelectedCategory(category: PlaceCategory): List<String> {
		return when (category) {
			PlaceCategory.HOSPITAL -> listOf("동물병원", "동물약국")
			PlaceCategory.PET_FACILITY -> listOf("미술관", "카페", "반려동물용품", "미용", "문예회관", "펜션", "식당", "여행지", "위탁관리", "박물관")
		}
	}
	
	private fun calculateBounds(location: Location, radiusInMeters: Double): List<Double> {
		// 위도 1도 = 약 111km, 경도 1도 = 약 88.9km (한반도 기준)
		val latDelta = radiusInMeters / 111000.0
		val lngDelta = radiusInMeters / (88900.0 * Math.cos(Math.toRadians(location.latitude)))
		
		return listOf(
			location.latitude - latDelta, // minLat
			location.latitude + latDelta, // maxLat
			location.longitude - lngDelta, // minLng
			location.longitude + lngDelta  // maxLng
		)
	}
}