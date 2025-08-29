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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.res.painterResource

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
	val sheetPeekHeight = 80.dp
	val scope = rememberCoroutineScope()

	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	val tabBarTopPadding = 16.dp
	val tabBarHeight = 38.dp
	val desiredSheetTopMargin = 140.dp

	val topBarAreaHeight = tabBarTopPadding + tabBarHeight + desiredSheetTopMargin
	val maxSheetHeight = screenHeight - topBarAreaHeight

	BottomSheetScaffold(
		scaffoldState = scaffoldState,
		sheetPeekHeight = sheetPeekHeight,
		sheetContainerColor = MyPageColors.CardBackground,
		sheetContent = {
			// [수정됨 1] Wrapper Box 패턴 적용
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(min = sheetPeekHeight, max = maxSheetHeight)
			) {
				if (selectedPlace == null) {
					PlaceList(
						uiState = uiState,
						onPlaceClick = { place ->
							selectedPlace = place
						}
					)
				} else {
					PlaceDetail(
						place = selectedPlace!!,
						onBackClick = {
							selectedPlace = null
						}
					)
				}
			}
		}
	) { innerPadding ->
		Box(
			modifier = Modifier.fillMaxSize()
		) {
			AndroidView(
				modifier = Modifier
					.matchParentSize()
					.padding(innerPadding),
				factory = {
					mapView.apply {
						start(object : MapLifeCycleCallback() {
							override fun onMapDestroy() {}
							override fun onMapError(error: Exception) { Log.e("KakaoMapError", "Map Error: ${error.message}") }
						}, object : KakaoMapReadyCallback() {
							override fun onMapReady(kakaoMap: KakaoMap) {
								kakaoMapInstance = kakaoMap
								kakaoMap.setOnLabelClickListener { _, _, label ->
									(label.tag as? MapPlace)?.let { place ->
										selectedPlace = place
										scope.launch { scaffoldState.bottomSheetState.expand() }
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
					.padding(top = 16.dp),
				contentAlignment = Alignment.TopCenter
			) {
				// [수정됨 2] TabRow 너비 문제 해결
				TabRow(
					selectedTabIndex = uiState.selectedCategory.ordinal,
					containerColor = Color.Transparent,
					modifier = Modifier
						.shadow(elevation = 4.dp, spotColor = Color(0x26000000), ambientColor = Color(0x26000000))
						.width(250.dp) // .wrapContentWidth() 삭제
						.height(38.dp)
						.background(color = MyPageColors.Background, shape = RoundedCornerShape(size = 12.dp))
						.padding(start = 4.dp, top = 3.dp, end = 4.dp, bottom = 3.dp),
					indicator = {},
					divider = {}
				) {
					PlaceCategory.values().forEach { category ->
						val isSelected = uiState.selectedCategory == category
						val tabModifier = if (isSelected) {
							Modifier
								.shadow(elevation = 4.dp, spotColor = Color(0x26000000), ambientColor = Color(0x26000000))
								.height(32.dp)
								.background(color = MyPageColors.TapChosen, shape = RoundedCornerShape(size = 10.dp))
								.padding(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 6.dp)
						} else {
							Modifier
								.height(32.dp)
								.padding(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 6.dp)
								.clip(RoundedCornerShape(size = 10.dp))
						}

						Tab(
							selected = isSelected,
							onClick = { viewModel.selectCategory(category) },
							modifier = tabModifier,
							text = {
								Box(
									modifier = Modifier.height(25.dp),
									contentAlignment = Alignment.Center
								) {
									Text(
										text = category.displayName,
										style = TextStyle(
											fontSize = 15.sp,
											lineHeight = 18.sp,
											fontWeight = FontWeight(600),
											color = if (isSelected) Color.Black else MyPageColors.Tertiary
										)
									)
								}
							},
							selectedContentColor = Color.Transparent,
							unselectedContentColor = Color.Transparent
						)
					}
				}
			}

			FloatingActionButton(
				onClick = { refreshLocation() },
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(
						end = 16.dp,
						// [수정됨 3] FAB 위치 문제 해결
						bottom = innerPadding.calculateBottomPadding() + 10.dp
					),
				shape = RoundedCornerShape(30.dp),
				containerColor = MyPageColors.CardBackground
			) {
				Icon(
					imageVector = Icons.Default.MyLocation,
					contentDescription = "현재 위치 새로고침",
					tint = MyPageColors.Primary
				)
			}
		}
	}

	LaunchedEffect(uiState, kakaoMapInstance) {
		kakaoMapInstance?.let { map ->
			map.labelManager?.clearAll()
			uiState.currentLocation?.let {
				val myLocationStyle = LabelStyle.from(R.drawable.map_marker)
					.setAnchorPoint(0.5f, 0.5f) // <--- 이 부분을 추가하세요! (아이콘 모양에 맞게 u, v 값 조절)
				val myLocationOptions = LabelOptions.from(LatLng.from(it.latitude, it.longitude))
					.setStyles(LabelStyles.from(myLocationStyle)) // 수정된 스타일 적용
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
fun PlaceList(
	uiState: MapUiState,
	onPlaceClick: (MapPlace) -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = "주변 ${uiState.selectedCategory.displayName} 시설",
			fontSize = 18.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
			textAlign = TextAlign.Start
		)
		if (uiState.isLoading) {
			Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
				Text(text = "주변에 장소를 불러오는 중입니다....", color = MyPageColors.Tertiary)
			}
		} else if (uiState.places.isEmpty()) {
			Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
				Text(text = "주변에 등록된 장소가 없습니다.", color = MyPageColors.Tertiary)
			}
		} else {
			LazyColumn(
				modifier = Modifier.padding(horizontal = 16.dp)
			) {
				items(uiState.places) { place ->
					PlaceItem(
						place = place,
						currentLocation = uiState.currentLocation,
						onClick = { onPlaceClick(place) }
					)
				}
				item { Spacer(modifier = Modifier.height(16.dp)) }
			}
		}
	}
}

@Composable
fun PlaceDetail(
	place: MapPlace,
	onBackClick: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(top = 7.dp)
		) {
			IconButton(onClick = onBackClick) {
				Icon(Icons.Default.ArrowBack, contentDescription = "목록보기")
			}
			Spacer(modifier = Modifier.width(8.dp))
			Text(text = "목록 보기", fontSize = 18.sp, color = MyPageColors.Secondary)
		}
		Spacer(modifier = Modifier.height(16.dp))
		Text(
			text = place.name,
			fontSize = 22.sp,
			fontWeight = FontWeight.Bold,
			color = MyPageColors.Primary,
			modifier = Modifier.padding(vertical = 8.dp)
		)
		val rawOperateTime = place.operateTime // "월~금: ...,토요일: ...,일요일: ..."
		val lines = rawOperateTime.split(",") // 먼저 쉼표로 각 줄을 나눔

		val formattedOperateTime = lines.mapIndexed { index, line ->
			if (index == 0) { // 첫 번째 줄에만
				"" + line.trim()
			} else {
				line.trim()
			}
		}.joinToString("\n") // 다시 \n으로 합침
		DetailInfoRow(icon = Icons.Default.LocationOn, content = place.address)
		DetailInfoRow(icon = Icons.Default.Call, content = place.phoneNumber)
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
			Text(text = content, fontSize = 16.sp, color = MyPageColors.Secondary)
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
			.clickable(onClick = onClick)
			.padding(horizontal = 16.dp, vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Image(
			painter = painterResource(id = place.category.icon),
			contentDescription = place.category.csvName,
			modifier = Modifier.size(40.dp)
		)
		Spacer(modifier = Modifier.width(16.dp))
		Column(modifier = Modifier.weight(1f)) {
			Text(text = place.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MyPageColors.Primary)
			Spacer(modifier = Modifier.height(3.dp))
			Row(verticalAlignment = Alignment.CenterVertically) {
				if (distanceText.isNotEmpty()) {
					Text(text = distanceText, color = MyPageColors.Secondary, fontSize = 14.sp)
					Text(text = " | ", color = MyPageColors.Tertiary, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 4.dp))
				}
				Text(text = place.shortAddress, color = MyPageColors.Secondary, fontSize = 14.sp)
			}
		}
	}
}

private fun getCurrentLocation(activity: Activity, onResult: (Double, Double) -> Unit) {
	val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
	try {
		if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
			ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			fusedLocationClient.lastLocation.addOnSuccessListener { location ->
				location?.let { onResult(it.latitude, it.longitude) }
					?: Toast.makeText(activity, "현재 위치를 가져올 수 없습니다. GPS를 확인해주세요.", Toast.LENGTH_SHORT).show()
			}.addOnFailureListener {
				Log.e("LocationError", "Failed to get location.", it)
				Toast.makeText(activity, "위치 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
			}
		} else {
			Toast.makeText(activity, "위치 권한이 없습니다.", Toast.LENGTH_SHORT).show()
		}
	} catch (e: SecurityException) {
		Log.e("LocationError", "Location permission not granted.", e)
		Toast.makeText(activity, "보안 문제로 위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
	}
}