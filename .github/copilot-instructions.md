# HappyDog Android App - AI Coding Agent Instructions

## Project Overview
HappyDog is a native Android pet care app built with **Jetpack Compose** and **Clean Architecture**. The app provides pet management, social features (멍스타그램), maps, translation, and care tracking.

- **Language**: Kotlin 1.9.24
- **UI**: Jetpack Compose (Material3, Compose BOM 2024.06.00)
- **DI**: Hilt 2.50
- **Network**: Retrofit 2.9.0 + OkHttp 4.12.0
- **Backend API**: FastAPI (OpenAPI spec in `docs/openapi_pretty.json`)
- **Package**: `com.example.pet_project_frontend`

## Architecture & Module Structure

### Multi-Module Setup
```
app/              # Main application module (@HiltAndroidApp)
core/
  ├─ common/      # SafeApi, AppResult, shared utilities
  ├─ designsystem/# Compose theme & reusable UI components
  └─ navigation/  # Screen sealed class, navigation routes
```

### Clean Architecture Layers (in `app/`)
```
presentation/     # @HiltViewModel, Compose UI (@Composable screens)
domain/          # Repository interfaces, Use Cases, domain models
data/
  ├─ remote/     # Retrofit API interfaces, DTOs, interceptors
  ├─ repository/ # RepositoryImpl classes (@Singleton)
  ├─ local/      # DataStore (TokenManager), Room DAO (places)
  └─ mapper/     # DTO ↔ Domain model conversion
```

**Flow**: UI → ViewModel (@Inject UseCase) → UseCase (@Inject Repository) → RepositoryImpl (@Inject ApiService) → Retrofit

## Key Conventions & Patterns

### 1. Dependency Injection (Hilt)
- **Application**: `PetCareApplication` is `@HiltAndroidApp`
- **ViewModels**: `@HiltViewModel` + constructor `@Inject`
- **MainActivity**: `@AndroidEntryPoint`, injects `@Named("NATIVE_APP_KEY")` for Kakao Map SDK
- **Modules**:
  - `NetworkModule`: Provides Retrofit, OkHttpClient, API interfaces, interceptors (AuthInterceptor, IdempotencyInterceptor, TokenAuthenticator)
  - `RepositoryModule`: Binds repository interfaces to implementations
  - `DataModule`: Provides DataStore, TokenManager, Room DB

### 2. Network Layer
- **Base URL**: Configured via `BuildConfig.API_BASE_URL` (from `local.properties` → `API_BASE_URL`)
  - Debug default: `http://10.0.2.2:5000/` (Android emulator localhost)
  - Release: `https://api.happydog.com/`
- **Error Handling**: Use `SafeApi.response {}` or `SafeApi.body {}` (returns `AppResult<T>`)
  - `AppResult.Success<T>`, `AppResult.Error(code, message)`, `AppResult.Exception(throwable)`
- **Idempotency**: POST/PUT/PATCH requests auto-include `X-Idempotency-Key` via `IdempotencyInterceptor`
  - Docs: `docs/DEEPLINKS_AND_IDEMPOTENCY.md`
  - Backend returns `Idempotent-Replay: true` for replayed requests
- **Auth**: 
  - `AuthInterceptor` adds `Authorization: Bearer <token>` from `TokenManager`
  - `TokenAuthenticator` auto-refreshes token on 401
  - Two Retrofit instances: `@Named("AuthRetrofit")` (no auth) for login/refresh, default for protected endpoints

### 3. Navigation (Compose)
- **Routes**: Defined in `Screen` sealed class (`core/navigation/NavigationRoutes.kt`)
  - Main: `Login`, `PetRegistration`, `PetCare`, `Map`, `Community`, `Translator`, `MyPage`
- **NavHost**: `PetCareNavHost` in `MainActivity` (entry point based on auth + pet status)
- **ViewModel Scoping**: Share ViewModels across nav graph via `hiltViewModel(navController.getBackStackEntry(parentRoute))`
  - Example: `MyPageViewModel` shared across all MyPage sub-screens
  - Example: `IdentityVerificationViewModel` shared across verification flow
- **Bottom Nav**: Shows for main screens only (`PetCare`, `Map`, `Community`, `Translator`, `MyPage`)
- **Deep Links**: Registered in `AndroidManifest.xml` and nav graph (`app://pet-care/dashboard`)

### 4. State Management
- **UI State**: Each screen has a `UiState` data class (e.g., `MungStarUiState(isLoading, posts, error)`)
- **StateFlow**: ViewModels expose `StateFlow<UiState>`, UI collects via `collectAsStateWithLifecycle()`
- **Loading/Error**: Always handle `isLoading`, `error: String?` in UI state

