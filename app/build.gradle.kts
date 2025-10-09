// [수정됨] 'java.util.Properties'를 사용하기 위해 import 구문을 파일 최상단에 추가합니다.
import java.util.Properties

// Helper to strip accidental quotes from properties like "VALUE" or 'VALUE'
fun String.unquote(): String = this.trim().removeSurrounding("\"").removeSurrounding("'")

// local.properties 파일을 읽기 위한 코드
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { input ->
        localProperties.load(input)
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.pet_project_frontend"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pet_project_frontend"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

    // Populate BuildConfig from local.properties so runtime injection has actual values
    val googleClientIdRaw = localProperties.getProperty("GOOGLE_SERVER_CLIENT_ID")
        ?: (project.findProperty("GOOGLE_SERVER_CLIENT_ID") as? String)
        ?: ""
    val kakaoNativeAppKeyRaw = localProperties.getProperty("KAKAO_NATIVE_APP_KEY")
        ?: (project.findProperty("KAKAO_NATIVE_APP_KEY") as? String)
        ?: ""

    val googleClientId = googleClientIdRaw.unquote()
    val kakaoNativeAppKey = kakaoNativeAppKeyRaw.unquote()

    buildConfigField("String", "GOOGLE_SERVER_CLIENT_ID", "\"$googleClientId\"")
    buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKey\"")

    // For Kakao Map SDK meta-data replacement in AndroidManifest
    manifestPlaceholders["kakaoAppKey"] = kakaoNativeAppKey
    // Some Kakao artifacts reference upper-cased placeholder name; set both to be safe
    manifestPlaceholders["KAKAO_APP_KEY"] = kakaoNativeAppKey
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            // local.properties 우선, 없으면 -P API_BASE_URL, 그마저도 없으면 에뮬레이터 기본값
            val rawApiBaseUrl = (localProperties.getProperty("API_BASE_URL")
                ?: (project.findProperty("API_BASE_URL") as? String)
                ?: "http://10.0.2.2:5000/")
            val apiBaseUrl = rawApiBaseUrl.unquote()
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_BASE_URL", "\"https://api.happydog.com/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        // Compose Compiler 1.5.14 is compatible with Kotlin 1.9.24
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Dependency Injection - Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("com.google.dagger:dagger:2.50")
    // Network - Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // uCrop 최신 (원형 디밍 레이어 지원) - 마이페이지 프로필 설정
    implementation("com.github.yalantis:ucrop:2.2.11")

    // JSON Parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Room Database (KSP)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Google Services
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Kakao Map SDK
    implementation ("com.kakao.maps.open:android:2.11.9")
    implementation("com.kakao.sdk:v2-user:2.19.0")
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Date/Time Support for older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.select.tf.ops)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:navigation"))
    implementation(project(":core:designsystem"))
}

kapt {
    correctErrorTypes = true
}

