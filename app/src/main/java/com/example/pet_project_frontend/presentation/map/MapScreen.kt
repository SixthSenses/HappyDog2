package com.example.pet_project_frontend.presentation.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
	viewModel: MapViewModel = hiltViewModel()
) {
	val context = LocalContext.current
	val mapView = remember { MapView(context) }
	var kakaoMapInstance by remember { mutableStateOf<KakaoMap?>(null) }
	val uiState by viewModel.uiState.collectAsState()
	var isFirstLocationUpdate by remember { mutableStateOf(true) }
	var selectedPlace by remember { mutableStateOf<MapPlace?>(null) }

	val refreshLocation = {
		getCurrentLocation(context as Activity) { lat, lon ->
			val location = android.location.Location("").apply {
				latitude = lat
				longitude = lon
			}
			viewModel.updateCurrentLocation(location)
			kakaoMapInstance?.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lon), 15))
			Toast.makeText(context, "현재 위치를 업데이트했습니다.", Toast.LENGTH_SHORT).show()
		}
	}

	val permissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		onResult = { isGranted -> if (isGranted) refreshLocation() else Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show() }
	)

	val scaffoldState = rememberBottomSheetScaffoldState()
	val sheetPeekHeight = 100.dp
	val scope = rememberCoroutineScope()

	BottomSheetScaffold(
		scaffoldState = scaffoldState,
		sheetPeekHeight = sheetPeekHeight,
		sheetContainerColor = MyPageColors.CardBackground,
		sheetContent = {
			if (selectedPlace == null) {
				// 목록 화면 UI
				PlaceList(
					uiState = uiState,
					onPlaceClick = { place ->
						selectedPlace = place // 장소를 클릭하면 상태 변경
					}
				)
			} else {
				// 상세 화면 UI
				PlaceDetail(
					place = selectedPlace!!,
					onBackClick = {
						selectedPlace = null // 뒤로가기 클릭 시 상태를 null로 변경
					}
				)
			}
		}
	) { innerPadding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
		) {
			AndroidView(
				modifier = Modifier
					.matchParentSize()
					.padding(innerPadding),
				factory = {
					mapView.apply {
						start(object : MapLifeCycleCallback() {
							override fun onMapDestroy() {}
							override fun onMapError(error: Exception) {
								Log.e("KakaoMapError", "Map Error: ${error.message}")
							}
						}, object : KakaoMapReadyCallback() {
							override fun onMapReady(kakaoMap: KakaoMap) {
								kakaoMapInstance = kakaoMap

								kakaoMap.setOnLabelClickListener { map, layer, label ->
									// 클릭된 라벨의 태그를 MapPlace 타입으로 변환합니다.
									(label.tag as? MapPlace)?.let { place ->
										// 1. 선택된 장소 상태를 업데이트합니다.
										selectedPlace = place

										// 2. 바텀시트를 위로 확장시킵니다.
										scope.launch {
											scaffoldState.bottomSheetState.expand()
										}
									}
									true
								}
								if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
									getCurrentLocation(context as Activity) { lat, lon ->
										val location = android.location.Location("").apply { latitude = lat; longitude = lon }
										viewModel.updateCurrentLocation(location)
									}
								} else {
									permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
								}
							}
						})
					}
				}
			)

			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 16.dp, start = 16.dp, end = 16.dp)
			) {
				TabRow(
					selectedTabIndex = uiState.selectedCategory.ordinal,
					containerColor = MyPageColors.CardBackground,
					modifier = Modifier.clip(RoundedCornerShape(30.dp)),
					indicator = {},
					divider = {}
				) {
					PlaceCategory.values().forEach { category ->
						val isSelected = uiState.selectedCategory == category
						Tab(
							selected = uiState.selectedCategory == category,
							onClick = { viewModel.selectCategory(category) },
							modifier = Modifier.background(
								if (isSelected)  Color.Transparent else MyPageColors.Background
							),
							text = { Text(category.displayName, fontWeight = FontWeight.Bold) },
							selectedContentColor = MyPageColors.Primary,
							unselectedContentColor = MyPageColors.Tertiary

						)
					}
				}
			}

			FloatingActionButton(
				onClick = { refreshLocation() },
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(end = 16.dp, bottom = sheetPeekHeight + 8.dp),
				shape = RoundedCornerShape(30.dp),
				containerColor = MyPageColors.CardBackground
			) {
				Icon(imageVector = Icons.Default.MyLocation, contentDescription = "현재 위치 새로고침", tint = MyPageColors.Primary)
			}
		}
	}

	LaunchedEffect(uiState, kakaoMapInstance) {
		kakaoMapInstance?.let { map ->
			map.labelManager?.clearAll()
			uiState.currentLocation?.let {
				val myLocationOptions = LabelOptions.from(LatLng.from(it.latitude, it.longitude)).setStyles(LabelStyles.from(LabelStyle.from(R.drawable.my_location_marker)))
				map.labelManager?.layer?.addLabel(myLocationOptions)
				if (isFirstLocationUpdate) {
					val position = LatLng.from(it.latitude, it.longitude)
					map.moveCamera(CameraUpdateFactory.newCenterPosition(position, 15))
					isFirstLocationUpdate = false
				}
			}
			uiState.places.forEach { place ->
				val markerResId = place.category.markerResId
				val options = LabelOptions.from(LatLng.from(place.latitude, place.longitude))
					.setStyles(LabelStyles.from(LabelStyle.from(markerResId)))
					.setTag(place)
				map.labelManager?.layer?.addLabel(options)
			}
		}
	}
}
@Composable
fun PlaceList(uiState: MapUiState, onPlaceClick: (MapPlace) -> Unit) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.defaultMinSize(minHeight = 1.dp)
	) {
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = "주변 '${uiState.selectedCategory.displayName}' 시설",
			fontSize = 18.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
			textAlign = TextAlign.Start
		)
		if (uiState.isLoading) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 32.dp),
				contentAlignment = Alignment.Center
			) {
				Text(text = "주변에 장소를 불러오는 중입니다....", color = MyPageColors.Tertiary)
			}
		} else if(uiState.places.isEmpty()) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 32.dp),
				contentAlignment = Alignment.Center
			) {
				Text(text = "주변에 등록된 장소가 없습니다.", color = MyPageColors.Tertiary)
			}
		} else {
				LazyColumn(
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.fillMaxHeight()
				) {
					items(uiState.places) { place ->
						PlaceItem(
							place = place,
							currentLocation = uiState.currentLocation,
							onClick = { onPlaceClick(place) } // 클릭 이벤트 전달
						)
					}
					item { Spacer(modifier = Modifier.height(16.dp)) }
				}

			}

		}
	}


