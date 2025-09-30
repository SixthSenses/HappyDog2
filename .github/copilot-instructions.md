## Non-negotiables

- Android Architecture Compliance (must follow): Adhere end-to-end to Google’s official Android Architecture recommendations: https://developer.android.com/topic/architecture/recommendations?hl=en. Enforce separation of concerns across layered architecture (UI, Domain, Data), MVVM with unidirectional data flow and a single source of truth, repositories as data boundaries, coroutines/Flow between layers, lifecycle-aware state collection (repeatOnLifecycle / collectAsStateWithLifecycle), and Hilt DI. ViewModels expose a single immutable uiState (StateFlow) created with stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial). Avoid Android lifecycle references or AndroidViewModel in ViewModels; never place business logic in Activities/Fragments/Composables.
- Professional, production-grade code: All code must be production quality—clear, typed APIs; minimal but meaningful tests when changing public behavior; precise naming; comments only when non-obvious; remove dead code; no TODOs without an owner/issue; follow Kotlin/Android style guides.
- Root-cause fixes only: When resolving errors, identify and fix the true root cause—do not add workarounds, silence exceptions, blanket-disable lint rules, or add arbitrary delays. Document the cause and resolution in commits and add a regression test when feasible.

## HappyDog2 – AI Agent Working Guide (concise)

Architecture and state
- Modules: UI/Domain/Data inside `app/`, shared: `core:common`, `core:navigation`, `core:designsystem` (see `settings.gradle.kts`).
- MVVM + UDF/SSoT: ViewModel owns a single `StateFlow<UiState>`; repositories persist via DataStore. No mutable state to UI.
- Only expose `suspend`/`Flow` from Data/Domain. Launch coroutines in ViewModel with `viewModelScope`.

UI, navigation, and examples
- Collect state with `collectAsStateWithLifecycle()`; see `app/MainActivity.kt` (branches on `isLoggedIn` and `hasPet`).
- Start route is `Screen.PetCare`; if `hasPet == false`, redirect to registration (see `PetCareNavigation.kt`, `NavigationRoutes.kt`).
- Deep links live in `core/navigation/DeepLinks.kt`; e.g., register `pet_care/dashboard?petId={?}&date={?}&tab={?}`.
- Use `stateIn(scope, SharingStarted.WhileSubscribed(5000), initial)`; example in `app/MainViewModel.kt`.

Networking, auth, and idempotency
- Retrofit + OkHttp; APIs under `app/data/remote/api/*Api.kt`; provided via Hilt in `core/di/NetworkModule.kt`.
- Bearer auth: `AuthInterceptor` injects token; `data/remote/authenticator/TokenAuthenticator.kt` refreshes on 401 using an empty JSON `{}` body and a loop-guard header.
- Idempotency: Write requests add `X-Idempotency-Key` (UUIDv4). Auto-injection via `data/remote/interceptors/IdempotencyInterceptor.kt`. Treat `Idempotent-Replay|Idempotency-Replay: true` as success replay. Policy: `docs/DEEPLINKS_AND_IDEMPOTENCY.md`.
- DTOs use Gson `@SerializedName` (`app/data/remote/dto/**`); map to domain in `app/data/mapper/**`. Align with `docs/openapi_pretty.json` and `docs/api/*`.

Storage and DI
- Tokens only: `TokenManager` via AndroidX DataStore (see usages in `data/repository/*Impl.kt`). No other client-side selections persisted (single-pet policy).
- Hilt modules in `core/di/*.kt` (NetworkModule, RepositoryModule, DataStoreModule, AppModule). Bind repositories in `RepositoryModule` to `app/domain/repository/*`.

Build, run, and local environment (Windows/cmd)
- Build APK: `gradlew.bat assembleDebug`; run unit tests: `gradlew.bat test`; device tests: `gradlew.bat connectedAndroidTest`.
- Firebase: add `app/google-services.json` and register SHA1 (`gradlew.bat signingReport`).
- Backend base URL in `local.properties` → `BuildConfig.API_BASE_URL`. Emulator: `http://10.0.2.2:5000/`. Real device: use `ipconfig` IPv4, e.g., `http://<IPv4>:5000/`.
- Required keys in `local.properties`: `API_BASE_URL`, `GOOGLE_SERVER_CLIENT_ID`, `KAKAO_NATIVE_APP_KEY`. Kakao placeholders come from BuildConfig.

Do and Don’t
- Do: centralize URLs/routes (`BuildConfig`, `core:navigation`), and strings in `strings.xml`/`core:common`.
- Don’t: start coroutines in repository/domain, leak mutable state, or log secrets/tokens.

When adding a feature
1) API: define in `data/remote/api` (+ Hilt provide if needed).
2) Repository: add contract `domain/repository`, implement `data/repository/*Impl`, bind in `RepositoryModule`.
3) ViewModel: expose single `uiState` via `stateIn(WhileSubscribed(5000))` and intents.
4) UI: collect with lifecycle and navigate via `Screen.*.route` and `DeepLinks`.

Pointers
- Policies: `docs/DEEPLINKS_AND_IDEMPOTENCY.md`. OpenAPI: `docs/openapi_pretty.json`. Navigation helpers: `core/navigation/DeepLinks.kt`.
- Examples to follow: `app/MainViewModel.kt`, `app/MainActivity.kt`, `data/remote/interceptors/IdempotencyInterceptor.kt`.