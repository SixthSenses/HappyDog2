# HappyDog2 AI Agent Briefing

본 문서는 HappyDog2 프론트 작업을 수행할 AI 에이전트에게 제공하는 표준 브리핑입니다. 백엔드 스펙은 `docs/openapi_pretty.json`(Postman 컬렉션과 동등)이며, 이 브리핑과 함께 전달하면 긴 문서 없이도 일관된 산출물을 얻을 수 있습니다.

## 프로젝트 개요
- 플랫폼: Android (Kotlin, Jetpack Compose, MVVM)
- 레이어: UI → Domain → Data. 상태는 ViewModel의 단일 StateFlow(UDF/SSoT)
- 네트워킹: Retrofit + OkHttp, Hilt DI, Bearer 인증, Idempotency 헤더 사용
- 소스: 앱 모듈 `app/`, 코어 공유 모듈 `core/*`

## 백엔드 스펙 소스
- 단일 소스: `docs/openapi_pretty.json`
- 동일한 Postman 컬렉션: JH 워크스페이스 "HappyDog API2"

## 인증/보안 규칙 요약
- Bearer 토큰 필요: `/api/*` 대부분. 토큰은 `TokenManager(DataStore)`를 통해 저장/주입
- 401: `TokenAuthenticator`로 갱신 시도 → 실패 시 로그인 흐름으로 복귀
- 403/404: 보호된 리소스 접근 시 선택된 펫 ID 초기화 등 글로벌 상태 복구
- 쓰기 요청(POST/PUT/PATCH): `X-Idempotency-Key` 반드시 첨부(UUIDv4)

## 네트워크 구현 규칙
- Retrofit 인터페이스: `app/data/remote/api/*Api.kt`
- DTO: `app/data/remote/dto`, Gson `@SerializedName`
- Mapper: `app/data/mapper`
- Repository: Domain 인터페이스(`app/domain/repository`) ↔ Impl(`app/data/repository`)
- DI: `core/di/NetworkModule.kt`, `RepositoryModule.kt`

## UI 상태/패턴
- ViewModel은 단일 `StateFlow<UiState>`만 노출(WhileSubscribed(5000))
- UI는 `collectAsStateWithLifecycle()`로 수집, 이벤트는 ViewModel로 역방향 전송
- 오류 표준: `UiState.Error(code, message)`로 매핑

## 라우팅/딥링크
- 시작 화면: PetCare. `selectedPetId` 없으면 등록 플로우로 라우팅
- 딥링크는 `core/navigation`에 상수 정의 및 NavHost 등록

## 엔드포인트 인덱스(요약)
- 헬스: GET `/health`
- 인증: GET `/api/auth/google/authorize`, GET/POST `/api/auth/social`, POST `/api/auth/token/refresh`, POST `/api/auth/logout`
- 업로드: POST `/api/uploads/url`, POST `/api/uploads/finalize-cartoon`
- 펫케어: POST `/api/pet-care/:pet_id/records`, GET `/api/pet-care/:pet_id/records/daily`, PATCH/DELETE `/api/pet-care/:pet_id/records/:log_id`
- 알림: GET `/api/notifications`, GET `/api/notifications/unread-count`, POST `/api/notifications/:notification_id/ack`

세부 스키마와 예시는 `openapi_pretty.json`를 참조하세요.

## 에이전트 산출물 기준(Definition of Done)
1) API: Retrofit 인터페이스, DTO, Mapper, Repository 구현 + DI 바인딩
2) ViewModel: 단일 StateFlow로 연결, 에러/로딩/성공 상태 전파, 재시도 인텐트 제공
3) UI(Compose): 상태 수집, 입력 검증, 스낵바/다이얼로그로 오류 피드백, 네비게이션 연동
4) 테스트: 최소 단위 테스트(Mapper/Repository), 간단한 ViewModel 테스트
5) 문서: README 스니펫(화면 흐름, 의존성, 실행 방법)

## 작업 우선순위 예시
1) 인증 흐름: `/api/auth/social`, `/api/auth/token/refresh`, `/api/auth/logout`
2) 펫케어 기록 목록/생성/수정/삭제
3) 알림 목록/미확인 카운트/확인 처리
4) 업로드 URL 발급 + 만화 공개 전환

## 공통 엣지 케이스
- 토큰 만료/갱신 실패, 403 이후 상태 초기화, 네트워크 타임아웃/재시도, 빈 목록, 큰 페이로드 업로드

## 참고 문서
- `docs/DEEPLINKS_AND_IDEMPOTENCY.md` — 딥링크/아이템포턴시 정책
- `docs/openapi_pretty.json` — API 스펙 단일 소스

---
아래 템플릿으로 작업 지시를 전달하면 됩니다.

### 작업 지시 템플릿
- 목표: 무엇을 사용자에게 제공할지 한 줄 요약
- 관련 API: 메서드 + 경로 + 간단 설명
- 화면 상태: UiState 구조와 화면 이벤트 나열
- 성공 기준: 화면/상태/테스트/문서 측면의 완료 정의
- 제약: 디자인시스템/네비게이션 경로/에러 정책 등
