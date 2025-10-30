# HappyDog Android App - AI Coding Agent Instructions

- **Android Architecture Compliance (must follow)**: Adhere end-to-end to Google's official Android Architecture recommendations: https://developer.android.com/topic/architecture/recommendations?hl=en. Enforce separation of concerns across layered architecture (UI, Domain, Data), MVVM with unidirectional data flow and a single source of truth, repositories as data boundaries, coroutines/Flow between layers, lifecycle-aware state collection (repeatOnLifecycle / collectAsStateWithLifecycle), and Hilt DI. ViewModels expose a single immutable uiState (StateFlow) created with stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial). Avoid Android lifecycle references or AndroidViewModel in ViewModels; never place business logic in Activities/Fragments/Composables.
- **Professional, production-grade code**: All code must be production quality—clear, typed APIs; minimal but meaningful tests when changing public behavior; precise naming; comments only when non-obvious; remove dead code; no TODOs without an owner/issue; follow Kotlin/Android style guides.
- **Root-cause fixes only**: When resolving errors, identify and fix the true root cause—do not add workarounds, silence exceptions, blanket-disable lint rules, or add arbitrary delays. Document the cause and resolution in commits and add a regression test when feasible.
- **File corruption awareness**: EyeHealthScreen.kt and similar UI files are prone to import/content corruption. When build errors mention "Function declaration must have a name" or duplicate imports, delete corrupted files completely and recreate them cleanly rather than attempting to edit.

## HappyDog2 – AI Agent Working Guide

**Architecture and State Management**
- Modules: UI/Domain/Data inside `app/`, shared: `core:common`, `core:navigation`, `core:designsystem` (see `settings.gradle.kts`)
- MVVM + UDF/SSoT: ViewModel owns a single `StateFlow<UiState>`; repositories persist via DataStore. No mutable state to UI.
- Only expose `suspend`/`Flow` from Data/Domain. Launch coroutines in ViewModel with `viewModelScope`.
- State collection pattern: Use `collectAsStateWithLifecycle()` in UI, `stateIn(scope, SharingStarted.WhileSubscribed(5000), initial)` in ViewModels

**Critical StateFlow Pattern**
- ALL ViewModels must expose state via `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)`
- Example from `MainViewModel.kt`: `val isLoggedIn: StateFlow<Boolean> = userRepository.getAccessToken().map { !it.isNullOrBlank() }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = false)`
- UI collection: `val uiState by viewModel.uiState.collectAsStateWithLifecycle()` in all Composables
- Never expose MutableStateFlow to UI; always use StateFlow/immutable data classes

**Navigation System**
- Sealed class routes in `core/navigation/NavigationRoutes.kt`: `Screen.Login`, `Screen.PetCare`, `Screen.EyeHealth`, etc.
- Navigation logic in `MainActivity.kt`: `!isLoggedIn -> Screen.Login.route`, `!hasPet -> Screen.PetRegistration.route`, else `Screen.PetCare.route`
- Single-pet policy enforced in navigation: `LaunchedEffect(hasPet, currentRoute, isLoggedIn)` redirects to registration if no pet exists
- Bottom nav routes: `Screen.PetCare`, `Screen.Map`, `Screen.Community`, `Screen.Translator`, `Screen.MyPage`
- Deep links: Register constants in `core/navigation/DeepLinks.kt` and routes in `core/navigation/Routes.kt`

**Networking, Auth, and Idempotency**
- Retrofit + OkHttp; APIs under `app/data/remote/api/*Api.kt`; provided via Hilt in `NetworkModule.kt`
- Bearer auth: `AuthInterceptor` injects token; `TokenAuthenticator` refreshes on 401 using empty JSON `{}` body
- **Idempotency (Critical)**: ALL write requests (POST/PUT/PATCH) auto-inject `X-Idempotency-Key` (UUIDv4) via `IdempotencyInterceptor`. Treat response headers `Idempotent-Replay: true` or `Idempotency-Replay: true` as success replay. Apply to DELETE for specific endpoints (cartoon jobs). See `docs/DEEPLINKS_AND_IDEMPOTENCY.md`
- DTOs use Gson `@SerializedName` (`app/data/remote/dto/**`); map to domain in `app/data/mapper/**`
- Align with `docs/openapi_pretty.json` and `docs/api/*` documentation

**DI and Repository Binding (Hilt)**
- All repositories bind in `RepositoryModule.kt` using `@Binds @Singleton abstract fun bind*Repository(*RepositoryImpl): *Repository`
- Pattern: 1) Define in `domain/repository/*`, 2) Implement in `data/repository/*Impl.kt`, 3) Bind in `RepositoryModule`
- Hilt modules in `app/src/main/java/com/example/pet_project_frontend/core/di/` (NetworkModule, RepositoryModule, DataStoreModule, AppModule)

**Storage and Environment Setup**
- Tokens only: `TokenManager` via AndroidX DataStore. No other client-side selections persisted (single-pet policy)
- Backend URL: `local.properties` → `BuildConfig.API_BASE_URL`. Emulator: `http://10.0.2.2:5000/`. Real device: use `ipconfig` IPv4 + `:5000`
- Required keys in `local.properties`: `API_BASE_URL`, `GOOGLE_SERVER_CLIENT_ID`, `KAKAO_NATIVE_APP_KEY`
- Firebase: Add `app/google-services.json` and register SHA1 (`gradlew.bat signingReport`)

