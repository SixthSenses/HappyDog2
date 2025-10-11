// 프로필 사진 크롭 화면 - 제스처 및 임시 저장 로직 갱신.
// 변경의도: 프로필 사진 크롭 화면에서 원형 미리보기와 이미지 배치를 보정합니다.
package com.example.pet_project_frontend.presentation.mypage.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private object CropTokens {
    val TopBarBackground = Color(0xFF000000)
    val TopBarContent = Color(0xFFFFFFFF)
    val OverlayScrim = Color(0x8C000000)
    val Primary = Color(0xFF3182F6)
}

private data class CropSnapshot(
    val contentWidth: Float,
    val contentHeight: Float,
    val circleCenter: Offset,
    val circleRadius: Float,
    val baseScale: Float,
    val baseOffset: Offset
)

@Composable
fun PhotoCropScreen(
    source: Uri,
    onCropped: (Uri) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val navigationPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var originalBitmap by remember(source) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var isCropping by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var layoutSnapshot by remember { mutableStateOf<CropSnapshot?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(source) {
        isLoading = true
        loadFailed = false
        scale = 1f
        offset = Offset.Zero
        originalBitmap = loadBitmapFromUri(context, source)
        isLoading = false
        loadFailed = originalBitmap == null
    }

    val imageBitmap = originalBitmap?.asImageBitmap()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CropTopBar(onBack = onCancel)

            @Suppress("UnusedBoxWithConstraintsScope")
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val density = LocalDensity.current
                val horizontalPadding = 0.dp
                val verticalPadding = (maxHeight * 0.12f).coerceIn(48.dp, 140.dp)
                val contentWidthDp = (maxWidth - horizontalPadding * 2f).coerceAtLeast(120.dp)
                val contentHeightDp = (maxHeight - verticalPadding * 2f).coerceAtLeast(120.dp)

                val contentWidthPx = with(density) { contentWidthDp.toPx() }
                val contentHeightPx = with(density) { contentHeightDp.toPx() }
                val circleDiameterPx = min(contentWidthPx, contentHeightPx)
                val circleRadiusPx = circleDiameterPx / 2f

                val bitmap = originalBitmap
                val imageWidth = bitmap?.width?.toFloat() ?: contentWidthPx
                val imageHeight = bitmap?.height?.toFloat() ?: contentHeightPx
                val baseScale = if (bitmap != null) {
                    max(contentWidthPx / imageWidth, contentHeightPx / imageHeight)
                } else {
                    1f
                }
                val baseOffset = Offset(
                    (contentWidthPx - imageWidth * baseScale) / 2f,
                    (contentHeightPx - imageHeight * baseScale) / 2f
                )

                val snapshot = if (bitmap != null) {
                    CropSnapshot(
                        contentWidth = contentWidthPx,
                        contentHeight = contentHeightPx,
                        circleCenter = Offset(contentWidthPx / 2f, contentHeightPx / 2f),
                        circleRadius = circleRadiusPx,
                        baseScale = baseScale,
                        baseOffset = baseOffset
                    )
                } else {
                    null
                }

                LaunchedEffect(snapshot) {
                    layoutSnapshot = snapshot
                }

                fun clampOffset(candidate: Offset, currentScale: Float): Offset {
                    val scaledWidth = imageWidth * baseScale * currentScale
                    val scaledHeight = imageHeight * baseScale * currentScale
                    val maxOffsetX = ((scaledWidth - circleDiameterPx) / 2f).coerceAtLeast(0f)
                    val maxOffsetY = ((scaledHeight - circleDiameterPx) / 2f).coerceAtLeast(0f)
                    return Offset(
                        x = candidate.x.coerceIn(-maxOffsetX, maxOffsetX),
                        y = candidate.y.coerceIn(-maxOffsetY, maxOffsetY)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(contentWidthDp)
                        .height(contentHeightDp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageBitmap != null && !isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(imageBitmap, contentWidthPx, contentHeightPx) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val newScale = (scale * zoom).coerceIn(1f, 4f)
                                        scale = newScale
                                        offset = clampOffset(offset, newScale)
                                        offset = clampOffset(offset + pan, newScale)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "프로필 편집 이미지",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offset.x
                                        translationY = offset.y
                                    },
                                contentScale = ContentScale.Crop
                            )

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val left = (contentWidthPx - circleDiameterPx) / 2f
                                val top = (contentHeightPx - circleDiameterPx) / 2f
                                val overlayPath = Path().apply {
                                    addOval(
                                        Rect(
                                            offset = Offset(left, top),
                                            size = Size(circleDiameterPx, circleDiameterPx)
                                        )
                                    )
                                }
                                val layerBounds = Rect(offset = Offset.Zero, size = size)
                                drawContext.canvas.saveLayer(
                                    layerBounds,
                                    androidx.compose.ui.graphics.Paint()
                                )
                                drawRect(color = CropTokens.OverlayScrim)
                                drawPath(
                                    path = overlayPath,
                                    color = Color.Transparent,
                                    blendMode = BlendMode.Clear
                                )
                                drawContext.canvas.restore()
                            }
                        }
                    } else if (isLoading) {
                        CircularProgressIndicator(color = CropTokens.TopBarContent)
                    } else {
                        Text(
                            text = "이미지를 불러오지 못했습니다.",
                            color = Color.White,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                                fontWeight = FontWeight.W500
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val bitmap = originalBitmap
                    val snapshot = layoutSnapshot
                    if (bitmap != null && snapshot != null && !isCropping) {
                        isCropping = true
                        coroutineScope.launch {
                            val cropped = cropBitmapToCircle(bitmap, snapshot, scale, offset)
                            val uri = cropped?.let { persistBitmap(context, it) }
                            isCropping = false
                            if (uri != null) {
                                onCropped(uri)
                            } else {
                                onCancel()
                            }
                        }
                    }
                },
                enabled = !isCropping && !isLoading && !loadFailed,
                colors = ButtonDefaults.buttonColors(containerColor = CropTokens.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 21.dp)
                    .height(58.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "확인",
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                        fontWeight = FontWeight.W600,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp + navigationPadding))
        }

        if (isCropping) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CropTokens.TopBarContent)
            }
        }
    }
}