@Composable
fun PlaceDetail(place: MapPlace, onBackClick: () -> Unit) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
	) {
		// 뒤로가기 버튼과 제목
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(top = 8.dp)
		) {
			IconButton(onClick = onBackClick) {
				Icon(Icons.Default.ArrowBack, contentDescription = "목록보기")
			}
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = "목록 보기",
				fontSize = 18.sp,
				//fontWeight = FontWeight.Bold,
				color = MyPageColors.Secondary
			)
		}
		Spacer(modifier = Modifier.height(16.dp))
		// 장소명 (크고 진하게)
		Text(
			text = place.name,
			fontSize = 22.sp,
			fontWeight = FontWeight.Bold,
			color = MyPageColors.Primary,
			modifier = Modifier.padding(vertical = 8.dp)
		)
		// 상세 정보 내용
		DetailInfoRow(icon = Icons.Default.LocationOn, content = place.address)
		DetailInfoRow(icon = Icons.Default.Call, content = place.phoneNumber)
		val formattedOperateTime = place.operateTime.replace(",", "\n")
		DetailInfoRow(icon = Icons.Default.Schedule, content = formattedOperateTime)
		DetailInfoRow(icon = Icons.Default.Link, content = place.homePage, isLink = true)

		Spacer(modifier = Modifier.height(16.dp))
	}
}

@Composable
fun DetailInfoRow(icon: ImageVector, content: String, isLink: Boolean = false) {
	val context = LocalContext.current
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			modifier = Modifier.size(24.dp),
			tint = MyPageColors.IconColor
		)
		Spacer(modifier = Modifier.width(16.dp))

		if (isLink && content.startsWith("http")) {
			Text(
				text = content,
				fontSize = 16.sp,
				color = MyPageColors.Accent,
				textDecoration = TextDecoration.Underline,
				modifier = Modifier.clickable {
					try {
						val intent = Intent(Intent.ACTION_VIEW, Uri.parse(content))
						context.startActivity(intent)
					} catch (e: Exception) {
						Toast.makeText(context, "링크를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
					}
				}
			)
		} else {
			Text(
				text = content,
				fontSize = 16.sp,
				color = MyPageColors.Secondary
			)
		}
	}
}

@Composable
fun PlaceItem(place: MapPlace, currentLocation: Location?, onClick: () -> Unit) {
	var distanceText = ""
	currentLocation?.let {
		val placeLocation = Location("place").apply {
			latitude = place.latitude
			longitude = place.longitude
		}
		val distanceInMeters = it.distanceTo(placeLocation)
		distanceText = if (distanceInMeters >= 1000) {
			"%.1fkm".format(distanceInMeters / 1000)
		} else {
			"${distanceInMeters.roundToInt()}m"
		}
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick) // Row 전체에 클릭 효과 적용
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically // 세로 중앙 정렬
	) {
		// 아이콘
		Image(
			painter = painterResource(id = place.category.icon),
			contentDescription = place.category.csvName,
			modifier = Modifier.size(40.dp)
		)

		Spacer(modifier = Modifier.width(16.dp))

		// 텍스트 정보를 담는 Column
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = place.name,
				fontWeight = FontWeight.SemiBold,
				fontSize = 16.sp,
				color = MyPageColors.Primary
			)
			Spacer(modifier = Modifier.height(6.dp))
			Row(verticalAlignment = Alignment.CenterVertically) {
				if (distanceText.isNotEmpty()) {
					Text(
						text = distanceText,
						color = MyPageColors.Secondary,
						fontSize = 14.sp
					)
					Text(
						text = " | ",
						color = MyPageColors.Tertiary,
						fontSize = 14.sp,
						modifier = Modifier.padding(horizontal = 4.dp)
					)
				}
				Text(
					text = place.shortAddress,
					color = MyPageColors.Secondary,
					fontSize = 14.sp
				)
			}
		}
	}
}

private fun getCurrentLocation(activity: Activity, onResult: (Double, Double) -> Unit) {
	val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
	try {
		fusedLocationClient.lastLocation.addOnSuccessListener { location -> location?.let { onResult(it.latitude, it.longitude) } }
			.addOnFailureListener { Log.e("LocationError", "Failed to get location.", it) }
	} catch (e: SecurityException) {
		Log.e("LocationError", "Location permission not granted.", e)
	}
}