**Build and Development (Windows)**
- Build: `.\gradlew.bat assembleDebug` (use `.\` prefix in PowerShell)
- Tests: `gradlew.bat test`; Device tests: `gradlew.bat connectedAndroidTest`
- Backend setup: Use appropriate conda env from `pet_project_backend/envs/` (Windows CPU/CUDA, macOS CPU)

**Navigation Architecture Patterns**
- `PetCareNavigation.kt` defines all screen routes in `NavHost` with proper argument handling
- Two-tier routing: Management screens (`*Management.route`) → Record screens (`*Record.route`)
- Example: `FeedManagement` → `FeedRecord`, `ActivityManagement` → `ActivityRecord`
- Deep link support via `navDeepLink` with URI patterns from `DeepLinks.kt`
- Screen arguments use `navArgument` with proper types and nullability

**Critical Component Patterns**
- ViewModel state: Single `StateFlow<UiState>` exposed via `stateIn(WhileSubscribed(5000))` - see `MainViewModel.kt`
- UI collection: `collectAsStateWithLifecycle()` - see `MainActivity.kt` branching on `isLoggedIn`/`hasPet`
- Repository implementation: Return domain models wrapped in `AppResult<T>`, never throw exceptions to UI layer
- Error handling: Map to domain errors via `SafeApi.body { }`, convert to `UiState.Error` in ViewModel with user-safe messages
- UiState initial loading: ALWAYS start with `isLoading = false` to prevent infinite loading states; only set true during actual operations

**Critical UiState Anti-Pattern (MUST AVOID)**
❌ NEVER initialize UiState with `isLoading = true`:
```kotlin
data class UiState(val isLoading: Boolean = true)  // WRONG - causes infinite loading
```
✅ ALWAYS initialize with `isLoading = false`:
```kotlin
data class UiState(val isLoading: Boolean = false)  // CORRECT
```
- Set `isLoading = true` only when operation starts
- Immediately reset to `false` after operation completes (both success/error)
- See `WeightManagementViewModel.kt` for correct pattern

**New Feature Implementation Checklist**
1) **API Layer**:
   - Add interface in `data/remote/api/*Api.kt` with Retrofit annotations
   - Create DTOs in `data/remote/dto/request|response/` with `@SerializedName` annotations
   - Provide API in `NetworkModule.kt` if creating new service
   - Align with `docs/openapi_pretty.json` and `docs/api/*` specs
   
2) **Repository Layer**:
   - Define contract in `domain/repository/*Repository.kt` (interface)
   - Implement in `data/repository/*RepositoryImpl.kt` with `@Singleton`
   - Wrap all API calls in `SafeApi.body { }` to return `AppResult<T>`
   - Bind in `RepositoryModule.kt` using `@Binds @Singleton abstract fun`
   
3) **ViewModel Layer**:
   - Inject repository via constructor with `@Inject`
   - Define nested `data class UiState(val isLoading: Boolean = false, ...)`
   - Expose single `StateFlow<UiState>` via `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())`
   - Handle AppResult: `is Success -> _uiState.update { ... }`, `is Error -> _uiState.update { copy(error = ...) }`
   - Launch coroutines only in ViewModel, never in repository
   
4) **UI Layer**:
   - Collect state: `val uiState by viewModel.uiState.collectAsStateWithLifecycle()`
   - Handle loading/error/success states in UI
   - Send events to ViewModel methods, never manipulate state directly
   - Navigate via `Screen.*.route` or `Screen.*.createRoute(params)`
   
5) **Navigation Layer**:
   - Add sealed class entry in `NavigationRoutes.kt`: `object ScreenName : Screen("route?param={param}")`
   - Implement in `PetCareNavigation.kt`: `composable(route, arguments, deepLinks) { }`
   - Pass date parameters via navigation arguments with proper nullability
   - Use `hiltViewModel()` to get ViewModels in composable blocks

**Critical Don'ts**
- Don't start coroutines in repository/domain layers - only expose `suspend`/`Flow`
- Don't leak mutable state to UI - always use StateFlow/immutable data
- Don't log secrets/tokens. Inject keys only via `BuildConfig`/`manifestPlaceholders`
- Don't attempt to edit corrupted files with duplicate imports - delete and recreate instead
- Don't bypass idempotency for write operations - let `IdempotencyInterceptor` handle automatically
- Don't initialize UiState with `isLoading = true` - always start with `false`
- Don't throw exceptions from repository to ViewModel - wrap in `AppResult<T>`
- Don't call repository methods from composables - only from ViewModels

**Key Reference Files**
- Navigation: `core/navigation/NavigationRoutes.kt`, `PetCareNavigation.kt`, `MainActivity.kt`
- State patterns: `MainViewModel.kt` (WhileSubscribed), `MainActivity.kt` (lifecycle collection), `WeightManagementViewModel.kt` (correct loading pattern)
- Network setup: `NetworkModule.kt`, `data/remote/interceptors/`, `data/remote/authenticator/TokenAuthenticator.kt`
- Idempotency: `data/remote/interceptors/IdempotencyInterceptor.kt`, `docs/DEEPLINKS_AND_IDEMPOTENCY.md`
- Repository pattern: `data/repository/PetCareRepositoryImpl.kt` (SafeApi.body usage), `core/di/RepositoryModule.kt` (@Binds pattern)
- DTO examples: `data/remote/dto/response/WeightMonthlyAnalysisResponse.kt` (@SerializedName usage)
