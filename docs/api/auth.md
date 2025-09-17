# Auth 도메인 명세

소셜 로그인/OAuth와 토큰 수명주기(재발급, 로그아웃)를 담당합니다. 관련 딥링크/브라우저 플로는 `docs/DEEPLINKS_AND_IDEMPOTENCY.md` 참고.

기본 정보
- 베이스 URL: `/api`
- 태그: `auth`
- 인증: 일부 엔드포인트는 비인증(브라우저 플로 시작/토큰 교환). 그 외는 Bearer 필요할 수 있음.

주요 스키마
- `AuthorizationUrlResponseSchema` — OAuth 인가 URL 정보
- `AuthTokensResponseSchema` — 액세스/리프레시 토큰 묶음
- `AuthTokensRefreshResponseSchema` — 액세스 토큰 재발급 결과
- `LogoutRequestSchema` — 로그아웃 입력
- `SocialLoginSchema` — 소셜 로그인 코드 교환 요청

Endpoints
1) GET `/api/auth/google/authorize`
- 설명: Google OAuth 인증 URL 생성 또는 `?redirect=1` 시 302 리다이렉트
- 응답: 200(JSON `AuthorizationUrlResponseSchema`) 또는 302
- 인증: 없음
- 에러: VALIDATION_ERROR, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, INTERNAL_ERROR

2) POST `/api/auth/social`
- 설명: 소셜 로그인 코드 교환 및 신규 사용자 자동 가입 처리
- 요청: `SocialLoginSchema`(예: `auth_code` 포함)
- 응답: 200(JSON `AuthTokensResponseSchema`)
- 인증: 없음
- 멱등성: 적용(see DEEPLINKS_AND_IDEMPOTENCY) — `X-Idempotency-Key` 사용
- 에러: VALIDATION_ERROR, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, INTERNAL_ERROR

3) GET `/api/auth/social`
- 설명: OAuth 동의 후 서버 측 콜백(웹 브라우저)
- 응답: 200(JSON `AuthTokensResponseSchema`)
- 인증: 없음

4) POST `/api/auth/token/refresh`
- 설명: 유효한 Refresh Token으로 Access Token 재발급
- 요청: `EmptyRequestSchema`
- 응답: 200(JSON `AuthTokensRefreshResponseSchema`)
- 인증: Bearer(Refresh Token 전달은 서버 규약에 따름)
- 에러: 공통 오류 + 토큰 관련 도메인 오류

5) POST `/api/auth/logout`
- 설명: 로그아웃 및 토큰 블랙리스트 등록
- 요청: `LogoutRequestSchema`
- 응답: 200(JSON `AuthLogoutResponseSchema`)
- 인증: Bearer 권장
- 에러: 공통 오류

클라이언트 구현 요약
- Interceptor: `AuthInterceptor`가 Bearer 자동 주입, `TokenAuthenticator`가 401에 대해 재발급 시도(무한루프 방지 헤더 `Retry-After-Refresh`).
- 데이터 보관: `TokenManager`가 DataStore에 Access/Refresh/`selected_pet_id` 관리.
- 딥링크/브라우저: 앱은 `/api/auth/google/authorize`로 브라우저 플로를 시작하고, 앱 내 콜백 스킴은 사용하지 않음. 토큰 교환은 앱에서 `POST /api/auth/social` 수행.
