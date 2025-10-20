package com.example.pet_project_frontend.presentation.translator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import com.example.pet_project_frontend.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt
import java.util.ArrayDeque
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

@Composable
fun TranslatorScreen(openNotice: ((@Composable (closeNotice: () -> Unit) -> Unit) -> Unit)) {
    val cameraStateHolder = remember { CameraStateHolder() }
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            Log.d("CameraPermission", "Camera permission granted")
        } else {
            Log.d("CameraPermission", "Camera permission denied")
        }
    }

    LaunchedEffect(key1 = hasPermission) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        CameraPreviewScreen(cameraStateHolder, modifier = Modifier.fillMaxSize())
        openNotice { closeNotice ->
            TranslatorNotice(closeNotice)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "카메라 권한이 필요합니다.",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(19.dp))
                Text(
                    "설정  >  애플리케이션  >  행복하개  >  권한  >  카메라  >  허용",
                    fontSize = 16.sp,
                    letterSpacing = (-0.1).em,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun TranslatorTest() {
    val context = LocalContext.current
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.n02096294_7456)
    var bestCandidate by remember { mutableStateOf<FloatArray?>(null) }

    LaunchedEffect(Unit) {
        val modelBuffer = FileUtil.loadMappedFile(context, "best_float32.tflite")
        val interpreter = Interpreter(modelBuffer)
        interpreter.allocateTensors()

        val inputDetails = interpreter.getInputTensor(0)
        Log.d("TFLite", inputDetails.shape().contentToString())

        val outputDetails = interpreter.getOutputTensor(0)
        Log.d("TFLite", outputDetails.shape().contentToString())

        val tensorImage = TensorImage.fromBitmap(bitmap)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(640, 640, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()
        val processedTensorImage = imageProcessor.process(tensorImage)
        val tensorBuffer = processedTensorImage.tensorBuffer
        val inputBuffer = TensorBuffer.createFixedSize(inputDetails.shape(), inputDetails.dataType())
        inputBuffer.loadArray(tensorBuffer.floatArray)
        Log.d("TFLite", inputBuffer.shape.contentToString())

        val outputBuffer = TensorBuffer.createFixedSize(outputDetails.shape(), outputDetails.dataType())
        interpreter.run(inputBuffer.buffer, outputBuffer.buffer)
        Log.d("TFLite", outputBuffer.shape.contentToString())

        val outputArray = outputBuffer.floatArray
        val firstCandidate = FloatArray(77) { feature ->
            outputArray[feature * 8400]
        }
        Log.d("TFLite", firstCandidate.contentToString())

        val confidences = FloatArray(8400) { candidate ->
            outputArray[4 * 8400 + candidate]
        }
        val validIndices = confidences
            .toList()
            .mapIndexedNotNull { idx, value -> if (value >= 0.5f) idx else null }
        Log.d("TFLite", validIndices.toString())
        val bestIdx = validIndices.maxByOrNull { idx -> confidences[idx] }
        Log.d("TFLite", bestIdx.toString())
        bestCandidate = bestIdx?.let { idx ->
            FloatArray(77) { feature ->
                outputArray[feature * 8400 + idx]
            }
        }
        Log.d("TFLite", bestCandidate.contentToString())

        // lstm
        val lstmModelBuffer = FileUtil.loadMappedFile(context, "dog_mood_understanding.tflite")
        val options = Interpreter.Options().apply {
            addDelegate(FlexDelegate()) // Flex 연산 지원
        }
        val lstmInterpreter = Interpreter(lstmModelBuffer, options)
        lstmInterpreter.allocateTensors()

        val lstmInputDetails = lstmInterpreter.getInputTensor(0)
        Log.d("TFLite", lstmInputDetails.shape().contentToString())

        val lstmOutputDetails = lstmInterpreter.getOutputTensor(0)
        Log.d("TFLite", lstmOutputDetails.shape().contentToString())

    }

    bestCandidate?.let { candidate ->
        DrawPredictionOverlay(
            bitmap = bitmap,
            bestCandidate = candidate
        )

        val remappedKeypoints = remapKeypoints(normalizeKeypoints(candidate))
        Log.d("TFLite", remappedKeypoints.contentToString())
    }
}

fun normalizeKeypoints(bestCandidate: FloatArray): Pair<List<Float>, List<Float>> {
    // 1. 바운딩 박스 정보 추출
    // 모든 값은 이미지 전체 기준 0~1로 정규화되어 있다고 가정
    val bboxXCenter = bestCandidate[0]
    val bboxYCenter = bestCandidate[1]
    val bboxWidth = bestCandidate[2]
    val bboxHeight = bestCandidate[3]
    // bestCandidateArray[4]는 confidence, 사용하지 않음

    // 바운딩 박스의 좌상단 코너 (이미지 기준 정규화된 좌표)
    // YOLO 형식에서 center, width/height를 사용하므로 좌상단을 계산합니다.
    val bboxXMin = bboxXCenter - (bboxWidth / 2f)
    val bboxYMin = bboxYCenter - (bboxHeight / 2f)

    // 2. 키포인트 정보 추출 및 바운딩 박스 기준으로 재정규화
    val normalizedXCoords = mutableListOf<Float>()
    val normalizedYCoords = mutableListOf<Float>()

    // 5번째 인덱스부터 키포인트 시작. 각 키포인트는 (x, y, v) 3개 값으로 구성.
    val kpStartIndex = 5
    val numKeypoints = 24

    for (i in 0 until numKeypoints) {
        val xCoord = bestCandidate[kpStartIndex + i * 3]
        val yCoord = bestCandidate[kpStartIndex + i * 3 + 1]

        val normalizedX = (xCoord - bboxXMin) / bboxWidth
        val normalizedY = (yCoord - bboxYMin) / bboxHeight

        normalizedXCoords.add(normalizedX)
        normalizedYCoords.add(normalizedY)
    }

    var finalXCoords = normalizedXCoords.toMutableList() // List는 immutable, 변경을 위해 MutableList로 복사
    var finalYCoords = normalizedYCoords.toMutableList()

    val minX = minOf(0.0f, normalizedXCoords.minOrNull()!!)
    val maxX = maxOf(1.0f, normalizedXCoords.maxOrNull()!!)
    if (minX < 0f || maxX > 1f) {
        finalXCoords = finalXCoords.map { x -> (x - minX) / (maxX - minX) }.toMutableList()
    }
    val minY = minOf(0.0f, normalizedYCoords.minOrNull()!!)
    val maxY = maxOf(1.0f, normalizedYCoords.maxOrNull()!!)
    if (minY < 0f || maxY > 1f) {
        finalYCoords = finalYCoords.map { y -> (y - minY) / (maxY - minY) }.toMutableList()
    }

    return Pair(finalXCoords, finalYCoords)
}

fun remapKeypoints(keypoints: Pair<List<Float>, List<Float>>): FloatArray {
    val xCoords = keypoints.first
    val yCoords = keypoints.second
    val remappedKeypoints = floatArrayOf(
        0.5f,
        0.5f,
        xCoords[16],
        yCoords[16],
        0.5f,
        0.5f,
        xCoords[17],
        yCoords[17],
        0.5f,
        0.5f,
        xCoords[2],
        yCoords[2],
        xCoords[8],
        yCoords[8],
        xCoords[0],
        yCoords[0],
        xCoords[6],
        yCoords[6],
        xCoords[5],
        yCoords[5],
        xCoords[11],
        yCoords[11],
        xCoords[3],
        yCoords[3],
        xCoords[9],
        yCoords[9],
        xCoords[12],
        yCoords[12],
        xCoords[13],
        yCoords[13]
    )
    return remappedKeypoints
}

@Composable
fun DrawPredictionOverlay(
    bitmap: Bitmap,
    bestCandidate: FloatArray
) {
    // 원본 비율 자동 계산
    val imageWidth = bitmap.width
    val imageHeight = bitmap.height

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(imageWidth / imageHeight.toFloat())
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Canvas(modifier = Modifier.matchParentSize()) {
            // 추론 결과는 항상 정규화(0~1)된 값이라고 가정
            val cx = bestCandidate[0] * size.width
            val cy = bestCandidate[1] * size.height
            val w = bestCandidate[2] * size.width
            val h = bestCandidate[3] * size.height

            val left = cx - w / 2f
            val top = cy - h / 2f

            drawRect(
                color = Color.Green,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(w, h),
                style = Stroke(width = 4f)
            )

            val keypoints = bestCandidate.sliceArray(5 until 77)
            for (i in keypoints.indices step 3) {
                val px = keypoints[i] * size.width
                val py = keypoints[i + 1] * size.height
                drawCircle(
                    color = Color.Red,
                    center = Offset(px, py),
                    radius = 6f
                )
            }
        }
    }
}

