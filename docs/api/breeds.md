# Breeds 도메인 명세

품종 조회/검색.

기본 정보
- 베이스 URL: `/api`
- 태그: `breeds`
- 인증: 불필요(공개 조회)

Endpoints
1) GET `/api/breeds/`
- 설명: 품종 목록 조회
- 응답: 200(JSON `BreedListSchema`)

2) GET `/api/breeds/{breed_name}`
- 설명: 특정 품종 정보 조회
- 응답: 200(JSON `BreedSchema`), 404(ErrorResponseSchema)

3) GET `/api/breeds/search`
- 설명: 품종 검색
- 응답: 200(JSON `BreedListSchema`), 400(ErrorResponseSchema)

4) GET `/api/breeds/exists/{breed_name}`
- 설명: 품종 존재 여부 확인
- 응답: 200(JSON `BreedExistsResponseSchema`)