@Composable
private fun CropTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CropTokens.TopBarBackground)
    ) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .height(64.dp)
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = CropTokens.TopBarContent
                )
            }
            Text(
                text = "크기 조절",
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                    fontWeight = FontWeight.W600,
                    color = CropTokens.TopBarContent
                )
            )
        }
    }
}

private suspend fun loadBitmapFromUri(
    context: Context,
    source: Uri
): Bitmap? = withContext(Dispatchers.IO) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val decoderSource = ImageDecoder.createSource(context.contentResolver, source)
            ImageDecoder.decodeBitmap(decoderSource) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            context.contentResolver.openInputStream(source)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }
    } catch (e: Exception) {
        null
    }
}

private suspend fun cropBitmapToCircle(
    bitmap: Bitmap,
    snapshot: CropSnapshot,
    scale: Float,
    offset: Offset
): Bitmap? = withContext(Dispatchers.Default) {
    val topLeft = snapshot.circleCenter - Offset(snapshot.circleRadius, snapshot.circleRadius)
    val bottomRight = snapshot.circleCenter + Offset(snapshot.circleRadius, snapshot.circleRadius)

    fun mapToOriginal(point: Offset): Offset {
        val containerCenter = Offset(snapshot.contentWidth / 2f, snapshot.contentHeight / 2f)
        val adjusted = containerCenter + (point - offset - containerCenter) / scale
        val original = Offset(
            x = (adjusted.x - snapshot.baseOffset.x) / snapshot.baseScale,
            y = (adjusted.y - snapshot.baseOffset.y) / snapshot.baseScale
        )
        return Offset(
            x = original.x.coerceIn(0f, bitmap.width.toFloat()),
            y = original.y.coerceIn(0f, bitmap.height.toFloat())
        )
    }

    val mappedTopLeft = mapToOriginal(topLeft)
    val mappedBottomRight = mapToOriginal(bottomRight)

    val cropWidth = mappedBottomRight.x - mappedTopLeft.x
    val cropHeight = mappedBottomRight.y - mappedTopLeft.y
    if (cropWidth <= 0f || cropHeight <= 0f) {
        return@withContext null
    }

    val cropSize = min(cropWidth, cropHeight)
    val centerX = (mappedTopLeft.x + mappedBottomRight.x) / 2f
    val centerY = (mappedTopLeft.y + mappedBottomRight.y) / 2f
    val left = (centerX - cropSize / 2f).coerceIn(0f, bitmap.width - cropSize)
    val top = (centerY - cropSize / 2f).coerceIn(0f, bitmap.height - cropSize)
    val rect = RectF(left, top, left + cropSize, top + cropSize)

    val outputSize = cropSize.roundToInt().coerceAtLeast(1)
    val output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val maskPath = AndroidPath().apply {
        addCircle(
            outputSize / 2f,
            outputSize / 2f,
            outputSize / 2f,
            AndroidPath.Direction.CW
        )
    }
    canvas.clipPath(maskPath)
    canvas.drawBitmap(bitmap, -rect.left, -rect.top, paint)
    output
}

private suspend fun persistBitmap(
    context: Context,
    bitmap: Bitmap
): Uri? = withContext(Dispatchers.IO) {
    // TODO 서버 연동 시 치환: 파일 저장 대신 업로드 API로 교체 예정
    try {
        val file = File(context.cacheDir, "profile_crop_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()
        Uri.fromFile(file)
    } catch (e: Exception) {
        bitmap.recycle()
        null
    }
}