@Composable
fun TranslationResult(text: String?) {
    var activeIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        if (text == null) {
            while(true) {
                delay(600)
                activeIndex = (activeIndex + 1) % 3
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp)
    ) {
        if (text != null) {
            Box(
                modifier = Modifier
                    .height(53.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0x66191F28))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.01).em,
                    color = Color.White
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .height(53.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0x66191F28))
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { idx ->
                    val targetAlpha = if (idx == activeIndex) 0.3f else 1f
                    val animatedAlpha by animateFloatAsState(targetValue = targetAlpha, animationSpec = tween(600))
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(color = Color.White.copy(alpha = animatedAlpha), radius = size.minDimension / 2)
                    }
                }
            }
        }
    }
}

@Composable
fun TranslatorHint() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 108.dp)
    ) {
        Box(
            modifier = Modifier
                .height(31.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xCC212121))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "반려견이 나오도록 카메라를 조정해주세요.",
                fontSize = 18.sp,
                letterSpacing = (-0.025).em,
                color = Color.White
            )
        }
    }
}

@Composable
fun TranslatorNotice(
    closeNotice: () -> Unit
) {
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (offsetY.value > 300f) { // 닫기 임계값 예시
                                closeNotice()
                            }
                            // 임계값 미만이면 점차 원위치로 애니메이션
                            offsetY.animateTo(0f, animationSpec = tween(300))
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val newOffset = (offsetY.value + dragAmount).coerceAtLeast(0f)
                            offsetY.snapTo(newOffset)
                        }
                    }
                )
            }
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(bottom = 36.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(13.dp))
            Box(
                modifier = Modifier
                    .size(50.dp, 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE4E7EA))
            )
            Spacer(modifier = Modifier.height(30.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "강아지 번역기",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF191F28)
                )
                Spacer(modifier = Modifier.height(19.dp))
                Text(
                    "강아지의 행동을 정확하게 인식하려면 전신이 화면에 잘 보이도록 촬영해주세요.",
                    fontSize = 18.sp,
                    lineHeight = 1.45.em,
                    letterSpacing = (-0.01).em,
                    color = Color(0xFF4E5968)
                )
            }
            Spacer(modifier = Modifier.height(35.dp))
            Button(
                onClick = closeNotice,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3182F6),        // 버튼 배경색
                    contentColor = Color.White          // 버튼 내부 텍스트(내용) 색상
                )
            ) {
                Text(
                    "확인",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

class CameraStateHolder {
    var bestCandidate by mutableStateOf<FloatArray?>(null)

    var sequence = mutableStateListOf<FloatArray?>()
        private set

    fun addSequence(remappedKeypoints: FloatArray?) {
        if (sequence.size >= 20) {
            sequence.removeAt(0)
        }
        sequence.add(remappedKeypoints)
    }

    val hasAtLeast1Keypoints: Boolean
        get() = sequence.count { it != null } >= 1

    var mood: String? by mutableStateOf(null)
}

class ThrottledImageAnalyzer(
    private val context: Context,
    private val cameraStateHolder: CameraStateHolder
) : ImageAnalysis.Analyzer {
    private val yoloInterpreter by lazy {
        val modelBuffer = FileUtil.loadMappedFile(context, "best_float32.tflite")
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        Interpreter(modelBuffer, options)
    }
    private val dmuInterpreter by lazy {
        val modelBuffer = FileUtil.loadMappedFile(context, "dog_mood_understanding.tflite")
        val options = Interpreter.Options().apply {
            setNumThreads(4)
            addDelegate(FlexDelegate())
        }
        Interpreter(modelBuffer, options)
    }

    private val yoloInputDetails = yoloInterpreter.getInputTensor(0)
    private val yoloOutputDetails = yoloInterpreter.getOutputTensor(0)
    private val dmuInputDetails = dmuInterpreter.getInputTensor(0)
    private val dmuOutputDetails = dmuInterpreter.getOutputTensor(0)

    private var lastYoloTimestamp = 0L
    private var lastDmuTimestamp = 0L

    private var tempYoloResult: FloatArray? = null
    private var tempDmuResult: String? = null

    override fun analyze(image: ImageProxy) {
        try {
            runBlocking {
                coroutineScope {
                    launch {
                        val currentTimestamp = System.currentTimeMillis()
                        if (currentTimestamp - lastYoloTimestamp >= 200) {
                            cameraStateHolder.bestCandidate = tempYoloResult
                            cameraStateHolder.addSequence(
                                if (tempYoloResult != null) {
                                    remapKeypoints(normalizeKeypoints(tempYoloResult!!))
                                } else {
                                    null
                                }
                            )
                            lastYoloTimestamp = currentTimestamp
                            tempYoloResult = null
                        }
                        if (tempYoloResult == null) {
                            val bitmap = image.toBitmap()
                            val tensorImage = TensorImage.fromBitmap(bitmap)
                            val imageProcessor = ImageProcessor.Builder()
                                .add(ResizeOp(640, 640, ResizeOp.ResizeMethod.BILINEAR))
                                .add(NormalizeOp(0f, 255f))
                                .build()
                            val processedTensorImage = imageProcessor.process(tensorImage)
                            val tensorBuffer = processedTensorImage.tensorBuffer
                            val inputBuffer = TensorBuffer.createFixedSize(yoloInputDetails.shape(), yoloInputDetails.dataType())
                            inputBuffer.loadArray(tensorBuffer.floatArray)
                            val outputBuffer = TensorBuffer.createFixedSize(yoloOutputDetails.shape(), yoloOutputDetails.dataType())
                            yoloInterpreter.run(inputBuffer.buffer, outputBuffer.buffer)
                            val outputArray = outputBuffer.floatArray
                            val confidences = FloatArray(8400) { candidate ->
                                outputArray[4 * 8400 + candidate]
                            }
                            val validIndices = confidences
                                .toList()
                                .mapIndexedNotNull { idx, value -> if (value >= 0.7f) idx else null }
                            val bestIdx = validIndices.maxByOrNull { idx -> confidences[idx] }
                            val bestCandidate = bestIdx?.let { idx ->
                                FloatArray(77) { feature ->
                                    outputArray[feature * 8400 + idx]
                                }
                            }
                            tempYoloResult = bestCandidate
                        }
                    }
                    launch {
                        val currentTimestamp = System.currentTimeMillis()
                        if (currentTimestamp - lastDmuTimestamp >= 2000) {
                            cameraStateHolder.mood = tempDmuResult
                            lastDmuTimestamp = currentTimestamp
                            tempDmuResult = null
                        }
                        val nonNullSequence = cameraStateHolder.sequence.filterNotNull()
                        if (tempDmuResult == null && nonNullSequence.size >= 10) {
                            val last10 = nonNullSequence.takeLast(10)
                            val flat = last10.flatMap { it.asList() }.toFloatArray()
                            val inputBuffer = TensorBuffer.createFixedSize(dmuInputDetails.shape(), dmuInputDetails.dataType())
                            inputBuffer.loadArray(flat)
                            val outputBuffer = TensorBuffer.createFixedSize(dmuOutputDetails.shape(), dmuOutputDetails.dataType())
                            dmuInterpreter.run(inputBuffer.buffer, outputBuffer.buffer)
                            val outputArray = outputBuffer.floatArray
                            val mood = when (
                                outputArray.indices.maxByOrNull { outputArray[it] }
                            ) {
                                0 -> "\uD83D\uDE21  공격"
                                1 -> "\uD83D\uDE28  공포"
                                2 -> "\uD83D\uDE22  불안/슬픔"
                                3 -> "\uD83D\uDE0A  편안/안정"
                                4 -> "\uD83D\uDE0D  행복/즐거움"
                                5 -> "\uD83D\uDE12  불쾌"
                                else -> null
                            }
                            tempDmuResult = mood
                        }
                    }
                }
            }
        } catch (e: InterruptedException) {
            Log.e("ThrottledImageAnalyzer", "분석 중단", e)
        } finally {
            image.close()
        }
    }
}


@Composable
fun CameraPreviewScreen(
    stateHolder: CameraStateHolder, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val bestCandidate = stateHolder.bestCandidate
    val sequence = stateHolder.sequence
    val hasAtLeast1Keypoints = stateHolder.hasAtLeast1Keypoints
    val mood = stateHolder.mood
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val previewView = remember { PreviewView(context) }
    val analysisExecutor = remember {
        Executors.newSingleThreadExecutor().apply {
            // 예: 스레드 이름 지정 가능
            (this as? ThreadPoolExecutor)?.threadFactory = Executors.defaultThreadFactory()
        }
    }
    val analyzer = remember { ThrottledImageAnalyzer(context, stateHolder) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            cameraProvider?.let {
                startCameraWithAnalysis(
                    lifecycleOwner, it, previewView, analysisExecutor, analyzer
                )
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            analysisExecutor.shutdownNow() // 안전한 종료
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        if (!hasAtLeast1Keypoints) {
            TranslatorHint()
        } else {
            TranslationResult(mood)
        }
    }
}


private fun startCameraWithAnalysis(
    lifecycleOwner: LifecycleOwner,
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    analysisExecutor: Executor,
    analyzer: ImageAnalysis.Analyzer
) {
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val cameraPreview = CameraPreview.Builder()
        .setTargetRotation(previewView.display.rotation)
        .build()
        .also {
            it.surfaceProvider = previewView.surfaceProvider
        }

    val imageAnalyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetRotation(previewView.display.rotation)
        .build()
        .also {
            it.setAnalyzer(analysisExecutor, analyzer)
        }

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            cameraPreview,
            imageAnalyzer
        )
        Log.d("CameraComposable", "Camera bound to lifecycle with analysis successfully")
    } catch (exc: Exception) {
        Log.e("CameraComposable", "Use case binding failed", exc)
    }
}