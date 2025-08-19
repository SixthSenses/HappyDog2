package com.example.pet_project_frontend.presentation.map

import android.app.Application
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

// 지도에 표시할 장소에 대한 데이터 클래스
data class MapPlace(
	val name: String,
	val latitude: Double,
	val longitude: Double,
	val category: PlaceCategory
)

// 장소 카테고리를 정의하는 Enum 클래스
enum class PlaceCategory(val displayName: String) {
	HOSPITAL("동물병원"),
	PET_FRIENDLY("반려동물 동반")
}

// 화면의 상태를 나타내는 데이터 클래스 (현재 위치 포함)
data class MapUiState(
	val selectedCategory: PlaceCategory = PlaceCategory.HOSPITAL,
	val places: List<MapPlace> = emptyList(),
	val currentLocation: Location? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
	private val application: Application // assets 폴더 접근을 위해 Application Context 주입
) : ViewModel() {

	private val _uiState = MutableStateFlow(MapUiState())
	val uiState = _uiState.asStateFlow()

	// CSV에서 읽어온 모든 장소 데이터를 저장하는 리스트
	private var allPlaces: List<MapPlace> = emptyList()

	init {
		// ViewModel이 처음 생성될 때, CSV 파일에서 모든 장소 데이터를 미리 읽어옵니다.
		loadPlacesFromCsv()
	}

	// MapScreen에서 사용자의 현재 위치를 받아서 UI 상태를 업데이트하는 함수
	fun updateCurrentLocation(location: Location) {
		_uiState.update { it.copy(currentLocation = location) }
		// 현재 위치가 업데이트되면, 그 위치를 기준으로 장소들을 다시 필터링합니다.
		filterAndFetchPlaces()
	}

	// MapScreen에서 카테고리 탭을 선택했을 때 호출될 함수
	fun selectCategory(category: PlaceCategory) {
		_uiState.update { it.copy(selectedCategory = category) }
		// 카테고리가 변경되면, 장소들을 다시 필터링합니다.
		filterAndFetchPlaces()
	}

	// assets 폴더의 CSV 파일을 읽어 allPlaces 리스트에 저장하는 함수
	private fun loadPlacesFromCsv() {
		viewModelScope.launch {
			try {
				val inputStream = application.assets.open("places.csv") // CSV 파일명
				val reader = BufferedReader(InputStreamReader(inputStream))

				// CSV 데이터를 한 줄씩 읽어 MapPlace 객체로 변환
				allPlaces = reader.readLines().drop(1) // 첫 줄(헤더)은 건너뜁니다.
					.mapNotNull { line ->
						val tokens = line.split(",")
						// CSV 포맷: 이름,위도,경도,카테고리(HOSPITAL 또는 PET_FRIENDLY)
						if (tokens.size < 4) return@mapNotNull null
						try {
							MapPlace(
								name = tokens[0].trim(),
								latitude = tokens[1].trim().toDouble(),
								longitude = tokens[2].trim().toDouble(),
								category = PlaceCategory.valueOf(tokens[3].trim().uppercase())
							)
						} catch (e: Exception) {
							null // 데이터 변환 중 오류 발생 시 해당 줄은 무시
						}
					}
				// CSV 로딩이 끝나면 필터링을 한 번 실행합니다.
				filterAndFetchPlaces()
			} catch (e: Exception) {
				// 파일 읽기 실패 처리
			}
		}
	}

	// 현재 상태(위치, 카테고리)에 따라 장소들을 필터링하여 UI에 반영하는 함수
	private fun filterAndFetchPlaces() {
		val currentState = _uiState.value
		val currentLocation = currentState.currentLocation ?: return // 현재 위치가 없으면 중단

		val filteredPlaces = allPlaces.filter { place ->
			// 1. 선택된 카테고리와 일치하는지 확인
			val isCategoryMatch = place.category == currentState.selectedCategory

			// 2. 2km 이내인지 거리 계산
			val placeLocation = Location("place").apply {
				latitude = place.latitude
				longitude = place.longitude
			}
			val distanceInMeters = currentLocation.distanceTo(placeLocation)
			val isWithinDistance = distanceInMeters <= 2000

			// 두 조건을 모두 만족하는 장소만 선택
			isCategoryMatch && isWithinDistance
		}

		// 필터링된 장소 목록으로 UI 상태 업데이트
		_uiState.update { it.copy(places = filteredPlaces) }
	}
}