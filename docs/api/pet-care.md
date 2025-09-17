# Pet-Care 도메인 명세

펫케어 설정 및 기록 CRUD.

기본 정보
- 베이스 URL: `/api`
- 태그: `pet-care`
- 인증: Bearer 필요
- 멱등성: `POST /api/pet-care/{pet_id}/records` 적용

스키마
- `PetCareSettingsSchema`, `CareRecordCreateSchema`, `CareRecordUpdateSchema`, `RecordResponseSchema`, `DailyRecordsResponseSchema`

Endpoints
1) GET `/api/pet-care/{pet_id}/settings`
- 설명: 펫케어 설정 조회
- 응답: 200(JSON `PetCareSettingsSchema`)

2) PUT `/api/pet-care/{pet_id}/settings`
- 설명: 펫케어 설정 수정(부분 업데이트 지원)
- 요청: `PetCareSettingsSchema`
- 응답: 200(JSON `PetCareSettingsSchema`)

3) POST `/api/pet-care/{pet_id}/records`
- 설명: 개별 펫케어 기록 생성(식사, 활동, BCS, 체중, 대변, 구토)
- 요청: `CareRecordCreateSchema`
- 응답: 201(JSON `RecordResponseSchema`)
- 멱등성: 적용

4) PATCH `/api/pet-care/{pet_id}/records/{log_id}`
- 설명: 기록 수정
- 요청: `CareRecordUpdateSchema`
- 응답: 200(JSON `RecordResponseSchema`)

5) DELETE `/api/pet-care/{pet_id}/records/{log_id}`
- 설명: 기록 삭제
- 응답: 204(No Content)

6) GET `/api/pet-care/{pet_id}/records/daily`
- 설명: 특정 날짜 일괄 조회
- 응답: 200(JSON `DailyRecordsResponseSchema`)