### 5. API Integration
- **OpenAPI Spec**: `docs/openapi_pretty.json` (FastAPI backend)
- **DTOs**: Named with `Dto`, `Request`, `Response` suffixes (e.g., `PostCreateDto`, `PetProfileResponse`)
- **Mappers**: `toDomain()` extension functions convert DTOs to domain models
- **Repositories**: All network calls wrapped in `SafeApi.response { apiService.method() }`

### 6. Secrets & Config
- **`local.properties`** (gitignored):
  ```properties
  KAKAO_NATIVE_APP_KEY="your_key"
  GOOGLE_SERVER_CLIENT_ID=your_id
  API_BASE_URL="http://10.0.2.2:5000/"
  ```
- **BuildConfig**: Keys injected via `buildConfigField` in `app/build.gradle.kts`
- **Manifest Placeholders**: `${kakaoAppKey}` replaced at build time

## Common Development Workflows

### Build & Run
```powershell
# Build debug APK
.\gradlew.bat assembleDebug

# Install & run on emulator
.\gradlew.bat installDebug

# Run from IDE: Use "Build Debug (Gradle)" run configuration
```

### API Testing
- Check backend API docs: `docs/openapi_pretty.json`
- Test endpoints via Retrofit API interfaces in `data/remote/api/`
- Debug network calls: OkHttp logging enabled in debug builds (see Logcat with tag `OkHttp`)

### Adding New Features
1. **Domain**: Define model in `domain/model/`, repository interface in `domain/repository/`
2. **Data**: Create DTO in `data/remote/dto/`, API interface in `data/remote/api/`, implement repository
3. **DI**: Add `@Provides` for API in `NetworkModule`, `@Binds` for repository in `RepositoryModule`
4. **Presentation**: Create ViewModel + Screen, register route in `Screen` sealed class, add to `PetCareNavHost`

### Authentication Flow
1. User taps Google login → `LoginScreen` → `AuthRepository.socialLogin(authCode)`
2. Backend returns `access_token` + `refresh_token` → saved to DataStore via `TokenManager`
3. All protected API calls auto-include token via `AuthInterceptor`
4. On 401 → `TokenAuthenticator` auto-refreshes → retries original request
5. Logout → `AuthRepository.logout()` → clears tokens → navigate to `Login`

### Common Gotchas
- **KSP**: Hilt uses KSP (not kapt). Build errors? Clean project + rebuild.
- **Kakao Map**: Requires valid `KAKAO_NATIVE_APP_KEY` in `local.properties`. Check `MainActivity` logs for key hash.
- **Emulator Network**: Use `10.0.2.2` for Android emulator to access host's `localhost:5000`.
- **Idempotency**: Backend requires UUIDv4 in `X-Idempotency-Key` for write operations. Handled automatically by `IdempotencyInterceptor`.
- **ViewModel Scope**: For multi-screen flows, get parent entry's ViewModel: `hiltViewModel(navController.getBackStackEntry(parentRoute))`

## File Naming Patterns
- **Screens**: `*Screen.kt` (e.g., `LoginScreen.kt`, `MyPageScreen.kt`)
- **ViewModels**: `*ViewModel.kt` (e.g., `LoginViewModel.kt`)
- **Repositories**: `*RepositoryImpl.kt` implements `*Repository` interface
- **API Services**: `*Api.kt` (e.g., `PostApi.kt`, `PetApi.kt`)
- **DTOs**: `*Dto.kt`, `*Request.kt`, `*Response.kt`

## Testing Notes
- Test instrumentation: AndroidJUnit4 runner configured
- No unit tests currently implemented (opportunity for contribution!)

## External Dependencies
- **Firebase**: `google-services.json` in `app/` (Google Auth, potential FCM)
- **Kakao Map SDK**: Via maven `devrepo.kakao.com`, initialized in `MainActivity.onCreate()`
- **uCrop**: For circular profile image cropping
- **TensorFlow Lite**: Listed in `libs.versions.toml` (unused in current code?)

## Questions for Clarification
1. **Testing Strategy**: Should we add unit/integration tests? Any specific testing patterns preferred?
2. **Multi-Pet Support**: Code assumes 1 pet per user. Is multi-pet planned?
3. **TensorFlow Lite**: Models listed in `libs.versions.toml` but not used. Planned for dog nose-print verification?
4. **Localization**: UI strings hardcoded in Korean. Add string resources for i18n?
5. **Cartoon Feature**: `CartoonRepository` exists but UI/ViewModel incomplete. Priority?

---

**Key Files to Reference**:
- Architecture: `app/src/main/java/com/example/pet_project_frontend/MainActivity.kt`
- DI Setup: `core/di/NetworkModule.kt`, `core/di/RepositoryModule.kt`
- Navigation: `core/navigation/PetCareNavigation.kt`
- API Patterns: `data/repository/PostRepositoryImpl.kt`, `domain/repository/PostRepository.kt`
- Error Handling: `core/common/SafeApi.kt`, `data/remote/interceptors/`
