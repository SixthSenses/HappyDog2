# Pets 도메인 명세

반려동물 등록/프로필/바이오메트릭 분석(비문/안구).

기본 정보
- 베이스 URL: `/api`
- 태그: `pets`
- 인증: Bearer 필요
- 멱등성: `POST /api/pets`, `POST /api/pets/{pet_id}/nose-print`, `POST /api/pets/{pet_id}/eye-analysis`에 적용(see DEEPLINKS_AND_IDEMPOTENCY)

주요 스키마
- `PetRegistrationSchema`, `PetProfileResponseSchema`, `PetUpdateSchema`, `BiometricAnalysisRequestSchema`, `NosePrintRegistrationResponseSchema`, `EyeAnalysisResponseSchema`

Endpoints
1) POST `/api/pets/`
- 설명: 반려동물 등록(사용자당 1마리 제한)
- 요청: `PetRegistrationSchema`
- 응답: 201(JSON `PetProfileResponseSchema`)
- 멱등성: 적용

2) GET `/api/pets/{pet_id}`
- 설명: 소유자 전용 프로필 조회(비공개 정보 포함)
- 응답: 200(JSON `PetProfileResponseSchema`)

3) PATCH `/api/pets/{pet_id}`
- 설명: 프로필 부분 업데이트
- 요청: `PetUpdateSchema`
- 응답: 200(JSON `PetProfileResponseSchema`)

4) POST `/api/pets/{pet_id}/nose-print`
- 설명: 비문 등록/인증
- 요청: `BiometricAnalysisRequestSchema`
- 응답: 200(JSON `NosePrintRegistrationResponseSchema`)
- 멱등성: 적용

5) POST `/api/pets/{pet_id}/eye-analysis`
- 설명: 안구 분석
- 요청: `BiometricAnalysisRequestSchema`
- 응답: 200(JSON `EyeAnalysisResponseSchema`)
- 멱등성: 적용

6) GET `/api/pets/profile`
- 설명: view 파라미터에 따른 뷰 기반 프로필 조회(정책 기반 필터링)
- 응답: 200(JSON `PetViewBasedResponseSchema`)
