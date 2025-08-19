package com.example.pet_project_frontend.presentation.map

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.R
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

@Composable
fun MapScreen(
	viewModel: MapViewModel = hiltViewModel()
) {
	val context = LocalContext.current
	val mapView = remember { MapView(context) }
	var kakaoMapInstance by remember { mutableStateOf<KakaoMap?>(null) }
	val uiState by viewModel.uiState.collectAsState()

	val permissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		onResult = { isGranted ->
			if (isGranted) {
				getCurrentLocation(context as Activity) { lat, lon ->
					val location = android.location.Location("").apply {
						latitude = lat
						longitude = lon
					}
					viewModel.updateCurrentLocation(location)
				}
			} else {
				Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
			}
		}
	)

	AndroidView(
		modifier = Modifier.fillMaxSize(),
		factory = {
			mapView.apply {
				start(object : MapLifeCycleCallback() {
					override fun onMapDestroy() {
						Log.d("KakaoMapLifeCycle", "onMapDestroy")
					}

					override fun onMapError(error: Exception) {
						Log.e("KakaoMapError", "지도 에러 발생: ${error.message}", error)
					}
				}, object : KakaoMapReadyCallback() {
					override fun onMapReady(kakaoMap: KakaoMap) {
						kakaoMapInstance = kakaoMap
						Log.d("KakaoMapSuccess", "지도 준비 완료!")

						if (ContextCompat.checkSelfPermission(
								context,
								Manifest.permission.ACCESS_FINE_LOCATION
							) == PackageManager.PERMISSION_GRANTED
						) {
							getCurrentLocation(context as Activity) { lat, lon ->
								val location = android.location.Location("").apply {
									latitude = lat
									longitude = lon
								}
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

	LaunchedEffect(uiState, kakaoMapInstance) {
		kakaoMapInstance?.let { map ->
			uiState.currentLocation?.let { location ->
				val position = LatLng.from(location.latitude, location.longitude)
				map.moveCamera(CameraUpdateFactory.newCenterPosition(position, 15))
			}

			map.labelManager?.clearAll()

			uiState.places.forEach { place ->
				val options = LabelOptions.from(LatLng.from(place.latitude, place.longitude))
					.setStyles(LabelStyles.from(LabelStyle.from(R.drawable.ic_launcher_foreground)))

				map.labelManager?.layer?.addLabel(options)
			}
		}
	}
}

private fun getCurrentLocation(activity: Activity, onResult: (Double, Double) -> Unit) {
	val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
	try {
		fusedLocationClient.lastLocation.addOnSuccessListener { location ->
			location?.let {
				onResult(it.latitude, it.longitude)
			}
		}
	} catch (e: SecurityException) {
		Log.e("LocationError", "위치 권한이 없습니다.", e)
	}
}