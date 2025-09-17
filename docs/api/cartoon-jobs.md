# Cartoon-Jobs 도메인 명세

만화 변환 작업 생성/조회/취소 및 헬스체크.

기본 정보
- 베이스 URL: `/api`
- 태그: `cartoon-jobs`
- 인증: Bearer 필요
- 멱등성: `POST /api/cartoon-jobs/`(생성), `DELETE /api/cartoon-jobs/{job_id}`(취소) 적용

스키마
- `CartoonJobCreateSchema`, `CartoonJobResponseSchema`, `CartoonJobHealthResponseSchema`

Endpoints
1) POST `/api/cartoon-jobs/`
- 설명: 만화 스타일 변환 비동기 작업 생성
- 요청: `CartoonJobCreateSchema`
- 응답: 202(JSON `CartoonJobResponseSchema`)
- 멱등성: 적용

2) GET `/api/cartoon-jobs/{job_id}`
- 설명: 작업 상태 조회
- 응답: 200(JSON `CartoonJobResponseSchema`)

3) DELETE `/api/cartoon-jobs/{job_id}`
- 설명: 작업 취소(상태에 따라 `INVALID_STATE_FOR_CANCEL` 등 도메인 오류 가능)
- 응답: 200(JSON `CartoonJobResponseSchema`)
- 멱등성: 적용(삭제 멱등)

4) GET `/api/cartoon-jobs/health`
- 설명: 작업 프로세서 건강 상태 조회
- 응답: 200(JSON `CartoonJobHealthResponseSchema`)
